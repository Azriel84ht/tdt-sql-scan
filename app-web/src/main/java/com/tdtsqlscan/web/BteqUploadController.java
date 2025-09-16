package com.tdtsqlscan.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.ddl.CreateIndexParser;
import com.tdtsqlscan.ddl.CreateTableParser;
import com.tdtsqlscan.ddl.DropTableParser;
import com.tdtsqlscan.dml.DeleteParser;
import com.tdtsqlscan.dml.InsertParser;
import com.tdtsqlscan.dml.UpdateParser;
import com.tdtsqlscan.etl.BteqCommand;
import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.etl.BteqScriptParser;
import com.tdtsqlscan.etl.BteqSqlCommand;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.select.SelectParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class BteqUploadController {

    private static final Logger logger = LoggerFactory.getLogger(BteqUploadController.class);

    private final BteqScriptParser bteqScriptParser;
    private final ChainFlowGraphConverter chainFlowGraphConverter;
    private final ObjectMapper objectMapper;
    private final Map<String, BteqScript> parsedScripts = new ConcurrentHashMap<>();


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
        parsedScripts.clear();

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
            parsedScripts.put(script.getScriptName(), script);
        }

        // Sort scripts: primary by user order, secondary by script name
        scripts.sort(Comparator
                .comparing((BteqScript s) -> fileOrderMap.getOrDefault(s.getScriptName(), Integer.MAX_VALUE))
                .thenComparing(BteqScript::getScriptName));

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
                    try {
                        String sql = command.getRawText();
                        Statement statement = CCJSqlParserUtil.parse(sql);
                        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();

                        if (statement instanceof Select) {
                            List<String> tableList = tablesNamesFinder.getTableList((Statement) statement);
                            for (String table : tableList) {
                                readTables.add(table.toUpperCase());
                            }
                        } else if (statement instanceof Insert) {
                            Insert insertStatement = (Insert) statement;
                            writtenTables.add(insertStatement.getTable().getName().toUpperCase());
                            if (insertStatement.getSelect() != null) {
                                List<String> tableList = tablesNamesFinder.getTableList((Statement) insertStatement.getSelect());
                                for (String table : tableList) {
                                    readTables.add(table.toUpperCase());
                                }
                            }
                        } else if (statement instanceof Update) {
                            Update updateStatement = (Update) statement;
                            writtenTables.add(updateStatement.getTable().getName().toUpperCase());
                            List<String> tableList = tablesNamesFinder.getTableList((Statement)updateStatement);
                            for (String table : tableList) {
                                if (!table.equalsIgnoreCase(updateStatement.getTable().getName())) {
                                    readTables.add(table.toUpperCase());
                                }
                            }
                        } else if (statement instanceof Delete) {
                            Delete deleteStatement = (Delete) statement;
                            writtenTables.add(deleteStatement.getTable().getName().toUpperCase());
                        } else if (statement instanceof CreateTable) {
                            CreateTable createTableStatement = (CreateTable) statement;
                            String tableName = createTableStatement.getTable().getName().toUpperCase();
                            createdTables.add(tableName);
                            writtenTables.add(tableName);
                        } else if (statement instanceof Drop) {
                            Drop dropStatement = (Drop) statement;
                            if ("TABLE".equalsIgnoreCase(dropStatement.getType())) {
                                droppedTables.add(dropStatement.getName().getName().toUpperCase());
                            }
                        }
                    } catch (JSQLParserException e) {
                        logger.error("Error parsing SQL command: " + command.getRawText(), e);
                    }
                }
            }

            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(script.getScriptName());
            metadata.setExecutionOrder(fileOrderMap.getOrDefault(script.getScriptName(), Integer.MAX_VALUE));
            metadata.setFileFormat("BTEQ");
            metadata.setFileSize(script.getSize());
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

        Graph chainFlowGraph = chainFlowGraphConverter.convert(scripts, fileOrderList, fileMetadataMap);
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

    /*
    @PostMapping("/api/visualize-select")
    public Graph visualizeSelect(@RequestParam("scriptName") String scriptName, @RequestParam("commandId") String commandId) {
        logger.info("Request to visualize select query for script: {}, commandId: {}", scriptName, commandId);

        BteqScript script = parsedScripts.get(scriptName);
        if (script == null) {
            logger.error("Script not found: {}", scriptName);
            return new Graph(); // Return empty graph
        }

        try {
            // commandId is "cmd-INDEX"
            int commandIndex = Integer.parseInt(commandId.split("-")[1]);
            BteqCommand command = script.getCommands().get(commandIndex);

            if (command instanceof BteqSqlCommand) {
                Object query = ((BteqSqlCommand) command).getQuery();
                SelectQuery selectQuery = null;

                if (query instanceof SelectQuery) {
                    selectQuery = (SelectQuery) query;
                } else if (query instanceof InsertQuery && ((InsertQuery) query).isSelect()) {
                    selectQuery = ((InsertQuery) query).getSelectQuery();
                } else if (query instanceof CreateTableQuery && ((CreateTableQuery) query).getSelectQuery() != null) {
                    selectQuery = ((CreateTableQuery) query).getSelectQuery();
                }

                if (selectQuery != null) {
                    SelectGraphConverter converter = new SelectGraphConverter();
                    return converter.convert(selectQuery);
                }
            }
        } catch (Exception e) {
            logger.error("Error generating select visualization for script: {}, commandId: {}", scriptName, commandId, e);
        }

        return new Graph(); // Return empty graph on error
    }
    */
}
