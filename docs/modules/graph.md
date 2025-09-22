# graph Module Documentation

## Purpose

The `graph` module provides the fundamental data structures for representing the relationships and data flow extracted from SQL and BTEQ scripts. It defines generic `Node`, `Edge`, and `Graph` objects that can be used to model various aspects of data lineage, dependencies between database objects, or the control flow within BTEQ scripts. This structured graph representation is crucial for visualization, analysis, and export functionalities within the TDT SQL Scan project.

## Key Components

### Classes

*   **`Node`**
    *   **Description:** Represents a single entity or point in the graph. In the context of SQL analysis, a node could represent a table, a column, a file, a process, or even a specific SQL statement.
    *   **Fields:**
        *   `id` (String): A unique identifier for the node.
        *   `label` (String): A human-readable label or name for the node, used for display purposes.
        *   `properties` (Map<String, Object>): A flexible map to store any additional attributes or metadata associated with the node (e.g., type, location, status).
    *   **Key Methods:**
        *   `getId()`: Returns the unique ID of the node.
        *   `getLabel()`: Returns the display label of the node.
        *   `getProperties()`: Returns the map of properties.
        *   `addProperty(String key, Object value)`: Adds a property to the node.

*   **`Edge`**
    *   **Description:** Represents a directed connection or relationship between two `Node` objects in the graph. In data lineage, an edge typically signifies data flow or a dependency.
    *   **Fields:**
        *   `source` (String): The `id` of the source node from which the edge originates.
        *   `target` (String): The `id` of the target node to which the edge points.
        *   `label` (String): A label describing the nature of the relationship (e.g., "reads from", "writes to", "depends on").
        *   `properties` (Map<String, Object>): A flexible map to store any additional attributes or metadata associated with the edge (e.g., type of dependency, column mapping).
    *   **Key Methods:**
        *   `getSource()`: Returns the ID of the source node.
        *   `getTarget()`: Returns the ID of the target node.
        *   `getLabel()`: Returns the label of the edge.
        *   `getProperties()`: Returns the map of properties.
        *   `addProperty(String key, Object value)`: Adds a property to the edge.

*   **`Graph`**
    *   **Description:** The main container class that holds a collection of `Node` and `Edge` objects, forming a complete graph structure. It also includes fields that appear to be related to graph layout or visualization.
    *   **Fields:**
        *   `nodes` (List<Node>): A list of all nodes in the graph.
        *   `edges` (List<Edge>): A list of all edges in the graph.
        *   `horizontalLaneYs` (List<Integer>): A list of Y-coordinates, likely used for horizontal lane positioning in a visual layout.
        *   `verticalLabelXs` (List<Integer>): A list of X-coordinates, likely used for vertical label positioning in a visual layout.
    *   **Key Methods:**
        *   `addNode(Node node)`: Adds a node to the graph.
        *   `addEdge(Edge edge)`: Adds an edge to the graph.
        *   `getNodes()`: Returns the list of nodes.
        *   `getEdges()`: Returns the list of edges.
        *   `getHorizontalLaneYs()`: Returns the list of horizontal lane Y-coordinates.
        *   `getVerticalLabelXs()`: Returns the list of vertical label X-coordinates.

## Functionality

The `graph` module provides the basic building blocks for:

*   Representing complex relationships between entities identified during SQL parsing.
*   Constructing data lineage graphs, showing how data flows between tables and columns.
*   Modeling control flow graphs for BTEQ scripts.
*   Storing rich metadata on both nodes and edges through their `properties` maps.
*   (Implicitly) Supporting graph visualization by providing layout-related fields (`horizontalLaneYs`, `verticalLabelXs`).

## Dependencies

This module has no external runtime dependencies beyond standard Java libraries.

## How to Use

To build a graph, you would instantiate a `Graph` object and then add `Node` and `Edge` instances to it.

```java
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;
import com.tdtsqlscan.graph.Edge;

public class GraphExample {
    public static void main(String[] args) {
        Graph dataFlowGraph = new Graph();

        // Create nodes
        Node tableA = new Node("table_A", "Source Table A");
        tableA.addProperty("type", "table");
        tableA.addProperty("schema", "public");

        Node tableB = new Node("table_B", "Intermediate Table B");
        tableB.addProperty("type", "table");

        Node processX = new Node("process_X", "ETL Process X");
        processX.addProperty("type", "process");

        Node reportC = new Node("report_C", "Final Report C");
        reportC.addProperty("type", "report");

        // Add nodes to the graph
        dataFlowGraph.addNode(tableA);
        dataFlowGraph.addNode(tableB);
        dataFlowGraph.addNode(processX);
        dataFlowGraph.addNode(reportC);

        // Create edges
        Edge edge1 = new Edge("table_A", "process_X", "reads from");
        edge1.addProperty("columns", "col1, col2");

        Edge edge2 = new Edge("process_X", "table_B", "writes to");
        edge2.addProperty("transformation", "aggregation");

        Edge edge3 = new Edge("table_B", "report_C", "used by");

        // Add edges to the graph
        dataFlowGraph.addEdge(edge1);
        dataFlowGraph.addEdge(edge2);
        dataFlowGraph.addEdge(edge3);

        System.out.println("Graph created with " + dataFlowGraph.getNodes().size() + " nodes and " + dataFlowGraph.getEdges().size() + " edges.");

        // Example of accessing graph data
        for (Node node : dataFlowGraph.getNodes()) {
            System.out.println("Node ID: " + node.getId() + ", Label: " + node.getLabel() + ", Type: " + node.getProperties().get("type"));
        }
    }
}
```
