package com.tdtsqlscan.graph;

import com.tdtsqlscan.etl.BteqCommand;
import com.tdtsqlscan.etl.BteqScript;

public class BteqScriptGraphConverter {

    public Graph convert(BteqScript script) {
        Graph graph = new Graph();
        Node previousNode = null;

        for (int i = 0; i < script.getCommands().size(); i++) {
            BteqCommand command = script.getCommands().get(i);
            String nodeId = "node-" + i;
            String nodeLabel = command.getRawText();

            Node currentNode = new Node(nodeId, nodeLabel);
            graph.addNode(currentNode);

            if (previousNode != null) {
                graph.addEdge(new Edge(previousNode.getId(), currentNode.getId(), ""));
            }
            previousNode = currentNode;
        }
        return graph;
    }
}
