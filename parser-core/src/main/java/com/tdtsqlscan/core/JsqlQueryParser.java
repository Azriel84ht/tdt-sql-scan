package com.tdtsqlscan.core;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class JsqlQueryParser implements QueryParser {

    @Override
    public boolean supports(String sql) {
        return true;
    }

    @Override
    public SQLQuery parse(String sql) throws SQLParseException {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            return JsqlAstConverter.convert(statement, sql);
        } catch (Exception e) {
            throw new SQLParseException("Error parsing SQL statement: " + sql, e);
        }
    }
}
