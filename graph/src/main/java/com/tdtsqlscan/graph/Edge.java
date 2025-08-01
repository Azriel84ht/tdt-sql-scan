package com.tdtsqlscan.graph;

public class Edge {
    private final String source;
    private final String target;
    private final String label;

    public Edge(String source, String target, String label) {
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getLabel() {
        return label;
    }
}
