package com.tdtsqlscan.web;

import com.tdtsqlscan.ddl.CreateTableQuery;
import com.tdtsqlscan.dml.InsertQuery;
import com.tdtsqlscan.etl.*;
import com.tdtsqlscan.graph.Edge;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;

import java.util.HashMap;
import java.util.Map;

public class DataFlowGraphConverter {

    public Graph convert(BteqScript script) {
        Graph graph = new Graph();
        Map<String, Node> tableNodes = new HashMap<>();
        Node lastCommandNode = null;

        for (int i = 0; i < script.getCommands().size(); i++) {
            BteqCommand command = script.getCommands().get(i);
            String commandNodeId = "cmd-" + i;
            Node commandNode = createCommandNode(command, commandNodeId);
            graph.addNode(commandNode);

            // Add edge for sequential logic flow
            if (lastCommandNode != null) {
                Edge logicEdge = new Edge(lastCommandNode.getId(), commandNode.getId(), "");
                logicEdge.addProperty("dashes", true);
                logicEdge.addProperty("arrows", "to");
                graph.addEdge(logicEdge);
            }
            lastCommandNode = commandNode;

            // Handle data flow
            if (command instanceof BteqSqlCommand) {
                Object query = ((BteqSqlCommand) command).getQuery();
                if (query instanceof CreateTableQuery) {
                    handleCreateTable(graph, tableNodes, commandNode, (CreateTableQuery) query);
                } else if (query instanceof InsertQuery) {
                    handleInsert(graph, tableNodes, commandNode, (InsertQuery) query);
                }
            }
        }

        return graph;
    }

    private Node createCommandNode(BteqCommand command, String id) {
        String label;
        String shape = "box";

        if (command instanceof BteqConfigurationCommand) {
            label = "CONFIG";
            shape = "ellipse";
        } else if (command instanceof BteqControlCommand) {
            label = "." + ((BteqControlCommand) command).getType().toString();
            shape = "ellipse";
        } else if (command instanceof BteqSqlCommand) {
            Object query = ((BteqSqlCommand) command).getQuery();
            if (query instanceof CreateTableQuery) {
                label = "CREATE TABLE";
            } else if (query instanceof InsertQuery) {
                label = "INSERT";
            } else {
                label = "SQL";
            }
        } else {
            label = "UNKNOWN";
        }

        Node node = new Node(id, label);
        node.addProperty("shape", shape);
        node.addProperty("fullText", command.getRawText());
        return node;
    }

    private void handleCreateTable(Graph graph, Map<String, Node> tableNodes, Node commandNode, CreateTableQuery query) {
        String tableName = query.getTableName();
        if (tableName == null) return;

        Node tableNode = tableNodes.computeIfAbsent(tableName, k -> {
            Node newNode = new Node(k, k);
            newNode.addProperty("shape", "database");
            graph.addNode(newNode);
            return newNode;
        });

        Edge edge = new Edge(commandNode.getId(), tableNode.getId(), "creates");
        edge.addProperty("arrows", "to");
        graph.addEdge(edge);
    }

    private void handleInsert(Graph graph, Map<String, Node> tableNodes, Node commandNode, InsertQuery query) {
        String targetTable = query.getTableName();
        if (targetTable == null) return;

        Node targetNode = tableNodes.computeIfAbsent(targetTable, k -> {
            Node newNode = new Node(k, k);
            newNode.addProperty("shape", "database");
            graph.addNode(newNode);
            return newNode;
        });

        Edge toEdge = new Edge(commandNode.getId(), targetNode.getId(), "inserts into");
        toEdge.addProperty("arrows", "to");
        graph.addEdge(toEdge);

        String sourceTable = query.getSourceTableName();
        if (sourceTable != null) {
            Node sourceNode = tableNodes.computeIfAbsent(sourceTable, k -> {
                Node newNode = new Node(k, k);
                newNode.addProperty("shape", "database");
                graph.addNode(newNode);
                return newNode;
            });
            Edge fromEdge = new Edge(sourceNode.getId(), commandNode.getId(), "reads from");
            fromEdge.addProperty("arrows", "to");
            graph.addEdge(fromEdge);
        }
    }
}
