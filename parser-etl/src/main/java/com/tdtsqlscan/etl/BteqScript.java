package com.tdtsqlscan.etl;

import com.tdtsqlscan.core.SQLQuery;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Represents a BTEQ script, which is a collection of BTEQ commands.
 */
public class BteqScript {

    private String scriptName;
    private final List<BteqCommand> commands;
    private long size;
    private String encoding;


    public BteqScript() {
        this.commands = new ArrayList<>();
    }

    public BteqScript(String scriptName) {
        this.scriptName = scriptName;
        this.commands = new ArrayList<>();
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public void addCommand(BteqCommand command) {
        this.commands.add(command);
    }

    public List<BteqCommand> getCommands() {
        return commands;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getTransactions() {
        return (int) commands.stream()
                .filter(c -> c instanceof BteqSqlCommand)
                .count();
    }

    public List<String> getInputTables() {
        List<String> tables = new ArrayList<>();
        for (BteqCommand command : commands) {
            if (command instanceof BteqSqlCommand) {
                SQLQuery query = ((BteqSqlCommand) command).getQuery();
                if (query instanceof com.tdtsqlscan.select.SelectQuery) {
                    com.tdtsqlscan.select.SelectQuery q = (com.tdtsqlscan.select.SelectQuery) query;
                    q.getTables().forEach(t -> tables.add(t.getName()));
                    q.getJoins().forEach(j -> tables.add(j.getTable().getName()));
                } else if (query instanceof com.tdtsqlscan.dml.UpdateQuery) {
                    com.tdtsqlscan.dml.UpdateQuery q = (com.tdtsqlscan.dml.UpdateQuery) query;
                    tables.addAll(q.getSourceTables());
                } else if (query instanceof com.tdtsqlscan.dml.DeleteQuery) {
                    com.tdtsqlscan.dml.DeleteQuery q = (com.tdtsqlscan.dml.DeleteQuery) query;
                    tables.add(q.getTable());
                } else if (query instanceof com.tdtsqlscan.dml.InsertQuery) {
                    com.tdtsqlscan.dml.InsertQuery q = (com.tdtsqlscan.dml.InsertQuery) query;
                    if (q.getSourceTableName() != null) {
                        tables.add(q.getSourceTableName());
                    }
                } else if (query instanceof com.tdtsqlscan.ddl.CreateTableQuery) {
                    com.tdtsqlscan.ddl.CreateTableQuery q = (com.tdtsqlscan.ddl.CreateTableQuery) query;
                    tables.addAll(q.getSourceTables());
                }
            }
        }
        return tables.stream().distinct().sorted().collect(java.util.stream.Collectors.toList());
    }

    public List<String> getOutputTables() {
        List<String> createdTables = new ArrayList<>();
        List<String> droppedTables = new ArrayList<>();
        for (BteqCommand command : commands) {
            if (command instanceof BteqSqlCommand) {
                SQLQuery query = ((BteqSqlCommand) command).getQuery();
                if (query instanceof com.tdtsqlscan.ddl.CreateTableQuery) {
                    com.tdtsqlscan.ddl.CreateTableQuery q = (com.tdtsqlscan.ddl.CreateTableQuery) query;
                    if (!q.isVolatile()) {
                        createdTables.add(q.getTableName());
                    }
                } else if (query instanceof com.tdtsqlscan.ddl.DropTableQuery) {
                    com.tdtsqlscan.ddl.DropTableQuery q = (com.tdtsqlscan.ddl.DropTableQuery) query;
                    droppedTables.add(q.getTableName());
                } else if (query instanceof com.tdtsqlscan.dml.InsertQuery) {
                    com.tdtsqlscan.dml.InsertQuery q = (com.tdtsqlscan.dml.InsertQuery) query;
                    createdTables.add(q.getTableName());
                } else if (query instanceof com.tdtsqlscan.dml.UpdateQuery) {
                    com.tdtsqlscan.dml.UpdateQuery q = (com.tdtsqlscan.dml.UpdateQuery) query;
                    createdTables.add(q.getTargetTable());
                }
            }
        }
        createdTables.removeAll(droppedTables);
        return createdTables.stream().distinct().sorted().collect(java.util.stream.Collectors.toList());
    }
}
