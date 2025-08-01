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
        Map<String, List<Node>> tableNodeGroups = new HashMap<>();
        int x = 0;

        for (BteqCommand command : script.getCommands()) {
            if (command instanceof BteqSqlCommand) {
                BteqSqlCommand sqlCommand = (BteqSqlCommand) command;
                String tableName = null;
                String commandType = null;
                String label = null;

                if (sqlCommand.getQuery() instanceof CreateTableQuery) {
                    CreateTableQuery createTableQuery = (CreateTableQuery) sqlCommand.getQuery();
                    tableName = createTableQuery.getTableName();
                    commandType = "CREATE_TABLE";
                    label = "CREATE TABLE\n" + tableName;
                } else if (sqlCommand.getQuery() instanceof InsertQuery) {
                    InsertQuery insertQuery = (InsertQuery) sqlCommand.getQuery();
                    tableName = insertQuery.getTableName();
                    commandType = "INSERT";
                    label = "INSERT\n" + tableName;
                }

                if (tableName != null) {
                    tableNodeGroups.putIfAbsent(tableName, new ArrayList<>());
                    String nodeId = commandType + "-" + tableName + "-" + tableNodeGroups.get(tableName).size();
                    Node node = new Node(nodeId, label);
                    node.addProperty("commandType", commandType);
                    node.addProperty("fullText", sqlCommand.getRawText());
                    int labelWidth = label.length() * 8;
                    node.addProperty("width", labelWidth);
                    tableNodeGroups.get(tableName).add(node);
                }
            }
        }

        int y = 0;
        Map<String, Node> lastTableNode = new HashMap<>();
        for (Map.Entry<String, List<Node>> entry : tableNodeGroups.entrySet()) {
            x = 0;
            Node previousNode = null;
            for (Node node : entry.getValue()) {
                node.addProperty("x", x + (int)node.getProperties().get("width") / 2);
                node.addProperty("y", y);
                graph.addNode(node);

                if (node.getProperties().get("commandType").equals("INSERT")) {
                    BteqSqlCommand sqlCommand = (BteqSqlCommand) script.getCommands().stream()
                            .filter(c -> c instanceof BteqSqlCommand && ((BteqSqlCommand) c).getRawText().equals(node.getProperties().get("fullText")))
                            .findFirst().get();
                    InsertQuery insertQuery = (InsertQuery) sqlCommand.getQuery();
                    if (insertQuery.getSourceTableName() != null && lastTableNode.containsKey(insertQuery.getSourceTableName())) {
                        graph.addEdge(new Edge(lastTableNode.get(insertQuery.getSourceTableName()).getId(), node.getId(), ""));
                    }
                }

                if (previousNode != null) {
                    graph.addEdge(new Edge(previousNode.getId(), node.getId(), ""));
                }
                previousNode = node;
                lastTableNode.put(entry.getKey(), node);
                x += (int)node.getProperties().get("width") + 100; // 100 is the margin
            }
            y += 200; // Increased vertical spacing
        }


        return graph;
    }
}
