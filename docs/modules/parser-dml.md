# parser-dml Module Documentation

## Purpose

The `parser-dml` module is dedicated to parsing Data Manipulation Language (DML) statements within the TDT SQL Scan project. It handles SQL commands that modify data within database objects, such as `INSERT`, `UPDATE`, and `DELETE`. This module transforms raw DML strings into structured AST objects, enabling the system to understand data flow, dependencies, and changes.

## Key Components

### Classes

*   **`DeleteParser`**
    *   **Description:** Implements the `QueryParser` interface to specifically handle `DELETE FROM` statements.
    *   **Key Methods:**
        *   `boolean supports(String sql)`: Returns `true` if the SQL string starts with "DELETE FROM".
        *   `SQLQuery parse(String sql)`: Parses the `DELETE` statement, extracting the target table and (conceptually) the `WHERE` clause condition.

*   **`DeleteQuery`**
    *   **Description:** Extends `SQLQuery` and serves as the AST for a `DELETE` statement. It encapsulates the name of the table from which rows are being deleted and the `SQLCondition` representing the `WHERE` clause.
    *   **Fields:** `table` (String), `condition` (SQLCondition).

*   **`InsertParser`**
    *   **Description:** Implements `QueryParser` to parse `INSERT INTO` statements. It supports both `INSERT INTO ... VALUES (...)` and `INSERT INTO ... SELECT (...)` constructs.
    *   **Key Methods:**
        *   `boolean supports(String sql)`: Returns `true` if the SQL string starts with "INSERT INTO".
        *   `InsertQuery parse(String sql)`: Parses the `INSERT` statement. It identifies the target table. If it's an `INSERT ... VALUES` statement, it extracts the column names and the values being inserted. If it's an `INSERT ... SELECT` statement, it delegates the parsing of the sub-`SELECT` query to the `SelectParser` from the `parser-select` module to identify source tables and the full `SelectQuery` AST.

*   **`InsertQuery`**
    *   **Description:** Extends `SQLQuery` and represents the AST for an `INSERT` statement. It stores the target table, and depending on the type of insert, either the list of columns and values, or the list of source tables and the `SelectQuery` object from the embedded `SELECT` statement.
    *   **Fields:** `tableName` (String), `columns` (List<String>), `values` (List<List<String>>), `sourceTables` (List<String>), `selectQuery` (SelectQuery).
    *   **Key Methods:**
        *   `boolean isSelect()`: Returns `true` if the `INSERT` statement is an `INSERT ... SELECT` type.

*   **`UpdateParser`**
    *   **Description:** Implements `QueryParser` to parse `UPDATE` statements.
    *   **Key Methods:**
        *   `boolean supports(String sql)`: Returns `true` if the SQL string starts with "UPDATE".
        *   `SQLQuery parse(String sql)`: Parses the `UPDATE` statement, extracting the target table. It also attempts to identify source tables that might be referenced in `FROM` clauses or subqueries within the `SET` clause.

*   **`UpdateQuery`**
    *   **Description:** Extends `SQLQuery` and represents the AST for an `UPDATE` statement. It encapsulates the name of the table being updated and a list of any identified source tables that contribute data to the update operation.
    *   **Fields:** `targetTable` (String), `sourceTables` (List<String>).

## Functionality

The `parser-dml` module provides the capability to:

*   Identify and parse `DELETE FROM` statements, extracting the target table and `WHERE` conditions.
*   Identify and parse `INSERT INTO` statements, distinguishing between `VALUES` and `SELECT` based inserts.
*   For `INSERT ... VALUES`, extract target columns and the literal values being inserted.
*   For `INSERT ... SELECT`, identify the target table and the source tables involved in the sub-`SELECT` query.
*   Identify and parse `UPDATE` statements, extracting the target table and any source tables referenced in `FROM` or `SET` clauses.

## Dependencies

This module depends on the `parser-core` module for foundational classes (`QueryParser`, `SQLParseException`, `SQLParserUtils`, `SQLQuery`, `SQLCondition`) and the `parser-select` module for parsing sub-queries within `INSERT ... SELECT` statements.

## How to Use

To use the DML parsers, you would typically instantiate the specific parser (e.g., `InsertParser`) and then call its `parse` method with the DML statement.

