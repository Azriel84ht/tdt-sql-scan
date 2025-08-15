package com.tdtsqlscan.web;

import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;
import com.tdtsqlscan.graph.Edge;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChainFlowGraphConverter {

    public Graph convert(List<BteqScript> scripts, List<Map<String, String>> fileOrder) {
        Graph graph = new Graph();
        Map<String, Integer> orderByName = fileOrder.stream()
                .collect(Collectors.toMap(
                        map -> map.get("name"),
                        map -> Integer.parseInt(map.get("order"))
                ));

        Map<Integer, List<BteqScript>> scriptsByOrder = scripts.stream()
                .collect(Collectors.groupingBy(script -> orderByName.get(script.getScriptName())));

        int x = 0;
        int y_gap = 100;

        Map<Integer, List<Node>> nodesByOrder = scriptsByOrder.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            int y = 0;
                            List<Node> nodes = new java.util.ArrayList<>();
                            for (BteqScript script : entry.getValue()) {
                                Node node = new Node(script.getScriptName(), script.getScriptName());
                                node.getProperties().put("x", String.valueOf(entry.getKey() * 200));
                                node.getProperties().put("y", String.valueOf(y * y_gap));
                                y++;
                                nodes.add(node);
                                graph.addNode(node);
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
                    graph.addEdge(new Edge(fromNode.getId(), toNode.getId(), ""));
                }
            }
        }

        return graph;
    }
}
