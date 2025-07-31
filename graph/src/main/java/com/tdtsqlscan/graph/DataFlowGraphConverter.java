package com.tdtsqlscan.graph;

import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.etl.BteqSqlCommand;
import com.tdtsqlscan.ddl.CreateTableQuery;
import com.tdtsqlscan.dml.InsertQuery;

import java.util.HashMap;
import java.util.Map;

public class DataFlowGraphConverter {

    public Graph convert(BteqScript script) {
        Graph graph = new Graph();
        Map<String, Node> tableNodes = new HashMap<>();

        script.getCommands().forEach(command -> {
            if (command instanceof BteqSqlCommand) {
                BteqSqlCommand sqlCommand = (BteqSqlCommand) command;
                if (sqlCommand.getQuery() instanceof CreateTableQuery) {
                    CreateTableQuery createTableQuery = (CreateTableQuery) sqlCommand.getQuery();
                    String tableName = createTableQuery.getTableName();
                    if (!tableNodes.containsKey(tableName)) {
                        Node node = new Node(tableName, tableName);
                        node.addProperty("commandType", "TABLE");
                        tableNodes.put(tableName, node);
                        graph.addNode(node);
                    }
                } else if (sqlCommand.getQuery() instanceof InsertQuery) {
                    InsertQuery insertQuery = (InsertQuery) sqlCommand.getQuery();
                    String tableName = insertQuery.getTableName();
                    if (!tableNodes.containsKey(tableName)) {
                        Node node = new Node(tableName, tableName);
                        node.addProperty("commandType", "TABLE");
                        tableNodes.put(tableName, node);
                        graph.addNode(node);
                    }
                }
            }
        });

        return graph;
    }
}
