package com.tdtsqlscan.core;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

public class JsqlAstConverter {

    public static SQLQuery convert(Statement statement, String rawSql) {
        if (statement instanceof Select) {
            return convertSelect((Select) statement, rawSql);
        } else if (statement instanceof CreateTable) {
            return convertCreateTable((CreateTable) statement, rawSql);
        } else if (statement instanceof CreateIndex) {
            return convertCreateIndex((CreateIndex) statement, rawSql);
        } else if (statement instanceof Drop) {
            return convertDrop((Drop) statement, rawSql);
        } else if (statement instanceof Insert) {
            return convertInsert((Insert) statement, rawSql);
        } else if (statement instanceof Update) {
            return convertUpdate((Update) statement, rawSql);
        } else if (statement instanceof Delete) {
            return convertDelete((Delete) statement, rawSql);
        }
        return null;
    }

    private static SelectQuery convertSelect(Select select, String rawSql) {
        if (select instanceof net.sf.jsqlparser.statement.select.ParenthesedSelect) {
            net.sf.jsqlparser.statement.select.ParenthesedSelect parenthesedSelect = (net.sf.jsqlparser.statement.select.ParenthesedSelect) select;
            return convertSelect(parenthesedSelect.getSelect(), rawSql);
        }

        SelectQuery selectQuery = new SelectQuery(rawSql);
        if (select instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) select;
            // columns
            plainSelect.getSelectItems().forEach(si -> selectQuery.addColumn(si.toString()));
            // tables
            if (plainSelect.getFromItem() != null) {
                if (plainSelect.getFromItem() instanceof Table) {
                    Table table = (Table) plainSelect.getFromItem();
                    String alias = table.getAlias() != null ? table.getAlias().getName() : null;
                    selectQuery.addTable(new SQLTableRef(table.getFullyQualifiedName(), alias));
                } else {
                    selectQuery.addTable(new SQLTableRef(plainSelect.getFromItem().toString(), null));
                }
            }
            // joins
            if (plainSelect.getJoins() != null) {
                plainSelect.getJoins().forEach(j -> {
                    SQLJoin.Type joinType = SQLJoin.Type.UNKNOWN;
                    if (j.isInner()) joinType = SQLJoin.Type.INNER;
                    if (j.isLeft()) joinType = SQLJoin.Type.LEFT;
                    if (j.isRight()) joinType = SQLJoin.Type.RIGHT;
                    if (j.isFull()) joinType = SQLJoin.Type.FULL;
                    if (j.isCross()) joinType = SQLJoin.Type.CROSS;

                    SQLTableRef rightTable = null;
                    if (j.getRightItem() instanceof Table) {
                        Table table = (Table) j.getRightItem();
                        String alias = table.getAlias() != null ? table.getAlias().getName() : null;
                        rightTable = new SQLTableRef(table.getFullyQualifiedName(), alias);
                    } else {
                        rightTable = new SQLTableRef(j.getRightItem().toString(), null);
                    }

                    String onCondition = j.getOnExpression() != null ? j.getOnExpression().toString() : null;

                    selectQuery.addJoin(new SQLJoin(joinType, null, rightTable, onCondition));
                });
            }
            // where
            if (plainSelect.getWhere() != null) {
                selectQuery.addWhereCondition(new SQLCondition(plainSelect.getWhere().toString()));
            }
            // group by
            if (plainSelect.getGroupBy() != null) {
                plainSelect.getGroupBy().getGroupByExpressions().forEach(e -> selectQuery.addGroupBy(e.toString()));
            }
        }
        return selectQuery;
    }

    private static CreateTableQuery convertCreateTable(CreateTable createTable, String rawSql) {
        String tableName = createTable.getTable().getFullyQualifiedName();
        boolean isVolatile = false;
        if (createTable.getTableOptionsStrings() != null) {
            for (String option : createTable.getTableOptionsStrings()) {
                if (option.equalsIgnoreCase("VOLATILE")) {
                    isVolatile = true;
                    break;
                }
            }
        }

        List<ColumnDefinition> columns = new ArrayList<>();
        if (createTable.getColumnDefinitions() != null) {
            columns = createTable.getColumnDefinitions().stream()
                    .map(cd -> new ColumnDefinition(cd.getColumnName(), cd.getColDataType().toString()))
                    .collect(Collectors.toList());
        }

        SelectQuery selectQuery = null;
        List<SQLTableRef> sourceTables = new ArrayList<>();
        if (createTable.getSelect() != null) {
            selectQuery = (SelectQuery) convertSelect(createTable.getSelect(), createTable.getSelect().toString());
            if (selectQuery != null) {
                sourceTables = selectQuery.getTables();
            }
        } else {
            // Handle CREATE TABLE AS ... (SELECT ...)
            int asIndex = rawSql.toUpperCase().indexOf("AS");
            if (asIndex != -1) {
                String selectPart = rawSql.substring(asIndex + 2).trim();
                String selectSql;
                if (selectPart.toUpperCase().contains("WITH DATA")) {
                    selectSql = selectPart.substring(0, selectPart.toUpperCase().indexOf("WITH DATA")).trim();
                } else {
                    selectSql = selectPart;
                }

                if (selectSql.startsWith("(") && selectSql.endsWith(")")) {
                    selectSql = selectSql.substring(1, selectSql.length() - 1);
                }
                System.out.println("Parsing SELECT part: " + selectSql);
                try {
                    Statement selectStatement = net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(selectSql);
                    if (selectStatement instanceof Select) {
                        selectQuery = convertSelect((Select) selectStatement, selectSql);
                        if (selectQuery != null) {
                            sourceTables = selectQuery.getTables();
                        }
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
        }

        return new CreateTableQuery(rawSql, tableName, columns, isVolatile, sourceTables, selectQuery);
    }

    private static CreateIndexQuery convertCreateIndex(CreateIndex createIndex, String rawSql) {
        String indexName = createIndex.getIndex().getName();
        String tableName = createIndex.getTable().getFullyQualifiedName();
        boolean isUnique = createIndex.getIndex().getType() != null && createIndex.getIndex().getType().equalsIgnoreCase("UNIQUE");
        List<String> columns = createIndex.getIndex().getColumns().stream()
                .map(c -> c.getColumnName())
                .collect(Collectors.toList());

        return new CreateIndexQuery(rawSql, isUnique, indexName, tableName, columns);
    }

    private static DropTableQuery convertDrop(Drop drop, String rawSql) {
        String tableName = drop.getName().getFullyQualifiedName();
        return new DropTableQuery(rawSql, tableName);
    }

    private static InsertQuery convertInsert(Insert insert, String rawSql) {
        String tableName = insert.getTable().getFullyQualifiedName();
        List<String> columns = insert.getColumns() != null ?
                insert.getColumns().stream().map(c -> c.getColumnName()).collect(Collectors.toList()) :
                new ArrayList<>();

        List<List<String>> values = new ArrayList<>();
        if (insert.getSelect() == null) {
            // This is a simplified conversion. A real implementation would need to handle different types of ItemsList.
        }

        SelectQuery selectQuery = null;
        if (insert.getSelect() != null) {
            selectQuery = (SelectQuery) convertSelect(insert.getSelect(), insert.getSelect().toString());
        }

        List<SQLTableRef> sourceTables = new ArrayList<>();
        if (selectQuery != null) {
            sourceTables = selectQuery.getTables();
        }

        return new InsertQuery(rawSql, tableName, columns, values, sourceTables, selectQuery);
    }

    private static UpdateQuery convertUpdate(Update update, String rawSql) {
        String targetTable = update.getTable().getFullyQualifiedName();
        List<SQLTableRef> sourceTables = new ArrayList<>();
        if (update.getFromItem() != null) {
            // This is a simplified conversion. A real implementation would need to handle joins and subqueries.
            if (update.getFromItem() instanceof Table) {
                Table table = (Table) update.getFromItem();
                String alias = table.getAlias() != null ? table.getAlias().getName() : null;
                sourceTables.add(new SQLTableRef(table.getFullyQualifiedName(), alias));
            } else {
                sourceTables.add(new SQLTableRef(update.getFromItem().toString(), null));
            }
        }

        return new UpdateQuery(rawSql, targetTable, sourceTables);
    }

    private static DeleteQuery convertDelete(Delete delete, String rawSql) {
        String tableName = delete.getTable().getFullyQualifiedName();
        SQLCondition condition = null;
        if (delete.getWhere() != null) {
            condition = new SQLCondition(delete.getWhere().toString());
        }
        return new DeleteQuery(rawSql, tableName, condition);
    }
}
