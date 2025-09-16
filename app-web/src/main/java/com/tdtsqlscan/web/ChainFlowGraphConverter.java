package com.tdtsqlscan.web;

import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;
import com.tdtsqlscan.graph.Edge;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChainFlowGraphConverter {

    public Graph convert(List<BteqScript> scripts, List<Map<String, String>> fileOrder) {
        Graph graph = new Graph();
        Map<String, Integer> orderByName = fileOrder.stream()
                .collect(Collectors.toMap(
                        map -> map.get("name"),
                        map -> Integer.parseInt(map.get("order"))
                ));

        Map<String, BteqScript> scriptsByName = scripts.stream()
                .collect(Collectors.toMap(BteqScript::getScriptName, script -> script));

        Map<Integer, List<BteqScript>> scriptsByOrder = scripts.stream()
                .collect(Collectors.groupingBy(script -> orderByName.get(script.getScriptName())));

        int y_gap = 150;

        Map<Integer, List<Node>> nodesByOrder = new java.util.LinkedHashMap<>();
        List<Integer> sortedKeys = new java.util.ArrayList<>(scriptsByOrder.keySet());
        java.util.Collections.sort(sortedKeys);

        for (Integer order : sortedKeys) {
            List<BteqScript> scriptsInOrder = scriptsByOrder.get(order);
            List<Node> nodes = new java.util.ArrayList<>();
            int scriptCount = scriptsInOrder.size();
            int yOffset = (scriptCount > 1) ? (scriptCount - 1) * y_gap / 2 : 0;

            for (int i = 0; i < scriptCount; i++) {
                BteqScript script = scriptsInOrder.get(i);
                Node node = new Node(script.getScriptName(), script.getScriptName());
                int x = order * 400;
                int y = (i * y_gap) - yOffset;
                node.getProperties().put("x", x);
                node.getProperties().put("y", y);
                node.getProperties().put("shape", "image");
                node.getProperties().put("image", "images/bteq_script.png");
                node.getProperties().put("size", "50");

                BteqScript originalScript = scriptsByName.get(script.getScriptName());
                if (originalScript != null) {
                    node.getProperties().put("fileName", originalScript.getScriptName());
                    node.getProperties().put("fileSize", String.valueOf(originalScript.getSize()));
                    node.getProperties().put("fileEncoding", originalScript.getEncoding());
                }
                nodes.add(node);
                graph.addNode(node);
            }
            nodesByOrder.put(order, nodes);
        }

        List<Integer> sortedOrders = nodesByOrder.keySet().stream().sorted().collect(Collectors.toList());

        for (int i = 0; i < sortedOrders.size() - 1; i++) {
            Integer currentOrder = sortedOrders.get(i);
            Integer nextOrder = sortedOrders.get(i + 1);
            List<Node> currentNodes = nodesByOrder.get(currentOrder);
            List<Node> nextNodes = nodesByOrder.get(nextOrder);

            for (Node fromNode : currentNodes) {
                for (Node toNode : nextNodes) {
                    graph.addEdge(new Edge(fromNode.getId(), toNode.getId(), ""));
                }
            }
        }

        return graph;
    }
}
