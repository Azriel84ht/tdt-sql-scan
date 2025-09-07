package com.tdtsqlscan.web;

import com.tdtsqlscan.core.SQLParserUtils;
import com.tdtsqlscan.graph.Edge;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;
import com.tdtsqlscan.core.SelectQuery;
import com.tdtsqlscan.core.SQLTableRef;
import com.tdtsqlscan.core.SQLJoin;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts a SelectQuery object into a simple visual graph.
 * The graph shows the data flow from source tables to a result set.
 */
public class SelectGraphConverter {

    /**
     * Takes a SelectQuery object and generates a simple Graph.
     * This graph represents visually the input tables, the query process,
     * and the output result set.
     *
     * @param selectQuery The SelectQuery to convert.
     * @return A Graph object representing the select query.
     */
    public Graph convert(SelectQuery selectQuery) {
        Graph graph = new Graph();
        Set<String> tableNames = new HashSet<>();

        // Create central process node
        Node processNode = new Node("select-process", "SELECT Process");
        processNode.addProperty("shape", "ellipse");
        graph.addNode(processNode);

        // Create result set node
        Node resultSetNode = new Node("result-set", "Result Set");
        graph.addNode(resultSetNode);

        // Edge from process to result set
        Edge processToResultEdge = new Edge(processNode.getId(), resultSetNode.getId(), "");
        graph.addEdge(processToResultEdge);

        // Add source tables from main FROM clause
        for (SQLTableRef tableRef : selectQuery.getTables()) {
            tableNames.add(tableRef.getName());
        }

        // Add source tables from JOIN clauses
        for (SQLJoin join : selectQuery.getJoins()) {
            tableNames.add(join.getRight().getName());
        }

        // Create nodes and edges for each unique source table
        for (String tableName : tableNames) {
            Node tableNode = new Node(tableName, tableName);
            tableNode.addProperty("shape", "database");
            graph.addNode(tableNode);

            Edge edge = new Edge(tableNode.getId(), processNode.getId(), "");
            graph.addEdge(edge);
        }

        return graph;
    }
}
