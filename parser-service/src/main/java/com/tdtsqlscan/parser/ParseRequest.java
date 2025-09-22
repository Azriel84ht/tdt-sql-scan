package com.tdtsqlscan.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ParseRequest {

    @JsonProperty("script_content")
    @NotBlank
    @Size(max = 50000)
    private String scriptContent;

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }
}
