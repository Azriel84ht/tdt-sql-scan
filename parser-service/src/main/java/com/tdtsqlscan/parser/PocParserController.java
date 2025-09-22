package com.tdtsqlscan.parser;

import com.tdtsqlscan.parser.dto.ParseResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parse")
@Tag(name = "Parser POC", description = "Endpoints para el Proof of Concept del servicio de parsing")
public class PocParserController {

    private final AntlrBteqParserService parserService;

    @Autowired
    public PocParserController(AntlrBteqParserService parserService) {
        this.parserService = parserService;
    }

    @PostMapping(value = "/poc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Analiza un script BTEQ",
            description = "Recibe un script BTEQ en formato de texto plano dentro de un objeto JSON, lo procesa y devuelve una estructura detallada de las sentencias encontradas."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Objeto JSON que contiene el script BTEQ a analizar.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ParseRequest.class)
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Análisis exitoso.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ParseResultDto.class)
            )
    )
    public ResponseEntity<ParseResultDto> parseScript(@RequestBody ParseRequest request) {
        if (request == null || request.getScriptContent() == null || request.getScriptContent().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ParseResultDto result = parserService.parse(request.getScriptContent());
        return ResponseEntity.ok(result);
    }
}
