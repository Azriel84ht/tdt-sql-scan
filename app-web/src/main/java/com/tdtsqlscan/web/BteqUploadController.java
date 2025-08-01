package com.tdtsqlscan.web;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.ddl.CreateIndexParser;
import com.tdtsqlscan.ddl.CreateTableParser;
import com.tdtsqlscan.dml.DeleteParser;
import com.tdtsqlscan.dml.InsertParser;
import com.tdtsqlscan.dml.UpdateParser;
import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.etl.BteqScriptParser;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.select.SelectParser;
import com.tdtsqlscan.web.BteqScriptGraphConverter;
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

@RestController
public class BteqUploadController {

    private static final Logger logger = LoggerFactory.getLogger(BteqUploadController.class);

    private final BteqScriptParser bteqScriptParser;
    private final BteqScriptGraphConverter bteqScriptGraphConverter;
    private final DataFlowGraphConverter dataFlowGraphConverter;

    public BteqUploadController() {
        logger.info("Initializing BteqUploadController");
        List<QueryParser> sqlParsers = new ArrayList<>();
        sqlParsers.add(new SelectParser());
        sqlParsers.add(new CreateTableParser());
        sqlParsers.add(new CreateIndexParser());
        sqlParsers.add(new InsertParser());
        sqlParsers.add(new UpdateParser());
        sqlParsers.add(new DeleteParser());
        this.bteqScriptParser = new BteqScriptParser(sqlParsers);
        this.bteqScriptGraphConverter = new BteqScriptGraphConverter();
        this.dataFlowGraphConverter = new DataFlowGraphConverter();
        logger.info("BteqUploadController initialized");
    }

    @PostMapping("/upload")
    public Graph handleFileUpload(@RequestParam("files") MultipartFile[] files) throws IOException {
        logger.info("Received {} files for upload", files.length);
        BteqScript combinedScript = new BteqScript();
        for (MultipartFile file : files) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            BteqScript script = bteqScriptParser.parse(content);
            script.getCommands().forEach(combinedScript::addCommand);
        }
        Graph graph = dataFlowGraphConverter.convert(combinedScript);
        logger.info("Generated data flow graph with {} nodes and {} edges", graph.getNodes().size(), graph.getEdges().size());
        return graph;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from BTEQ Flow Visualizer!";
    }
}
