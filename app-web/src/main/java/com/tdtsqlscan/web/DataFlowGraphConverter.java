package com.tdtsqlscan.web;

import com.tdtsqlscan.graph.Edge;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;
import com.tdtsqlscan.web.client.dto.ParseResultDto;
import com.tdtsqlscan.web.client.dto.StatementDto;
import com.tdtsqlscan.web.client.dto.StatementType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;


public class DataFlowGraphConverter {

    private static final int BTEQ_LANE_Y = 0;
    private static final int DATA_LANE_START_Y = 150;
    private static final int LANE_HEIGHT = 120;
    private static final int X_OFFSET_STEP_SQL = 180;
    private static final int X_OFFSET_STEP_CONTROL = 75;

    private Graph graph;
    private LaneManager laneManager;
    private int xOffset;

    private static class LaneManager {
        private final Map<String, Integer> tableToLane = new HashMap<>();
        private int nextLane = 0;
        final Set<Integer> usedLanes = new HashSet<>();

        int getLaneForTable(String tableName) {
            if (tableName == null) {
                int lane = nextLane++;
                usedLanes.add(lane);
                return lane;
            }
            return tableToLane.computeIfAbsent(tableName.toUpperCase(), k -> {
                int lane = nextLane++;
                usedLanes.add(lane);
                return lane;
            });
        }
    }

    public Graph convert(ParseResultDto parseResult) {
        this.graph = new Graph();
        this.laneManager = new LaneManager();
        this.xOffset = 0;
        Node lastCommandNode = null;

        List<StatementDto> statements = parseResult.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            StatementDto stmt = statements.get(i);
            lastCommandNode = processStatement(stmt, i, lastCommandNode);
        }

        graph.getHorizontalLaneYs().add(BTEQ_LANE_Y + (DATA_LANE_START_Y - BTEQ_LANE_Y) / 2);
        for (int lane : laneManager.usedLanes) {
            graph.getHorizontalLaneYs().add(DATA_LANE_START_Y + (lane * LANE_HEIGHT) + (LANE_HEIGHT / 2));
        }

        return graph;
    }

    private Node processStatement(StatementDto stmt, int index, Node lastCommandNode) {
        String commandNodeId = "cmd-" + index;
        int yPos;
        int xStep = X_OFFSET_STEP_CONTROL;

        if (stmt.getType() == StatementType.BTEQ_COMMAND || "SELECT".equalsIgnoreCase(stmt.getCommandName())) {
            yPos = BTEQ_LANE_Y;
            if (".LABEL".equalsIgnoreCase(stmt.getCommandName())) {
                graph.getVerticalLabelXs().add(xOffset);
            }
        } else {
            xStep = X_OFFSET_STEP_SQL;
            String targetTable = getTargetTable(stmt);
            int lane = laneManager.getLaneForTable(targetTable);
            yPos = DATA_LANE_START_Y + (lane * LANE_HEIGHT);
        }

        Node commandNode = createCommandNode(stmt, commandNodeId);
        commandNode.addProperty("x", xOffset + xStep / 2);
        commandNode.addProperty("y", yPos);
        graph.addNode(commandNode);

        if (lastCommandNode != null) {
            Edge logicEdge = new Edge(lastCommandNode.getId(), commandNode.getId(), "");
            logicEdge.addProperty("dashes", true);
            logicEdge.addProperty("arrows", "to");
            graph.addEdge(logicEdge);
        }

        xOffset += xStep;
        return commandNode;
    }

    private String getTargetTable(StatementDto stmt) {
        if (stmt.getDetails() != null && stmt.getDetails().containsKey("table_name")) {
            return stmt.getDetails().get("table_name");
        }
        return null;
    }

    private Node createCommandNode(StatementDto stmt, String id) {
        String label = stmt.getCommandName();
        String shape = "box";
        String image = null;

        if (stmt.getType() == StatementType.BTEQ_COMMAND) {
            shape = "ellipse";
            // Map BTEQ command names to images
            switch (label.toUpperCase()) {
                case ".LOGON":
                case ".DATABASE":
                    label = "START";
                    shape = "image";
                    image = "/images/bteq_commands/start.png";
                    break;
                case ".SET":
                case ".DECLARE":
                    shape = "image";
                    image = "/images/bteq_commands/config.png";
                    label = "";
                    break;
                case ".EXPORT":
                    shape = "image";
                    image = "/images/bteq_commands/export.png";
                    label = "";
                    break;
                case ".LABEL":
                    shape = "image";
                    image = "/images/bteq_commands/label.png";
                    label = stmt.getDetails() != null ? stmt.getDetails().getOrDefault("label_name", "") : "";
                    break;
                case ".GOTO":
                    shape = "image";
                    image = "/images/bteq_commands/goto.png";
                    label = "";
                    break;
                case ".IF":
                    shape = "image";
                    image = "/images/bteq_commands/if.png";
                    label = "";
                    break;
                case ".EXIT":
                case ".LOGOFF":
                    shape = "star";
                    break;
            }
        } else { // SQL Statements
            shape = "image";
            switch (label.toUpperCase()) {
                case "CREATE TABLE":
                     image = stmt.getDetails() != null && "true".equalsIgnoreCase(stmt.getDetails().get("is_volatile"))
                            ? "/images/create_volatile_table.png"
                            : "/images/create_table.png";
                    break;
                case "INSERT":
                    image = "/images/insert.png";
                    break;
                case "SELECT":
                    image = "/images/select.png";
                    break;
                case "UPDATE":
                    image = "/images/update.png";
                    break;
                case "DROP TABLE":
                    image = "/images/drop_table.png";
                    break;
                case "DELETE":
                    image = "/images/delete.png";
                    break;
                case "CREATE INDEX":
                    image = "/images/right_arrow.png"; // Placeholder, might need a better icon
                    break;
                default:
                    shape = "box"; // Fallback for unknown SQL
                    break;
            }
        }

        Node node = new Node(id, label);
        node.addProperty("shape", shape);
        if (image != null) {
            node.addProperty("image", image);
            node.addProperty("size", 30);
        }
        node.addProperty("fullText", stmt.getRawContent());
        node.addProperty("fixed", true);

        if (stmt.getDetails() != null && !stmt.getDetails().isEmpty()) {
            node.addProperty("metadata", new LinkedHashMap<>(stmt.getDetails()));
        }

        // Simplified check for SELECT visualization
        if (stmt.getType() != StatementType.BTEQ_COMMAND && stmt.getDetails() != null && stmt.getDetails().containsKey("source_tables")) {
             node.addProperty("hasSelectQuery", true);
        }


        return node;
    }
}
