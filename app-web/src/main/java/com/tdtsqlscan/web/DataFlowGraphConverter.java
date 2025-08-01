package com.tdtsqlscan.web;

import com.tdtsqlscan.etl.BteqCommand;
import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.etl.BteqSqlCommand;
import com.tdtsqlscan.ddl.CreateTableQuery;
import com.tdtsqlscan.dml.InsertQuery;
import com.tdtsqlscan.graph.Edge;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                        Node node = new Node(tableName, "CREATE TABLE\n" + tableName);
                        node.addProperty("commandType", "CREATE_TABLE");
                        node.addProperty("fullText", sqlCommand.getRawText());
                        tableNodes.put(tableName, node);
                        graph.addNode(node);
                    }
                } else if (sqlCommand.getQuery() instanceof InsertQuery) {
                    InsertQuery insertQuery = (InsertQuery) sqlCommand.getQuery();
                    String tableName = insertQuery.getTableName();
                    Node tableNode = tableNodes.get(tableName);
                    if (tableNode != null) {
                        String insertId = "insert-" + tableName + "-" + graph.getNodes().size();
                        Node insertNode = new Node(insertId, "INSERT\n" + tableName);
                        insertNode.addProperty("commandType", "INSERT");
                        insertNode.addProperty("fullText", sqlCommand.getRawText());
                        graph.addNode(insertNode);
                        graph.addEdge(new Edge(tableNode.getId(), insertId, ""));
                    }
                }
            }
        });

        return graph;
    }
}
