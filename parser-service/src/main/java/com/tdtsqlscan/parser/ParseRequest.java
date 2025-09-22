package com.tdtsqlscan.parser;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ParseRequest {

    @JsonProperty("script_content")
    private String scriptContent;

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }
}
