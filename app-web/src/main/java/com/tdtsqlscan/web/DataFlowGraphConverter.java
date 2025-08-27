package com.tdtsqlscan.web;

import com.tdtsqlscan.ddl.CreateTableQuery;
import com.tdtsqlscan.ddl.DropTableQuery;
import com.tdtsqlscan.dml.DeleteQuery;
import com.tdtsqlscan.dml.InsertQuery;
import com.tdtsqlscan.dml.UpdateQuery;
import com.tdtsqlscan.etl.*;
import com.tdtsqlscan.graph.Edge;
import com.tdtsqlscan.graph.Graph;
import com.tdtsqlscan.graph.Node;

import java.util.*;


public class DataFlowGraphConverter {

    private static final int BTEQ_LANE_Y = 0;
    private static final int DATA_LANE_START_Y = 150;
    private static final int LANE_HEIGHT = 120;
    private static final int X_OFFSET_STEP_SQL = 180;
    private static final int X_OFFSET_STEP_CONTROL = 75;
    private static final int X_OFFSET_STEP_LANE_CHANGE = 50;

    private Graph graph;
    private Map<String, Node> tableNodes;
    private LaneManager laneManager;
    private int xOffset;
    private String currentDatabase = null;

    private static class LaneManager {
        private final Map<String, Integer> tableToLane = new HashMap<>();
        private int nextLane = 0;
        final Set<Integer> usedLanes = new HashSet<>();

        int getLaneForTable(String tableName) {
            if (tableName == null) {
                // For commands without a table, create a new lane but don't track the table
                int lane = nextLane++;
                usedLanes.add(lane);
                return lane;
            }

            if (tableToLane.containsKey(tableName)) {
                return tableToLane.get(tableName);
            }

            int lane = nextLane++;
            tableToLane.put(tableName, lane);
            usedLanes.add(lane);
            return lane;
        }
    }

    public Graph convert(BteqScript script) {
        this.graph = new Graph();
        this.tableNodes = new HashMap<>();
        this.laneManager = new LaneManager();
        this.xOffset = 0;
        Node lastCommandNode = null;

        // Pre-calculate lane numbers for all commands to detect lane changes
        Map<BteqCommand, Integer> commandToLane = new HashMap<>();
        LaneManager laneNumberer = new LaneManager();
        for (BteqCommand command : script.getCommands()) {
            commandToLane.put(command, getLaneNumber(command, laneNumberer));
        }

        int i = 0;
        while (i < script.getCommands().size()) {
            BteqCommand command = script.getCommands().get(i);

            if (command instanceof BteqControlCommand && ((BteqControlCommand) command).getType() == BteqCommandType.DATABASE) {
                String[] parts = command.getRawText().trim().split("\\s+");
                if (parts.length > 1) {
                    String dbName = parts[1];
                    if (dbName.endsWith(";")) {
                        dbName = dbName.substring(0, dbName.length() - 1);
                    }
                    this.currentDatabase = dbName;
                }
            }
            int xStep;

            // Check if the command is a candidate for grouping
            if (isGroupableInsert(command)) {
                List<BteqCommand> group = findInsertGroup(script.getCommands(), i);
                lastCommandNode = processInsertGroup(group, i, lastCommandNode);

                int groupSize = group.size();
                int currentLane = commandToLane.get(command);
                int nextLane = -2; // Use a value that is different from any possible lane number
                if (i + groupSize < script.getCommands().size()) {
                    nextLane = commandToLane.get(script.getCommands().get(i + groupSize));
                }

                if (nextLane != -2 && currentLane != nextLane) {
                    xStep = X_OFFSET_STEP_LANE_CHANGE;
                } else {
                    xStep = X_OFFSET_STEP_SQL;
                }
                i += groupSize; // Skip past the commands that were just grouped
            } else {
                lastCommandNode = processCommand(command, i, lastCommandNode);

                int currentLane = commandToLane.get(command);
                int nextLane = -2;
                if (i + 1 < script.getCommands().size()) {
                    nextLane = commandToLane.get(script.getCommands().get(i + 1));
                }

                if (nextLane != -2 && currentLane != nextLane) {
                    xStep = X_OFFSET_STEP_LANE_CHANGE;
                } else {
                    if (command instanceof BteqControlCommand || command instanceof BteqConfigurationCommand) {
                        xStep = X_OFFSET_STEP_CONTROL;
                    } else {
                        xStep = X_OFFSET_STEP_SQL;
                    }
                }
                i++;
            }
            xOffset += xStep;
        }

        // After processing all commands, populate the guide line coordinates
        graph.getHorizontalLaneYs().add(BTEQ_LANE_Y + (DATA_LANE_START_Y - BTEQ_LANE_Y) / 2);
        for (int lane : laneManager.usedLanes) {
            graph.getHorizontalLaneYs().add(DATA_LANE_START_Y + (lane * LANE_HEIGHT) + (LANE_HEIGHT / 2));
        }

        return graph;
    }

