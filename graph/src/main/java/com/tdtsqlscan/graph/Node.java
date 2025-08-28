package com.tdtsqlscan.graph;

import java.util.Map;
import java.util.HashMap;

public class Node {
    private final String id;
    private final String label;
    private final Map<String, Object> properties;

    public Node(String id, String label) {
        this.id = id;
        this.label = label;
        this.properties = new HashMap<>();
    }

    public String getId() {
        return id;
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
