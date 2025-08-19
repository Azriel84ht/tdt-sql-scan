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
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.select.SelectParser;
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
import java.util.LinkedHashMap;
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
        public Map<String, Map<String, Object>> fileMetadata;
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

        Map<String, Map<String, Object>> fileMetadata = new LinkedHashMap<>();
        for (BteqScript script : scripts) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("transactions", script.getTransactions());
            metadata.put("inputTables", script.getInputTables());
            metadata.put("outputTables", script.getOutputTables());
            fileMetadata.put(script.getScriptName(), metadata);
        }

        GraphResponse response = new GraphResponse();
        response.chainFlow = chainFlowGraph;
        response.bteqFlows = bteqFlows;
        response.fileMetadata = fileMetadata;

        return response;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from BTEQ Flow Visualizer!";
    }
}
