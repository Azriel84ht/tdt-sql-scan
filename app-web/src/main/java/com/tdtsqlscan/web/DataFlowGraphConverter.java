package com.tdtsqlscan.web;

import com.tdtsqlscan.ddl.CreateTableQuery;
import com.tdtsqlscan.dml.InsertQuery;
import com.tdtsqlscan.etl.BteqCommand;
import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.etl.BteqSqlCommand;
import com.tdtsqlscan.graph.Edge;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;

import java.util.HashMap;
import java.util.Map;

public class DataFlowGraphConverter {

    public Graph convert(BteqScript script) {
        Graph graph = new Graph();
        Map<String, Node> tableNodes = new HashMap<>();

        for (BteqCommand command : script.getCommands()) {
            if (command instanceof BteqSqlCommand) {
                BteqSqlCommand sqlCommand = (BteqSqlCommand) command;

                if (sqlCommand.getQuery() instanceof CreateTableQuery) {
                    CreateTableQuery createTableQuery = (CreateTableQuery) sqlCommand.getQuery();
                    String tableName = createTableQuery.getTableName();
                    if (tableName != null && !tableNodes.containsKey(tableName)) {
                        Node node = new Node(tableName, tableName);
                        node.addProperty("type", "TABLE");
                        tableNodes.put(tableName, node);
                        graph.addNode(node);
                    }
                } else if (sqlCommand.getQuery() instanceof InsertQuery) {
                    InsertQuery insertQuery = (InsertQuery) sqlCommand.getQuery();
                    String targetTable = insertQuery.getTableName();
                    String sourceTable = insertQuery.getSourceTableName();

                    if (targetTable != null) {
                        if (!tableNodes.containsKey(targetTable)) {
                            Node node = new Node(targetTable, targetTable);
                            node.addProperty("type", "TABLE");
                            tableNodes.put(targetTable, node);
                            graph.addNode(node);
                        }
                    }

                    if (sourceTable != null) {
                        if (!tableNodes.containsKey(sourceTable)) {
                            Node node = new Node(sourceTable, sourceTable);
                            node.addProperty("type", "TABLE");
                            tableNodes.put(sourceTable, node);
                            graph.addNode(node);
                        }
                    }

                    if (sourceTable != null && targetTable != null) {
                        graph.addEdge(new Edge(sourceTable, targetTable, ""));
                    }
                }
            }
        }

        return graph;
    }
}
