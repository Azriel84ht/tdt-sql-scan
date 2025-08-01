package com.tdtsqlscan.web;

import com.tdtsqlscan.etl.BteqCommand;
import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.graph.Edge;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;

public class BteqScriptGraphConverter {

    public Graph convert(BteqScript script) {
        Graph graph = new Graph();
        Node previousNode = null;

        for (int i = 0; i < script.getCommands().size(); i++) {
            BteqCommand command = script.getCommands().get(i);
            String nodeId = "node-" + i;
            String nodeLabel;
            String commandType;

            if (command instanceof com.tdtsqlscan.etl.BteqConfigurationCommand) {
                commandType = "CONFIGURATION";
                nodeLabel = "Config";
            } else if (command instanceof com.tdtsqlscan.etl.BteqControlCommand) {
                com.tdtsqlscan.etl.BteqControlCommand controlCommand = (com.tdtsqlscan.etl.BteqControlCommand) command;
                commandType = controlCommand.getType().toString();
                nodeLabel = "." + commandType;
            } else if (command instanceof com.tdtsqlscan.etl.BteqSqlCommand) {
                com.tdtsqlscan.etl.BteqSqlCommand sqlCommand = (com.tdtsqlscan.etl.BteqSqlCommand) command;
                if (sqlCommand.getQuery() != null) {
                    commandType = sqlCommand.getQuery().getType().toString();
                    nodeLabel = commandType.replace("_", " ");
                    if (sqlCommand.getQuery() instanceof com.tdtsqlscan.ddl.CreateTableQuery) {
                        nodeLabel = "CREATE TABLE\n" + ((com.tdtsqlscan.ddl.CreateTableQuery) sqlCommand.getQuery()).getTableName();
                    } else if (sqlCommand.getQuery() instanceof com.tdtsqlscan.dml.InsertQuery) {
                        nodeLabel = "INSERT\n" + ((com.tdtsqlscan.dml.InsertQuery) sqlCommand.getQuery()).getTableName();
                    }
                } else {
                    commandType = "SQL";
                    nodeLabel = "SQL";
                }
            } else {
                commandType = "UNKNOWN";
                nodeLabel = "Unknown";
            }

            Node currentNode = new Node(nodeId, nodeLabel);
            currentNode.addProperty("fullText", command.getRawText());
            currentNode.addProperty("commandType", commandType);
            graph.addNode(currentNode);

            if (previousNode != null) {
                graph.addEdge(new Edge(previousNode.getId(), currentNode.getId(), ""));
            }
            previousNode = currentNode;
        }
        return graph;
    }
}
