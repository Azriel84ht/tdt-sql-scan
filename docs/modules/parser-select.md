# parser-select Module Documentation

## Purpose

The `parser-select` module is responsible for parsing SQL `SELECT` statements. It takes a raw `SELECT` query string and transforms it into a structured `SelectQuery` object, which represents the Abstract Syntax Tree (AST) of the query. This structured representation allows for detailed analysis of the selected columns, tables involved, join conditions, filtering criteria (`WHERE` and `HAVING`), grouping, and ordering.

## Key Components

### Classes

*   **`SelectParser`**
    *   **Description:** Implements the `QueryParser` interface, providing the logic to determine if a given SQL string is a `SELECT` statement and to parse it into a `SelectQuery` object.
    *   **Key Methods:**
        *   `boolean supports(String sql)`: Returns `true` if the SQL string starts with "SELECT" (case-insensitive).
        *   `SQLQuery parse(String sql)`: Parses the `SELECT` statement. It extracts the select list, `FROM` clause, `JOIN` clauses, `WHERE` clause, `GROUP BY` clause, and `ORDER BY` clause, populating a `SelectQuery` object.
        *   `private SQLTableRef parseTableRef(String expr)`: A helper method to parse a table reference string (e.g., `my_table AS mt`) into an `SQLTableRef` object.
        *   `private SQLJoin parseJoin(String clause)`: A helper method to parse a `JOIN` clause (e.g., `LEFT JOIN other_table ON ...`) into an `SQLJoin` object.

*   **`SelectQuery`**
    *   **Description:** Extends `SQLQuery` from the `parser-core` module. This class serves as the AST node for a `SELECT` statement, holding all the parsed components of the query.
    *   **Fields:**
        *   `List<String> columns`: A list of selected column expressions.
        *   `List<SQLTableRef> tables`: A list of primary table references in the `FROM` clause.
        *   `List<SQLJoin> joins`: A list of `SQLJoin` objects representing join operations.
        *   `List<SQLCondition> whereConds`: A list of `SQLCondition` objects for the `WHERE` clause.
        *   `List<String> groupBy`: A list of expressions used in the `GROUP BY` clause.
        *   `List<SQLCondition> havingConds`: A list of `SQLCondition` objects for the `HAVING` clause.
        *   `List<SQLOrderItem> orderBy`: A list of `SQLOrderItem` objects for the `ORDER BY` clause.
        *   `int limit`: The value specified in a `LIMIT` clause (if present), default -1.
        *   `int offset`: The value specified in an `OFFSET` clause (if present), default -1.
    *   **Methods:** Provides getter and setter methods for all its fields, and `add` methods for lists (e.g., `addColumn`, `addTable`).

## Functionality

The `parser-select` module provides the capability to:

*   Identify `SELECT` statements.
*   Extract individual columns or expressions from the `SELECT` list.
*   Identify the primary table(s) in the `FROM` clause.
*   Parse various types of `JOIN` clauses (INNER, LEFT, RIGHT, FULL) and their `ON` conditions.
*   Extract `WHERE` and `HAVING` clause conditions.
*   Identify `GROUP BY` expressions.
*   Parse `ORDER BY` items, including their direction (ASC/DESC).
*   (Conceptual) Support for `LIMIT` and `OFFSET` clauses, though the current `SelectParser` implementation does not fully parse these.

## Dependencies

This module depends on the `parser-core` module for foundational classes and utilities such as `QueryParser`, `SQLQuery`, `SQLParseException`, `SQLParserUtils`, `SQLTableRef`, `SQLJoin`, `SQLCondition`, and `SQLOrderItem`.

## How to Use

To use the `SelectParser`, you would typically instantiate it and then call its `parse` method with an SQL `SELECT` statement.

```java
import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.select.SelectParser;
import com.tdtsqlscan.select.SelectQuery;

public class SelectExample {
    public static void main(String[] args) {
        String sql = "SELECT a.col1, b.col2 FROM table1 a JOIN table2 b ON a.id = b.id WHERE a.col1 > 10 ORDER BY a.col1 DESC";
        SelectParser parser = new SelectParser();

        if (parser.supports(sql)) {
            try {
                SelectQuery selectQuery = (SelectQuery) parser.parse(sql);

                System.out.println("Original SQL: " + selectQuery.getSql());
                System.out.println("Type: " + selectQuery.getType());
                System.out.println("Columns: " + selectQuery.getColumns());
                System.out.println("Tables: " + selectQuery.getTables());
                System.out.println("Joins: " + selectQuery.getJoins());
                System.out.println("Where Conditions: " + selectQuery.getWhereConditions());
                System.out.println("Order By: " + selectQuery.getOrderBy());

            } catch (Exception e) {
                System.err.println("Error parsing SQL: " + e.getMessage());
            }
        } else {
            System.out.println("SQL not supported by SelectParser.");
        }
    }
}
```

## Supported Features & Limitations

**Supported Features:**

*   **Basic SELECT statements:** Parsing of standard `SELECT` queries.
*   **Column Extraction:** Identification of selected columns and expressions.
*   **FROM Clause:** Extraction of primary tables in the `FROM` clause.
*   **JOIN Clauses:** Parsing of `INNER`, `LEFT`, `RIGHT`, `FULL` joins with `ON` conditions.
*   **WHERE Clause:** Extraction of filtering conditions.
*   **GROUP BY Clause:** Identification of grouping expressions.
*   **ORDER BY Clause:** Parsing of ordering items, including ASC/DESC direction.

**Limitations:**

*   **`LIMIT` and `OFFSET` Clauses:** Although `SelectQuery` has fields for `limit` and `offset`, the current `SelectParser` implementation does not fully parse these clauses. Future implementation is required to extract these values from the SQL.
*   **Complex Subqueries:** The parser may struggle with deeply nested or complex subqueries in the `SELECT` list or `FROM` clause that are not direct table references.
*   **Window Functions and Analytical Expressions:** There is no explicit support for parsing window functions (e.g., `OVER (PARTITION BY ... ORDER BY ...)`) or analytical expressions.
*   **Advanced `CASE` Statements:** Complex `CASE` statements within the `SELECT` list or conditions may not be fully broken down into their individual components.
