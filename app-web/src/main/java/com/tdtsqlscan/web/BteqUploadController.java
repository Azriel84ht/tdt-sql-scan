package com.tdtsqlscan.web;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.ddl.CreateIndexParser;
import com.tdtsqlscan.ddl.CreateTableParser;
import com.tdtsqlscan.ddl.DropTableParser;
import com.tdtsqlscan.dml.DeleteParser;
import com.tdtsqlscan.dml.InsertParser;
import com.tdtsqlscan.dml.UpdateParser;
import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.etl.BteqScriptParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.ddl.CreateTableQuery;
import com.tdtsqlscan.ddl.DropTableQuery;
import com.tdtsqlscan.dml.DeleteQuery;
import com.tdtsqlscan.dml.InsertQuery;
import com.tdtsqlscan.dml.UpdateQuery;
import com.tdtsqlscan.etl.BteqCommand;
import com.tdtsqlscan.etl.BteqSqlCommand;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.select.SelectParser;
import com.tdtsqlscan.select.SelectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class BteqUploadController {

    private static final Logger logger = LoggerFactory.getLogger(BteqUploadController.class);

    private final BteqScriptParser bteqScriptParser;
    private final ChainFlowGraphConverter chainFlowGraphConverter;
    private final ObjectMapper objectMapper;

    public static class GraphResponse {
        public Graph chainFlow;
        public Map<String, Graph> bteqFlows;
        public Map<String, FileMetadata> fileMetadata;
    }

    public BteqUploadController() {
        logger.info("Initializing BteqUploadController");
        List<QueryParser> sqlParsers = new ArrayList<>();
        sqlParsers.add(new SelectParser());
        sqlParsers.add(new CreateTableParser());
        sqlParsers.add(new DropTableParser());
        sqlParsers.add(new CreateIndexParser());
        sqlParsers.add(new InsertParser());
        sqlParsers.add(new UpdateParser());
        sqlParsers.add(new DeleteParser());
        this.bteqScriptParser = new BteqScriptParser(sqlParsers);
        this.chainFlowGraphConverter = new ChainFlowGraphConverter();
        this.objectMapper = new ObjectMapper();
        logger.info("BteqUploadController initialized");
    }

    @PostMapping("/upload")
    public GraphResponse handleFileUpload(@RequestParam("files") MultipartFile[] files, @RequestParam("fileOrder") String fileOrderJson) throws IOException {
        logger.info("Received {} files for upload", files.length);

        List<Map<String, String>> fileOrderList = objectMapper.readValue(fileOrderJson, new TypeReference<List<Map<String, String>>>(){});
        Map<String, Integer> fileOrderMap = fileOrderList.stream()
                .collect(Collectors.toMap(map -> map.get("name"), map -> Integer.parseInt(map.get("order"))));


        List<BteqScript> scripts = new ArrayList<>();
        for (MultipartFile file : files) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            BteqScript script = bteqScriptParser.parse(content, file.getOriginalFilename());
            script.setSize(file.getSize());
            script.setEncoding(StandardCharsets.UTF_8.name());
            scripts.add(script);
        }

        // Sort scripts: primary by user order, secondary by script name
        scripts.sort(Comparator
                .comparing((BteqScript s) -> fileOrderMap.getOrDefault(s.getScriptName(), Integer.MAX_VALUE))
                .thenComparing(BteqScript::getScriptName));


        Graph chainFlowGraph = chainFlowGraphConverter.convert(scripts, fileOrderList);
        logger.info("Generated chain flow graph with {} nodes and {} edges", chainFlowGraph.getNodes().size(), chainFlowGraph.getEdges().size());

        Map<String, Graph> bteqFlows = new LinkedHashMap<>();
        for (BteqScript script : scripts) {
            // Important: Create a new converter for each script as it's stateful
            DataFlowGraphConverter dataFlowGraphConverter = new DataFlowGraphConverter();
            Graph bteqFlowGraph = dataFlowGraphConverter.convert(script);
            bteqFlows.put(script.getScriptName(), bteqFlowGraph);
            logger.info("Generated data flow graph for {} with {} nodes and {} edges",
                    script.getScriptName(), bteqFlowGraph.getNodes().size(), bteqFlowGraph.getEdges().size());
        }

        Map<String, FileMetadata> fileMetadataMap = new LinkedHashMap<>();
        for (BteqScript script : scripts) {
            Set<String> createdTables = new HashSet<>();
            Set<String> droppedTables = new HashSet<>();
            Set<String> readTables = new HashSet<>();
            Set<String> writtenTables = new HashSet<>();
            int transactionCount = 0;

            for (BteqCommand command : script.getCommands()) {
                if (command instanceof BteqSqlCommand) {
                    transactionCount++;
                    SQLQuery query = ((BteqSqlCommand) command).getQuery();
                    if (query instanceof SelectQuery) {
                        SelectQuery selectQuery = (SelectQuery) query;
                        for (SQLTableRef tableRef : selectQuery.getTables()) {
                            readTables.add(tableRef.getExpression().split(" ")[0].toUpperCase());
                        }
                    } else if (query instanceof InsertQuery) {
                        InsertQuery insertQuery = (InsertQuery) query;
                        writtenTables.add(insertQuery.getTableName().toUpperCase());
                        if (insertQuery.getSourceTableName() != null) {
                            readTables.add(insertQuery.getSourceTableName().toUpperCase());
                        }
                    } else if (query instanceof UpdateQuery) {
                        UpdateQuery updateQuery = (UpdateQuery) query;
                        writtenTables.add(updateQuery.getTargetTable().toUpperCase());
                        for (String sourceTable : updateQuery.getSourceTables()) {
                            readTables.add(sourceTable.toUpperCase());
                        }
                    } else if (query instanceof DeleteQuery) {
                        DeleteQuery deleteQuery = (DeleteQuery) query;
                        writtenTables.add(deleteQuery.getTable().toUpperCase());
                    } else if (query instanceof CreateTableQuery) {
                        CreateTableQuery createTableQuery = (CreateTableQuery) query;
                        String tableName = createTableQuery.getTableName().toUpperCase();
                        createdTables.add(tableName);
                        writtenTables.add(tableName);
                    } else if (query instanceof DropTableQuery) {
                        DropTableQuery dropTableQuery = (DropTableQuery) query;
                        droppedTables.add(dropTableQuery.getTableName().toUpperCase());
                    }
                }
            }

            FileMetadata metadata = new FileMetadata();
            metadata.setTransactions(transactionCount);

            Set<String> finalInputTables = new HashSet<>(readTables);
            finalInputTables.removeAll(createdTables);

            Set<String> finalOutputTables = new HashSet<>(writtenTables);
            finalOutputTables.removeAll(droppedTables);

            for (String table : finalInputTables) {
                metadata.addInputTable(table);
            }
            for (String table : finalOutputTables) {
                metadata.addOutputTable(table);
            }

            fileMetadataMap.put(script.getScriptName(), metadata);
        }

        GraphResponse response = new GraphResponse();
        response.chainFlow = chainFlowGraph;
        response.bteqFlows = bteqFlows;
        response.fileMetadata = fileMetadataMap;

        return response;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from BTEQ Flow Visualizer!";
    }
}
