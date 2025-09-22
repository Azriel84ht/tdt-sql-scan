package com.tdtsqlscan.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parse")
public class PocParserController {

    private final AntlrBteqParserService parserService;

    @Autowired
    public PocParserController(AntlrBteqParserService parserService) {
        this.parserService = parserService;
    }

    @PostMapping("/poc")
    public ResponseEntity<List<String>> parseScript(@RequestBody ParseRequest request) {
        if (request == null || request.getScriptContent() == null || request.getScriptContent().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<String> commands = parserService.parse(request.getScriptContent());
        return ResponseEntity.ok(commands);
    }
}
