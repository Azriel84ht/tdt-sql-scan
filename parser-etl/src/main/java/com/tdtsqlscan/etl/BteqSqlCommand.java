package com.tdtsqlscan.etl;

import com.tdtsqlscan.core.SQLQuery;

public class BteqSqlCommand implements BteqCommand {

    private final String rawText;
    private final SQLQuery query;

    public BteqSqlCommand(String rawText, SQLQuery query) {
        this.rawText = rawText;
        this.query = query;
    }

    public SQLQuery getQuery() {
        return query;
    }

    @Override
    public String getRawText() {
        return rawText;
    }
}