```java
import com.tdtsqlscan.core.SQLQuery;
import com.tdtsqlscan.dml.InsertParser;
import com.tdtsqlscan.dml.InsertQuery;
import com.tdtsqlscan.dml.UpdateParser;
import com.tdtsqlscan.dml.UpdateQuery;
import com.tdtsqlscan.dml.DeleteParser;
import com.tdtsqlscan.dml.DeleteQuery;

public class DmlExample {
    public static void main(String[] args) {
        String insertValuesSql = "INSERT INTO my_table (col1, col2) VALUES ('val1', 123);";
        InsertParser insertParser = new InsertParser();

        if (insertParser.supports(insertValuesSql)) {
            try {
                InsertQuery insertQuery = (InsertQuery) insertParser.parse(insertValuesSql);
                System.out.println("Insert Table: " + insertQuery.getTableName());
                System.out.println("Columns: " + insertQuery.getColumns());
                System.out.println("Values: " + insertQuery.getValues());
            } catch (Exception e) {
                System.err.println("Error parsing INSERT VALUES: " + e.getMessage());
            }
        }

        String insertSelectSql = "INSERT INTO target_table SELECT colA, colB FROM source_table WHERE colC > 10;";
        if (insertParser.supports(insertSelectSql)) {
            try {
                InsertQuery insertQuery = (InsertQuery) insertParser.parse(insertSelectSql);
                System.out.println("Insert Select Table: " + insertQuery.getTableName());
                System.out.println("Is Select: " + insertQuery.isSelect());
                System.out.println("Source Tables: " + insertQuery.getSourceTables());
                System.out.println("Select Query Columns: " + insertQuery.getSelectQuery().getColumns());
            } catch (Exception e) {
                System.err.println("Error parsing INSERT SELECT: " + e.getMessage());
            }
        }

        String updateSql = "UPDATE my_table SET col1 = 'new_val' WHERE id = 5;";
        UpdateParser updateParser = new UpdateParser();

        if (updateParser.supports(updateSql)) {
            try {
                UpdateQuery updateQuery = (UpdateQuery) updateParser.parse(updateSql);
                System.out.println("Update Target Table: " + updateQuery.getTargetTable());
                System.out.println("Update Source Tables: " + updateQuery.getSourceTables());
            } catch (Exception e) {
                System.err.println("Error parsing UPDATE: " + e.getMessage());
            }
        }

        String deleteSql = "DELETE FROM my_table WHERE status = 'inactive';";
        DeleteParser deleteParser = new DeleteParser();

        if (deleteParser.supports(deleteSql)) {
            try {
                DeleteQuery deleteQuery = (DeleteQuery) deleteParser.parse(deleteSql);
                System.out.println("Delete Table: " + deleteQuery.getTable());
            } catch (Exception e) {
                System.err.println("Error parsing DELETE: " + e.getMessage());
            }
        }
    }
}
```

## Sentencias DML Soportadas y Limitaciones

El módulo `parser-dml` está diseñado para manejar las siguientes sentencias DML:

*   `INSERT INTO ... VALUES`
*   `INSERT INTO ... SELECT`
*   `UPDATE`
*   `DELETE FROM`

**Limitaciones Actuales:**

Actualmente, el módulo no soporta el análisis de otras sentencias DML comunes o características avanzadas como:

*   Sentencias `MERGE`.
*   Subconsultas complejas dentro de las cláusulas `WHERE` o `SET` que no sean manejadas por el `parser-select` subyacente.
*   Sintaxis de `UPDATE` o `DELETE` de múltiples tablas que no sigan un patrón simple de tabla objetivo.
*   Asignaciones detalladas en `UpdateQuery`: Aunque `UpdateParser` identifica la tabla objetivo y las tablas de origen, el objeto `UpdateQuery` actual no expone directamente las asignaciones `SET column = value` de forma estructurada. Esto podría ser una mejora futura utilizando `SQLAssignment` del módulo `parser-core`.
*   Especificidades de Teradata: Algunas construcciones DML específicas de Teradata (ej. `UPDATE FROM`, `INSERT ... WITH`) pueden no ser completamente soportadas o analizadas en detalle.
