package com.tdtsqlscan.web;

import com.tdtsqlscan.ddl.CreateTableQuery;
import com.tdtsqlscan.dml.InsertQuery;
import com.tdtsqlscan.etl.*;
import com.tdtsqlscan.graph.Edge;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;

import java.util.*;


public class DataFlowGraphConverter {

    private static final int BTEQ_LANE_Y = 0;
    private static final int DATA_LANE_START_Y = 200;
    private static final int LANE_HEIGHT = 200;
    private static final int X_OFFSET_STEP = 250;
    private static final int TABLE_X_OFFSET = 120;

    private Graph graph;
    private Map<String, Node> tableNodes;
    private LaneManager laneManager;
    private int xOffset;

    private static class LaneManager {
        private final Map<String, Integer> tableToLane = new HashMap<>();
        private int nextLane = 0;

        int getLaneForTables(Set<String> tableNames) {
            if (tableNames.isEmpty()) {
                return assignNewLane(null);
            }

            Set<Integer> existingLanes = new HashSet<>();
            for (String table : tableNames) {
                if (tableToLane.containsKey(table)) {
                    existingLanes.add(tableToLane.get(table));
                }
            }

            if (existingLanes.isEmpty()) {
                return assignNewLane(tableNames);
            } else {
                int lane = Collections.min(existingLanes);
                assignLane(tableNames, lane);
                return lane;
            }
        }

        private int assignNewLane(Set<String> tableNames) {
            int lane = nextLane++;
            if (tableNames != null) {
                assignLane(tableNames, lane);
            }
            return lane;
        }

        private void assignLane(Set<String> tableNames, int lane) {
            for (String table : tableNames) {
                tableToLane.put(table, lane);
            }
        }
    }

    public Graph convert(BteqScript script) {
        this.graph = new Graph();
        this.tableNodes = new HashMap<>();
        this.laneManager = new LaneManager();
        this.xOffset = 0;
        Node lastCommandNode = null;

        for (int i = 0; i < script.getCommands().size(); i++) {
            BteqCommand command = script.getCommands().get(i);
            lastCommandNode = processCommand(command, i, lastCommandNode);
        }

        return graph;
    }

    private Node processCommand(BteqCommand command, int index, Node lastCommandNode) {
        String commandNodeId = "cmd-" + index;
        int yPos;
        int currentX = xOffset;
        Set<String> relatedTables = getRelatedTables(command);

        if (command instanceof BteqControlCommand || command instanceof BteqConfigurationCommand) {
            yPos = BTEQ_LANE_Y;
        } else {
            int lane = laneManager.getLaneForTables(relatedTables);
            yPos = DATA_LANE_START_Y + (lane * LANE_HEIGHT);
        }

        Node commandNode = createCommandNode(command, commandNodeId);

        if (command instanceof BteqSqlCommand) {
            // For SQL commands, center them between potential source/target tables
            commandNode.addProperty("x", currentX + X_OFFSET_STEP / 2);
            handleDataFlow(commandNode, (BteqSqlCommand) command, yPos, currentX);
        } else {
            // For non-SQL commands, place them at the start of the block
            commandNode.addProperty("x", currentX);
        }

        commandNode.addProperty("y", yPos);
        graph.addNode(commandNode);

        if (lastCommandNode != null) {
            Edge logicEdge = new Edge(lastCommandNode.getId(), commandNode.getId(), "");
            logicEdge.addProperty("dashes", true);
            logicEdge.addProperty("arrows", "to");
            graph.addEdge(logicEdge);
        }

        xOffset += X_OFFSET_STEP;
        return commandNode;
    }

    private Set<String> getRelatedTables(BteqCommand command) {
        Set<String> tables = new HashSet<>();
        if (command instanceof BteqSqlCommand) {
            Object query = ((BteqSqlCommand) command).getQuery();
            if (query instanceof CreateTableQuery) {
                tables.add(((CreateTableQuery) query).getTableName());
            } else if (query instanceof InsertQuery) {
                tables.add(((InsertQuery) query).getTableName());
                if (((InsertQuery) query).getSourceTableName() != null) {
                    tables.add(((InsertQuery) query).getSourceTableName());
                }
            }
        }
        return tables;
    }

    private Node createCommandNode(BteqCommand command, String id) {
        String label;
        String shape = "box";

        if (command instanceof BteqConfigurationCommand) {
            label = "CONFIG";
            shape = "ellipse";
        } else if (command instanceof BteqControlCommand) {
            label = "." + ((BteqControlCommand) command).getType().toString();
            shape = "ellipse";
        } else if (command instanceof BteqSqlCommand) {
            Object query = ((BteqSqlCommand) command).getQuery();
            if (query instanceof CreateTableQuery) {
                label = "CREATE TABLE";
            } else if (query instanceof InsertQuery) {
                label = "INSERT";
            } else {
                label = "SQL";
            }
        } else {
            label = "UNKNOWN";
        }

        Node node = new Node(id, label);
        node.addProperty("shape", shape);
        node.addProperty("fullText", command.getRawText());
        return node;
    }

    private void handleDataFlow(Node commandNode, BteqSqlCommand sqlCommand, int yPos, int currentX) {
        Object query = sqlCommand.getQuery();
        if (query instanceof CreateTableQuery) {
            String tableName = ((CreateTableQuery) query).getTableName();
            if (tableName == null) return;

            Node tableNode = getOrCreateTableNode(tableName, yPos);
            tableNode.addProperty("x", currentX + X_OFFSET_STEP); // Place table to the right
            Edge edge = new Edge(commandNode.getId(), tableNode.getId(), "creates");
            edge.addProperty("arrows", "to");
            graph.addEdge(edge);

        } else if (query instanceof InsertQuery) {
            InsertQuery insertQuery = (InsertQuery) query;
            String targetTable = insertQuery.getTableName();
            String sourceTable = insertQuery.getSourceTableName();

            if (sourceTable != null) {
                Node sourceNode = getOrCreateTableNode(sourceTable, yPos);
                sourceNode.addProperty("x", currentX); // Source table at the beginning of the block
                Edge fromEdge = new Edge(sourceNode.getId(), commandNode.getId(), "reads from");
                fromEdge.addProperty("arrows", "to");
                graph.addEdge(fromEdge);
            }

            if (targetTable != null) {
                Node targetNode = getOrCreateTableNode(targetTable, yPos);
                // Target table to the right of the command
                targetNode.addProperty("x", currentX + X_OFFSET_STEP);
                Edge toEdge = new Edge(commandNode.getId(), targetNode.getId(), "inserts into");
                toEdge.addProperty("arrows", "to");
                graph.addEdge(toEdge);
            }
        }
    }

    private Node getOrCreateTableNode(String tableName, int yPos) {
        Node tableNode = tableNodes.get(tableName);
        if (tableNode == null) {
            tableNode = new Node(tableName, tableName);
            tableNode.addProperty("shape", "database");
            tableNode.addProperty("y", yPos);
            tableNodes.put(tableName, tableNode);
            graph.addNode(tableNode);
        } else {
            // If table node already exists, update its y-position to the current lane
            // This can happen if a table is used in multiple flows. We prioritize the latest flow.
            tableNode.addProperty("y", yPos);
        }
        return tableNode;
    }
}
