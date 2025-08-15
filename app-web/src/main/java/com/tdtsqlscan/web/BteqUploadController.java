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
import java.util.stream.Collectors;

@RestController
public class BteqUploadController {

    private static final Logger logger = LoggerFactory.getLogger(BteqUploadController.class);

    private final BteqScriptParser bteqScriptParser;
    private final DataFlowGraphConverter dataFlowGraphConverter;
    private final ChainFlowGraphConverter chainFlowGraphConverter;
    private final ObjectMapper objectMapper;

    public static class GraphResponse {
        public Graph chainFlow;
        public Graph bteqFlow;
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
        this.dataFlowGraphConverter = new DataFlowGraphConverter();
        this.chainFlowGraphConverter = new ChainFlowGraphConverter();
        this.objectMapper = new ObjectMapper();
        logger.info("BteqUploadController initialized");
    }

    @PostMapping("/upload")
    public GraphResponse handleFileUpload(@RequestParam("files") MultipartFile[] files, @RequestParam("fileOrder") String fileOrderJson) throws IOException {
        logger.info("Received {} files for upload", files.length);

        List<Map<String, String>> fileOrder = objectMapper.readValue(fileOrderJson, new TypeReference<List<Map<String, String>>>(){});

        List<BteqScript> scripts = new ArrayList<>();
        for (MultipartFile file : files) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            BteqScript script = bteqScriptParser.parse(content, file.getOriginalFilename());
            scripts.add(script);
        }

        Graph chainFlowGraph = chainFlowGraphConverter.convert(scripts, fileOrder);
        logger.info("Generated chain flow graph with {} nodes and {} edges", chainFlowGraph.getNodes().size(), chainFlowGraph.getEdges().size());

        BteqScript combinedScript = new BteqScript();
        // The order of combining scripts might matter, so we should sort them
        List<String> sortedFileNames = fileOrder.stream()
                .sorted((a, b) -> Integer.compare(Integer.parseInt(a.get("order")), Integer.parseInt(b.get("order"))))
                .map(map -> map.get("name"))
                .collect(Collectors.toList());

        for (String fileName : sortedFileNames) {
            scripts.stream()
                    .filter(s -> s.getScriptName().equals(fileName))
                    .findFirst()
                    .ifPresent(s -> s.getCommands().forEach(combinedScript::addCommand));
        }

        Graph bteqFlowGraph = dataFlowGraphConverter.convert(combinedScript);
        logger.info("Generated data flow graph with {} nodes and {} edges", bteqFlowGraph.getNodes().size(), bteqFlowGraph.getEdges().size());

        GraphResponse response = new GraphResponse();
        response.chainFlow = chainFlowGraph;
        response.bteqFlow = bteqFlowGraph;

        return response;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from BTEQ Flow Visualizer!";
    }
}
