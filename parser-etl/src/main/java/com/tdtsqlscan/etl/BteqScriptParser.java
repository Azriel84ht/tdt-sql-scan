package com.tdtsqlscan.etl;

import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.core.SQLQuery;
import java.util.ArrayList;
import java.util.List;

public class BteqScriptParser {

    private final List<QueryParser> sqlParsers;

    public BteqScriptParser(List<QueryParser> sqlParsers) {
        this.sqlParsers = sqlParsers;
    }

    public BteqScript parse(String bteqScript) {
        String scriptWithoutComments = bteqScript.replaceAll("--.*|/\\*(?s:.*?)\\*/", "");
        BteqScript script = new BteqScript();
        StringBuilder sqlBuffer = new StringBuilder();
        boolean inSql = false;

        List<BteqCommand> configCommands = new ArrayList<>();

        String[] lines = scriptWithoutComments.split("\\r?\\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            if (trimmedLine.startsWith(".SET") || trimmedLine.startsWith(".LOGON")) {
                configCommands.add(parseBteqControlCommand(trimmedLine));
                continue;
            }

            if (!configCommands.isEmpty()) {
                script.addCommand(new BteqConfigurationCommand(configCommands));
                configCommands = new ArrayList<>();
            }

            if (inSql) {
                sqlBuffer.append(" ").append(trimmedLine);
                if (trimmedLine.endsWith(";")) {
                    String sql = sqlBuffer.toString();
                    sql = sql.substring(0, sql.length() - 1).trim(); // Remove semicolon
                    SQLQuery query = parseSql(sql);
                    script.addCommand(new BteqSqlCommand(sql, query));
                    sqlBuffer.setLength(0);
                    inSql = false;
                }
            } else {
                if (trimmedLine.startsWith(".")) {
                    script.addCommand(parseBteqControlCommand(trimmedLine));
                } else {
                    sqlBuffer.append(trimmedLine);
                    if (trimmedLine.endsWith(";")) {
                        String sql = sqlBuffer.toString();
                        sql = sql.substring(0, sql.length() - 1).trim(); // Remove semicolon
                        SQLQuery query = parseSql(sql);
                        script.addCommand(new BteqSqlCommand(sql, query));
                        sqlBuffer.setLength(0);
                    } else {
                        inSql = true;
                    }
                }
            }
        }

        if (!configCommands.isEmpty()) {
            script.addCommand(new BteqConfigurationCommand(configCommands));
        }

        return script;
    }

    private SQLQuery parseSql(String sql) {
        for (QueryParser parser : sqlParsers) {
            if (parser.supports(sql)) {
                try {
                    return parser.parse(sql);
                } catch (Exception e) {
                    // For now, just return null if parsing fails.
                    // In a real application, we would want to log this.
                    return null;
                }
            }
        }
        return null;
    }

    private BteqControlCommand parseBteqControlCommand(String line) {
        // Simple parsing logic. This will need to be more robust.
        String commandWithArgs = line.substring(1);
        String[] parts = commandWithArgs.split("\\s+", 2);
        String commandName = parts[0].toUpperCase();
        if (commandName.endsWith(";")) {
            commandName = commandName.substring(0, commandName.length() - 1);
        }
        BteqCommandType type;
        try {
            type = BteqCommandType.valueOf(commandName);
        } catch (IllegalArgumentException e) {
            type = BteqCommandType.OTHER;
        }
        return new BteqControlCommand(type, line);
    }
}
