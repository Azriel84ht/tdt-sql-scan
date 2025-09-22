package com.tdtsqlscan.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.web.client.ParserServiceClient;
import com.tdtsqlscan.web.client.dto.ParseResultDto;
import com.tdtsqlscan.web.client.dto.StatementDto;
import com.tdtsqlscan.web.client.dto.StatementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class BteqUploadController {

    private static final Logger logger = LoggerFactory.getLogger(BteqUploadController.class);

    private final ParserServiceClient parserServiceClient;
    private final ObjectMapper objectMapper;

    // We need to store the parsed results for later use, e.g., for visualizing SELECTs.
    // The key will be the script name.
    private final Map<String, ParseResultDto> parsedScriptsCache = new HashMap<>();


    public static class GraphResponse {
        public Graph chainFlow;
        public Map<String, Graph> bteqFlows;
        public Map<String, FileMetadata> fileMetadata;
    }

    // Helper class to hold script data along with its original name and order
    public static class ScriptData {
        public final String name;
        public final long size;
        public final String encoding;
        public final ParseResultDto parseResult;

        public ScriptData(String name, long size, String encoding, ParseResultDto parseResult) {
            this.name = name;
            this.size = size;
            this.encoding = encoding;
            this.parseResult = parseResult;
        }
    }


    public BteqUploadController(ParserServiceClient parserServiceClient, ObjectMapper objectMapper) {
        this.parserServiceClient = parserServiceClient;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/upload")
    public GraphResponse handleFileUpload(@RequestParam("files") MultipartFile[] files, @RequestParam("fileOrder") String fileOrderJson) throws IOException {
        logger.info("Received {} files for upload", files.length);
        parsedScriptsCache.clear();

        List<Map<String, String>> fileOrderList = objectMapper.readValue(fileOrderJson, new TypeReference<List<Map<String, String>>>() {});
        Map<String, Integer> fileOrderMap = fileOrderList.stream()
                .collect(Collectors.toMap(map -> map.get("name"), map -> Integer.parseInt(map.get("order"))));

        List<ScriptData> scripts = new ArrayList<>();
        for (MultipartFile file : files) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            ParseResultDto parseResult = parserServiceClient.parse(content);
            ScriptData scriptData = new ScriptData(file.getOriginalFilename(), file.getSize(), StandardCharsets.UTF_8.name(), parseResult);
            scripts.add(scriptData);
            parsedScriptsCache.put(scriptData.name, parseResult);
        }

        scripts.sort(Comparator.comparing(s -> fileOrderMap.getOrDefault(s.name, Integer.MAX_VALUE)));

        ChainFlowGraphConverter chainFlowGraphConverter = new ChainFlowGraphConverter();
        Graph chainFlowGraph = chainFlowGraphConverter.convert(scripts, fileOrderList);

        Map<String, Graph> bteqFlows = new LinkedHashMap<>();
        for (ScriptData script : scripts) {
            DataFlowGraphConverter dataFlowGraphConverter = new DataFlowGraphConverter();
            Graph bteqFlowGraph = dataFlowGraphConverter.convert(script.parseResult);
            bteqFlows.put(script.name, bteqFlowGraph);
        }

        Map<String, FileMetadata> fileMetadataMap = new LinkedHashMap<>();
        for (ScriptData script : scripts) {
            fileMetadataMap.put(script.name, extractFileMetadata(script.parseResult));
        }

        GraphResponse response = new GraphResponse();
        response.chainFlow = chainFlowGraph;
        response.bteqFlows = bteqFlows;
        response.fileMetadata = fileMetadataMap;

        return response;
    }

    private FileMetadata extractFileMetadata(ParseResultDto parseResult) {
        Set<String> createdTables = new HashSet<>();
        Set<String> droppedTables = new HashSet<>();
        Set<String> readTables = new HashSet<>();
        Set<String> writtenTables = new HashSet<>();
        int transactionCount = 0;

        for (StatementDto stmt : parseResult.getStatements()) {
            if (stmt.getType() == StatementType.SQL_DDL || stmt.getType() == StatementType.SQL_DML) {
                transactionCount++;
                Map<String, String> details = stmt.getDetails();
                if (details == null) continue;

                switch (stmt.getCommandName().toUpperCase()) {
                    case "CREATE TABLE":
                        String createdTable = details.get("table_name");
                        if (createdTable != null) {
                            createdTables.add(createdTable.toUpperCase());
                            writtenTables.add(createdTable.toUpperCase());
                        }
                        // If CREATE TABLE AS SELECT, it also reads from tables
                        if (details.containsKey("source_tables")) {
                            String sourceTables = details.get("source_tables");
                            for(String tbl : sourceTables.split(",")) {
                                readTables.add(tbl.trim().toUpperCase());
                            }
                        }
                        break;
                    case "DROP TABLE":
                        String droppedTable = details.get("table_name");
                        if (droppedTable != null) {
                            droppedTables.add(droppedTable.toUpperCase());
                        }
                        break;
                    case "INSERT":
                        String targetTable = details.get("table_name");
                        if (targetTable != null) {
                            writtenTables.add(targetTable.toUpperCase());
                        }
                        if (details.containsKey("source_tables")) {
                            String sourceTables = details.get("source_tables");
                             for(String tbl : sourceTables.split(",")) {
                                readTables.add(tbl.trim().toUpperCase());
                            }
                        }
                        break;
                    case "UPDATE":
                        String updatedTable = details.get("table_name");
                        if(updatedTable != null) {
                            writtenTables.add(updatedTable.toUpperCase());
                        }
                        if (details.containsKey("source_tables")) {
                            String sourceTables = details.get("source_tables");
                             for(String tbl : sourceTables.split(",")) {
                                readTables.add(tbl.trim().toUpperCase());
                            }
                        }
                        break;
                    case "DELETE":
                         String deletedFromTable = details.get("table_name");
                         if(deletedFromTable != null) {
                            writtenTables.add(deletedFromTable.toUpperCase());
                         }
                        break;
                    case "SELECT":
                         if (details.containsKey("source_tables")) {
                            String sourceTables = details.get("source_tables");
                             for(String tbl : sourceTables.split(",")) {
                                readTables.add(tbl.trim().toUpperCase());
                            }
                        }
                        break;
                }
            }
        }

        FileMetadata metadata = new FileMetadata();
        metadata.setTransactions(transactionCount);

        Set<String> finalInputTables = new HashSet<>(readTables);
        finalInputTables.removeAll(createdTables);

        Set<String> finalOutputTables = new HashSet<>(writtenTables);
        finalOutputTables.removeAll(droppedTables);

        finalInputTables.forEach(metadata::addInputTable);
        finalOutputTables.forEach(metadata::addOutputTable);

        return metadata;
    }

    // This endpoint is now broken because it relies on BteqScript and specific command indexing.
    // It will need to be refactored or removed. For now, it will return an empty graph.
    // TODO: Refactor visualize-select to work with ParseResultDto
    @PostMapping("/api/visualize-select")
    public Graph visualizeSelect(@RequestParam("scriptName") String scriptName, @RequestParam("commandId") String commandId) {
        logger.warn("The visualize-select endpoint is temporarily disabled pending refactoring.");
        return new Graph();
    }
}
