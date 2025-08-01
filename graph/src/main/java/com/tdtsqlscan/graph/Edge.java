package com.tdtsqlscan.graph;

import java.util.HashMap;
import java.util.Map;

public class Edge {
    private final String source;
    private final String target;
    private final String label;
    private final Map<String, Object> properties;

    public Edge(String source, String target, String label) {
        this.source = source;
        this.target = target;
        this.label = label;
        this.properties = new HashMap<>();
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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }
}
