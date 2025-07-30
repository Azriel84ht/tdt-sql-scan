package com.tdtsqlscan.web;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.ddl.CreateIndexParser;
import com.tdtsqlscan.ddl.CreateTableParser;
import com.tdtsqlscan.dml.DeleteParser;
import com.tdtsqlscan.dml.InsertParser;
import com.tdtsqlscan.dml.UpdateParser;
import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.etl.BteqScriptParser;
import com.tdtsqlscan.graph.BteqScriptGraphConverter;
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

@RestController
public class BteqUploadController {

    private static final Logger logger = LoggerFactory.getLogger(BteqUploadController.class);

    public BteqUploadController() {
        logger.info("Initializing BteqUploadController");
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("files") MultipartFile[] files) throws IOException {
        logger.info("Received {} files for upload", files.length);
        return "{\"status\": \"ok\"}";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from BTEQ Flow Visualizer!";
    }
}
