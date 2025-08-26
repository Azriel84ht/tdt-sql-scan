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

    public BteqScript parse(String bteqScript, String scriptName) {
        String scriptWithoutComments = bteqScript.replaceAll("--.*|/\\*(?s:.*?)\\*/", "");
        BteqScript script = new BteqScript(scriptName);
        StringBuilder sqlBuffer = new StringBuilder();
        boolean inSql = false;
        boolean initialConfigBlockProcessed = false;
        List<BteqCommand> configCommands = new ArrayList<>();

        String[] lines = scriptWithoutComments.split("\\r?\\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            // Group initial config commands
            if (!initialConfigBlockProcessed &&
                (trimmedLine.startsWith(".SET") || trimmedLine.startsWith(".LOGON") || trimmedLine.startsWith(".DECLARE")
                    || trimmedLine.startsWith(".DATABASE"))) {
                configCommands.add(parseBteqControlCommand(trimmedLine));
                continue;
            }

            // End of initial config block. Dump collected commands.
            if (!configCommands.isEmpty()) {
                script.addCommand(new BteqConfigurationCommand(new ArrayList<>(configCommands)));
                configCommands.clear();
                initialConfigBlockProcessed = true;
            }

            // Process current line
            if (trimmedLine.startsWith(".")) {
                if(inSql) { // If we encounter a BTEQ command, the previous SQL buffer is implicitly ended
                    String sql = sqlBuffer.toString().trim();
                    if (!sql.isEmpty()) {
                        SQLQuery query = parseSql(sql);
                        script.addCommand(new BteqSqlCommand(sql, query));
                    }
                    sqlBuffer.setLength(0);
                    inSql = false;
                }
                script.addCommand(parseBteqControlCommand(trimmedLine));
                continue;
            }

            // Append the line to the current SQL buffer
            if (sqlBuffer.length() > 0) {
                sqlBuffer.append(" ");
            }
            sqlBuffer.append(trimmedLine);
            inSql = true;

            // Check if the buffer now ends with a terminator
            String currentBuffer = sqlBuffer.toString();
            if (currentBuffer.endsWith(";") || currentBuffer.endsWith(".")) {
                String sql = currentBuffer.substring(0, currentBuffer.length() - 1).trim();
                if (!sql.isEmpty()) {
                    SQLQuery query = parseSql(sql);
                    script.addCommand(new BteqSqlCommand(sql, query));
                }
                sqlBuffer.setLength(0);
                inSql = false;
            }
        }

        // Dump any remaining config commands if the script ends with them
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
