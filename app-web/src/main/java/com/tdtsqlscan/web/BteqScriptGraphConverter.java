package com.tdtsqlscan.web;

import com.tdtsqlscan.etl.BteqCommand;
import com.tdtsqlscan.etl.BteqScript;

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
                nodeLabel = "Configuration";
            } else if (command instanceof com.tdtsqlscan.etl.BteqControlCommand) {
                com.tdtsqlscan.etl.BteqControlCommand controlCommand = (com.tdtsqlscan.etl.BteqControlCommand) command;
                commandType = controlCommand.getType().toString();
                nodeLabel = "." + commandType;
            } else if (command instanceof com.tdtsqlscan.etl.BteqSqlCommand) {
                com.tdtsqlscan.etl.BteqSqlCommand sqlCommand = (com.tdtsqlscan.etl.BteqSqlCommand) command;
                if (sqlCommand.getQuery() != null) {
                    commandType = sqlCommand.getQuery().getType().toString();
                    nodeLabel = commandType.replace("_", " ");
                } else {
                    commandType = "SQL";
                    nodeLabel = "SQL";
                }
            } else {
                commandType = "UNKNOWN";
                nodeLabel = "UNKNOWN";
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
