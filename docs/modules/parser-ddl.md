# parser-ddl Module Documentation

## Purpose

The `parser-ddl` module is dedicated to parsing Data Definition Language (DDL) statements within the TDT SQL Scan project. It handles SQL commands that define or modify the structure of database objects, such as tables and indexes. This module transforms raw DDL strings into structured AST objects, enabling the system to understand schema changes and dependencies.

## Key Components

### Classes

*   **`ColumnDefinition`**
    *   **Description:** A simple data class representing a column's definition within a `CREATE TABLE` statement. It stores the column's name and its data type.
    *   **Key Methods:**
        *   `static ColumnDefinition from(String raw)`: A factory method that parses a raw column definition string (e.g., "column_name VARCHAR(100)") into a `ColumnDefinition` object.

*   **`CreateIndexParser`**
    *   **Description:** Implements the `QueryParser` interface to specifically handle `CREATE [UNIQUE] INDEX` statements.
    *   **Key Methods:**
        *   `boolean supports(String sql)`: Returns `true` if the SQL string contains "CREATE" and "INDEX" keywords.
        *   `CreateIndexQuery parse(String sql)`: Parses the `CREATE INDEX` statement, extracting the index name, the table it's created on, and the columns included in the index.

*   **`CreateIndexQuery`**
    *   **Description:** Extends `SQLQuery` and serves as the AST for a `CREATE INDEX` statement. It encapsulates details such as whether the index is unique, its name, the table it belongs to, and the list of columns.
    *   **Fields:** `unique` (boolean), `indexName` (String), `tableName` (String), `columns` (List<String>).

*   **`CreateTableParser`**
    *   **Description:** Implements `QueryParser` to parse `CREATE TABLE` statements. This parser is capable of handling both standard `CREATE TABLE (column_definitions...)` and `CREATE TABLE AS SELECT` (CTAS) statements, including `VOLATILE` tables.
    *   **Key Methods:**
        *   `boolean supports(String sql)`: Uses a regular expression to identify `CREATE TABLE` statements.
        *   `CreateTableQuery parse(String sql)`: Parses the `CREATE TABLE` statement. For standard tables, it extracts column definitions. For CTAS statements, it leverages the `SelectParser` from the `parser-select` module to parse the sub-query and identify source tables.

*   **`CreateTableQuery`**
    *   **Description:** Extends `SQLQuery` and represents the AST for a `CREATE TABLE` statement. It stores the table name, a list of `ColumnDefinition` objects, a flag for `VOLATILE` tables, and for CTAS, a list of source tables and the embedded `SelectQuery` object.
    *   **Fields:** `tableName` (String), `columns` (List<ColumnDefinition>), `isVolatile` (boolean), `sourceTables` (List<String>), `selectQuery` (SelectQuery).

*   **`DropTableParser`**
    *   **Description:** Implements `QueryParser` to parse `DROP TABLE` statements.
    *   **Key Methods:**
        *   `boolean supports(String sql)`: Returns `true` if the SQL string starts with "DROP TABLE".
        *   `DropTableQuery parse(String sql)`: Parses the `DROP TABLE` statement, extracting the name of the table to be dropped.

*   **`DropTableQuery`**
    *   **Description:** Extends `SQLQuery` and represents the AST for a `DROP TABLE` statement. It primarily stores the name of the table to be dropped.
    *   **Fields:** `tableName` (String).

## Functionality

The `parser-ddl` module provides the capability to:

*   Identify and parse `CREATE TABLE` statements, including `VOLATILE` tables and `CREATE TABLE AS SELECT` (CTAS) constructs.
*   Extract column names and types from `CREATE TABLE` definitions.
*   Identify source tables involved in CTAS operations.
*   Identify and parse `CREATE [UNIQUE] INDEX` statements, extracting index names, target tables, and indexed columns.
*   Identify and parse `DROP TABLE` statements, extracting the name of the table to be dropped.

## Dependencies

This module depends on the `parser-core` module for foundational classes (`QueryParser`, `SQLParseException`, `SQLParserUtils`, `SQLQuery`) and the `parser-select` module for parsing sub-queries within CTAS statements.

## How to Use

To use the DDL parsers, you would instantiate the specific parser (e.g., `CreateTableParser`) and call its `parse` method.

```java
import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.ddl.CreateTableParser;
import com.tdtsqlscan.ddl.CreateTableQuery;
import com.tdtsqlscan.ddl.DropTableParser;
import com.tdtsqlscan.ddl.DropTableQuery;

public class DdlExample {
    public static void main(String[] args) {
        String createSql = "CREATE VOLATILE TABLE my_new_table (id INT, name VARCHAR(100)) ON COMMIT PRESERVE ROWS;";
        CreateTableParser createParser = new CreateTableParser();

        if (createParser.supports(createSql)) {
            try {
                CreateTableQuery createQuery = (CreateTableQuery) createParser.parse(createSql);
                System.out.println("Created Table: " + createQuery.getTableName());
                System.out.println("Volatile: " + createQuery.isVolatile());
                System.out.println("Columns: " + createQuery.getColumns().size());
            } catch (Exception e) {
                System.err.println("Error parsing CREATE TABLE: " + e.getMessage());
            }
        }

        String dropSql = "DROP TABLE old_table;";
        DropTableParser dropParser = new DropTableParser();

        if (dropParser.supports(dropSql)) {
            try {
                DropTableQuery dropQuery = (DropTableQuery) dropParser.parse(dropSql);
                System.out.println("Dropped Table: " + dropQuery.getTableName());
            } catch (Exception e) {
                System.err.println("Error parsing DROP TABLE: " + e.getMessage());
            }
        }

        String ctasSql = "CREATE TABLE sales_summary AS (SELECT region, SUM(amount) AS total_sales FROM sales GROUP BY region);";
        if (createParser.supports(ctasSql)) {
            try {
                CreateTableQuery ctasQuery = (CreateTableQuery) createParser.parse(ctasSql);
                System.out.println("CTAS Table: " + ctasQuery.getTableName());
                System.out.println("Source Tables (from SELECT): " + ctasQuery.getSourceTables());
                System.out.println("Select Query Columns: " + ctasQuery.getSelectQuery().getColumns());
            } catch (Exception e) {
                System.err.println("Error parsing CTAS: " + e.getMessage());
            }
        }
    }
}
```

## Sentencias DDL Soportadas y Limitaciones

El módulo `parser-ddl` está diseñado para manejar las siguientes sentencias DDL:

*   `CREATE TABLE` (incluyendo tablas `VOLATILE` y `CREATE TABLE AS SELECT` - CTAS)
*   `CREATE [UNIQUE] INDEX`
*   `DROP TABLE`

**Limitaciones Actuales:**

Actualmente, el módulo no soporta el análisis de otras sentencias DDL comunes como:

*   `ALTER TABLE` (añadir/modificar/eliminar columnas, cambiar propiedades de tabla)
*   `TRUNCATE TABLE`
*   `CREATE VIEW`
*   `CREATE DATABASE` o `CREATE SCHEMA`
*   Cualquier otra sentencia DDL no mencionada explícitamente arriba.

Además, las definiciones de columnas complejas con múltiples restricciones o valores por defecto avanzados pueden no ser completamente desglosadas. Las especificidades de Teradata, como `WITH DATA`, `NO PRIMARY INDEX`, `PARTITION BY`, etc., son parcialmente manejadas en el contexto de `CREATE TABLE` pero no se analizan en detalle como componentes AST separados a menos que se indique lo contrario en la documentación de `CreateTableQuery`.