    private Node processCommand(BteqCommand command, int index, Node lastCommandNode) {
        String commandNodeId = "cmd-" + index;
        int yPos;
        int currentX = xOffset;

        if (command instanceof BteqControlCommand || command instanceof BteqConfigurationCommand ||
            (command instanceof BteqSqlCommand && ((BteqSqlCommand) command).getQuery() instanceof com.tdtsqlscan.select.SelectQuery)) {
            yPos = BTEQ_LANE_Y;
            if (command instanceof BteqControlCommand && ((BteqControlCommand) command).getType() == BteqCommandType.LABEL) {
                graph.getVerticalLabelXs().add(currentX);
            }
        } else {
            // For SQL commands, the lane is determined by the TARGET table.
            String targetTable = getTargetTable(command);
            int lane = laneManager.getLaneForTable(targetTable);
            yPos = DATA_LANE_START_Y + (lane * LANE_HEIGHT);
        }

        Node commandNode = createCommandNode(command, commandNodeId);

        if (command instanceof BteqSqlCommand) {
            // For SQL commands, center them between potential source/target tables
            commandNode.addProperty("x", currentX + X_OFFSET_STEP_SQL / 2);
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

        return commandNode;
    }

    private int getLaneNumber(BteqCommand command, LaneManager laneManager) {
        if (command instanceof BteqControlCommand || command instanceof BteqConfigurationCommand ||
                (command instanceof BteqSqlCommand && ((BteqSqlCommand) command).getQuery() instanceof com.tdtsqlscan.select.SelectQuery)) {
            return -1; // Special value for the BTEQ lane
        } else {
            String targetTable = getTargetTable(command);
            return laneManager.getLaneForTable(targetTable);
        }
    }

    private String getTargetTable(BteqCommand command) {
        String tableName = null;
        if (command instanceof BteqSqlCommand) {
            Object query = ((BteqSqlCommand) command).getQuery();
            if (query instanceof CreateTableQuery) tableName = ((CreateTableQuery) query).getTableName();
            else if (query instanceof InsertQuery) tableName = ((InsertQuery) query).getTableName();
            else if (query instanceof UpdateQuery) tableName = ((UpdateQuery) query).getTargetTable();
            else if (query instanceof DropTableQuery) tableName = ((DropTableQuery) query).getTableName();
            else if (query instanceof DeleteQuery) tableName = ((DeleteQuery) query).getTable();
        }
        return tableName != null ? tableName.toUpperCase() : null;
    }

    private boolean isGroupableInsert(BteqCommand command) {
        if (!(command instanceof BteqSqlCommand)) {
            return false;
        }
        Object query = ((BteqSqlCommand) command).getQuery();
        if (!(query instanceof InsertQuery)) {
            return false;
        }
        // Groupable inserts are those without a source table (i.e., INSERT ... VALUES)
        return ((InsertQuery) query).getSourceTableName() == null;
    }

    private Node processInsertGroup(List<BteqCommand> group, int startIndex, Node lastCommandNode) {
        BteqCommand firstCommand = group.get(0);
        String targetTable = ((InsertQuery) ((BteqSqlCommand) firstCommand).getQuery()).getTableName().toUpperCase();
        String commandNodeId = "cmd-group-" + startIndex;

        // Build the consolidated node
        StringBuilder fullText = new StringBuilder();
        for (BteqCommand cmd : group) {
            fullText.append(cmd.getRawText()).append("\n\n");
        }
        String label = String.format("Batch INSERT (%d)", group.size());
        Node commandNode = new Node(commandNodeId, label);
        commandNode.addProperty("shape", "image");
        commandNode.addProperty("image", "images/insert.png");
        commandNode.addProperty("size", 30);
        commandNode.addProperty("fullText", fullText.toString().trim());
        commandNode.addProperty("fixed", true);

        // Position and connect the node
        int lane = laneManager.getLaneForTable(targetTable);
        int yPos = DATA_LANE_START_Y + (lane * LANE_HEIGHT);
        int currentX = xOffset;

        commandNode.addProperty("x", currentX + X_OFFSET_STEP_SQL / 2);
        commandNode.addProperty("y", yPos);
        graph.addNode(commandNode);

        // Keep this call to register the table for lane management, but don't create an edge
        getOrCreateTableNode(targetTable, yPos);

        // Connect the logic flow
        if (lastCommandNode != null) {
            Edge logicEdge = new Edge(lastCommandNode.getId(), commandNode.getId(), "");
            logicEdge.addProperty("dashes", true);
            logicEdge.addProperty("arrows", "to");
            graph.addEdge(logicEdge);
        }

        return commandNode;
    }

    private List<BteqCommand> findInsertGroup(List<BteqCommand> commands, int startIndex) {
        List<BteqCommand> group = new ArrayList<>();
        BteqCommand firstCommand = commands.get(startIndex);
        String targetTable = ((InsertQuery) ((BteqSqlCommand) firstCommand).getQuery()).getTableName();
        group.add(firstCommand);

        for (int i = startIndex + 1; i < commands.size(); i++) {
            BteqCommand nextCommand = commands.get(i);
            if (isGroupableInsert(nextCommand)) {
                String nextTargetTable = ((InsertQuery) ((BteqSqlCommand) nextCommand).getQuery()).getTableName();
                if (targetTable.equals(nextTargetTable)) {
                    group.add(nextCommand);
                } else {
                    break; // Different table, end of group
                }
            } else {
                break; // Not a groupable insert, end of group
            }
        }
        return group;
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
            } else if (query instanceof UpdateQuery) {
                tables.add(((UpdateQuery) query).getTargetTable());
            } else if (query instanceof DropTableQuery) {
                tables.add(((DropTableQuery) query).getTableName());
            }
        }
        return tables;
    }

    private Node createCommandNode(BteqCommand command, String id) {
        String label = "UNKNOWN";
        String shape = "box";
        String image = null;
        Node node = new Node(id, ""); // Create node with empty label initially

        if (command instanceof BteqConfigurationCommand) {
            label = "START";
            shape = "image";
            image = "images/bteq_commands/start.png";
        } else if (command instanceof BteqControlCommand) {
            BteqControlCommand controlCommand = (BteqControlCommand) command;
            BteqCommandType type = controlCommand.getType();

            if (type == BteqCommandType.SET || type == BteqCommandType.DECLARE) {
                shape = "image";
                image = "images/bteq_commands/config.png";
                label = ""; // The icon is the representation
            } else if (type == BteqCommandType.EXPORT) {
                shape = "image";
                image = "images/bteq_commands/export.png";
                label = "";
            } else if (type == BteqCommandType.LABEL) {
                shape = "image";
                image = "images/bteq_commands/label.png";
                String rawText = controlCommand.getRawText().trim();
                String[] parts = rawText.split("\\s+");
                if (parts.length > 1) {
                    label = parts[1];
                    if (label.endsWith(";")) {
                        label = label.substring(0, label.length() - 1);
                    }
                } else {
                    label = "";
                }
            } else if (type == BteqCommandType.GOTO) {
                shape = "image";
                image = "images/bteq_commands/goto.png";
                label = "";
            } else if (type == BteqCommandType.IF) {
                shape = "image";
                image = "images/bteq_commands/if.png";
                label = "";
            } else if (type == BteqCommandType.OTHER) {
                String rawText = controlCommand.getRawText().trim();
                if (rawText.startsWith(".")) {
                    label = rawText.split("\\s+")[0];
                } else {
                    label = ".OTHER";
                }
                shape = "ellipse";
            } else {
                label = "." + type.toString();
                shape = "ellipse";
            }

            if (type == BteqCommandType.EXIT) {
                shape = "star";
            }
        } else if (command instanceof BteqSqlCommand) {
            Object query = ((BteqSqlCommand) command).getQuery();
            shape = "image"; // Default to image for SQL commands

            if (query instanceof CreateTableQuery) {
                CreateTableQuery createTableQuery = (CreateTableQuery) query;

                // Add metadata for empty structure CREATE TABLE
                if (createTableQuery.getSourceTables().isEmpty()) {
                    node.addProperty("metadataType", "CREATE_TABLE_STRUCTURE");
                    String fullTableName = createTableQuery.getTableName();
                    String dbName = null;
                    String tableName = fullTableName;
                    boolean fromContext = false;

                    if (fullTableName.contains(".")) {
                        String[] parts = fullTableName.split("\\.");
                        dbName = parts[0];
                        tableName = parts[1];
                    } else if (this.currentDatabase != null) {
                        dbName = this.currentDatabase;
                        fromContext = true;
                    } else {
                        dbName = "[Default Database]";
                    }

                    node.addProperty("Tablename", tableName);
                    node.addProperty("Databasename", dbName);
                    node.addProperty("isDatabaseFromContext", fromContext);

                    List<Map<String, String>> columns = new ArrayList<>();
                    for (com.tdtsqlscan.ddl.ColumnDefinition col : createTableQuery.getColumns()) {
                        Map<String, String> colData = new HashMap<>();
                        colData.put("name", col.getName());
                        colData.put("type", col.getType());
                        columns.add(colData);
                    }
                    node.addProperty("columns", columns);
                }

                if (createTableQuery.isVolatile()) {
                    label = "CREATE VOLATILE TABLE";
                    image = "images/create_volatile_table.png";
                } else {
                    label = "CREATE TABLE";
                    image = "images/create_table.png";
                }
            } else if (query instanceof InsertQuery) {
                label = "INSERT";
                image = "images/insert.png";
            } else if (query instanceof com.tdtsqlscan.select.SelectQuery) {
                label = "SELECT";
                image = "images/select.png";
            } else if (query instanceof UpdateQuery) {
                label = "UPDATE";
                image = "images/update.png";
            } else if (query instanceof DropTableQuery) {
                label = "DROP TABLE";
                image = "images/drop_table.png";
            } else if (query instanceof DeleteQuery) {
                label = "DELETE";
                image = "images/delete.png";
            } else {
                label = "SQL";
                shape = "box"; // Revert to box for other SQL
            }
        }

        node.setLabel(label);
        node.addProperty("shape", shape);
        if (image != null) {
            node.addProperty("image", image);
            node.addProperty("size", 30);
        }
        node.addProperty("fullText", command.getRawText());
        node.addProperty("fixed", true);
        if (command instanceof BteqConfigurationCommand) {
            node.addProperty("noContextMenu", true);
        }
        return node;
    }

    private void handleDataFlow(Node commandNode, BteqSqlCommand sqlCommand, int yPos, int currentX) {
        // This method is now obsolete, as per the user's request to not show data flow arrows
        // or table nodes, only the command nodes on the correct lanes.
    }

    private Node getOrCreateTableNode(String tableName, int yPos) {
        String upperCaseTableName = tableName.toUpperCase();
        Node tableNode = tableNodes.get(upperCaseTableName);
        if (tableNode == null) {
            // Use the original case-preserved name for the node's ID and label for display
            tableNode = new Node(tableName, tableName);
            tableNode.addProperty("shape", "database");
            tableNode.addProperty("y", yPos);
            tableNode.addProperty("fixed", true);
            // Use the uppercase name for the map key to ensure case-insensitivity
            tableNodes.put(upperCaseTableName, tableNode);
            // DO NOT add the node to the graph.
        }
        return tableNode;
    }
}
