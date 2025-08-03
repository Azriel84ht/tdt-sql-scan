package com.tdtsqlscan.graph;

import java.util.List;
import java.util.ArrayList;

public class Graph {
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final List<Integer> horizontalLaneYs;
    private final List<Integer> verticalLabelXs;

    public Graph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.horizontalLaneYs = new ArrayList<>();
        this.verticalLabelXs = new ArrayList<>();
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Integer> getHorizontalLaneYs() {
        return horizontalLaneYs;
    }

    public List<Integer> getVerticalLabelXs() {
        return verticalLabelXs;
    }
}
