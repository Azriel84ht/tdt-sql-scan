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
        int max_y_offset = 0;

        Map<Integer, List<Node>> nodesByOrder = scriptsByOrder.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<Node> nodes = new java.util.ArrayList<>();
                            int scriptCount = entry.getValue().size();
                            int yOffset = (scriptCount > 1) ? (scriptCount - 1) * y_gap / 2 : 0;

                            for (int i = 0; i < scriptCount; i++) {
                                BteqScript script = entry.getValue().get(i);
                                Node node = new Node(script.getScriptName(), script.getScriptName());
                                int x = entry.getKey() * 400;
                                int y = (i * y_gap) - yOffset + max_y_offset;
                                node.getProperties().put("x", String.valueOf(x));
                                node.getProperties().put("y", String.valueOf(y));
                                node.getProperties().put("shape", "image");
                                node.getProperties().put("image", "/images/bteq_script.png");
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
                            if (scriptCount > 1) {
                               // max_y_offset += (scriptCount -1) * y_gap;
                            }
                            return nodes;
                        }
                ));

        List<Integer> sortedOrders = nodesByOrder.keySet().stream().sorted().collect(Collectors.toList());

        for (int i = 0; i < sortedOrders.size() - 1; i++) {
            Integer currentOrder = sortedOrders.get(i);
            Integer nextOrder = sortedOrders.get(i + 1);
            List<Node> currentNodes = nodesByOrder.get(currentOrder);
            List<Node> nextNodes = nodesByOrder.get(nextOrder);

            for (Node fromNode : currentNodes) {
                for (Node toNode : nextNodes) {
                    // Create an arrow node
                    String arrowId = "arrow-" + UUID.randomUUID().toString();
                    Node arrowNode = new Node(arrowId, "");
                    int fromX = Integer.parseInt((String) fromNode.getProperties().get("x"));
                    int toX = Integer.parseInt((String) toNode.getProperties().get("x"));
                    int fromY = Integer.parseInt((String) fromNode.getProperties().get("y"));
                    arrowNode.getProperties().put("x", String.valueOf(fromX + (toX - fromX) / 2));
                    arrowNode.getProperties().put("y", String.valueOf(fromY));
                    arrowNode.getProperties().put("shape", "image");
                    arrowNode.getProperties().put("image", "/images/right_arrow.png");
                    arrowNode.getProperties().put("size", "30");
                    graph.addNode(arrowNode);

                    graph.addEdge(new Edge(fromNode.getId(), arrowNode.getId(), ""));
                    graph.addEdge(new Edge(arrowNode.getId(), toNode.getId(), ""));
                }
            }
        }

        return graph;
    }
}
