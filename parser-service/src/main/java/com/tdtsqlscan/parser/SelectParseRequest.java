package com.tdtsqlscan.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class SelectParseRequest {

    @NotBlank(message = "The select_query field cannot be blank.")
    @JsonProperty("select_query")
    private String selectQuery;

    public String getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }
}
