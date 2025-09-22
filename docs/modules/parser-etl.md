# parser-etl Module Documentation

## Purpose

The `parser-etl` module is specifically designed to parse and understand Teradata BTEQ (Basic Teradata Query) scripts. BTEQ scripts often combine SQL statements with BTEQ-specific control commands (e.g., `.LOGON`, `.IF`, `.GOTO`). This module's primary goal is to break down a BTEQ script into its constituent commands, identify embedded SQL statements, and leverage other parser modules to provide a structured representation of the entire script's flow and content.

## Key Components

### Interfaces

*   **`BteqCommand`**
    *   **Description:** A simple interface that all BTEQ command representations must implement. It provides a method to retrieve the raw text of the command.

### Enums

*   **`BteqCommandType`**
    *   **Description:** An enumeration listing various types of BTEQ control commands (e.g., `LOGON`, `SET`, `IF`, `ENDIF`, `GOTO`, `LABEL`, `SQL`). This helps categorize and identify the nature of each BTEQ instruction.

### Classes

*   **`BteqConfigurationCommand`**
    *   **Description:** Represents a collection of BTEQ commands that typically appear at the beginning of a script, setting up the environment (e.g., `.SET`, `.LOGON`, `.DATABASE`). It aggregates multiple `BteqCommand` objects.

*   **`BteqControlCommand`**
    *   **Description:** Represents a single BTEQ control command (e.g., `.LOGON username,password;`). It stores the `BteqCommandType` and the full raw text of the command.

*   **`BteqScript`**
    *   **Description:** The top-level object representing a parsed BTEQ script. It contains a list of `BteqCommand` objects in the order they appear in the script, along with metadata like the script's name, size, and encoding.

*   **`BteqScriptParser`**
    *   **Description:** The core parser for BTEQ scripts. It takes a raw BTEQ script string and a list of generic `QueryParser` instances (from `parser-select`, `parser-ddl`, `parser-dml`, etc.). It tokenizes the script, handles comments, identifies BTEQ control commands, and extracts embedded SQL statements. For each embedded SQL statement, it attempts to parse it using the provided `QueryParser` instances.
    *   **Key Methods:**
        *   `BteqScript parse(String bteqScript, String scriptName)`: Parses the given BTEQ script string into a `BteqScript` object.
        *   `private SQLQuery parseSql(String sql)`: A helper method that iterates through registered `QueryParser` instances to find one that supports and can parse a given SQL string.
        *   `private BteqControlCommand parseBteqControlCommand(String line)`: A helper method to identify and categorize BTEQ control commands from a line of text.

*   **`BteqSqlCommand`**
    *   **Description:** Represents an embedded SQL statement found within a BTEQ script. It stores the raw SQL text and the corresponding parsed `SQLQuery` object (which could be a `SelectQuery`, `CreateTableQuery`, `InsertQuery`, etc., depending on the SQL type).

## Functionality

The `parser-etl` module provides the capability to:

*   Parse entire BTEQ scripts, separating BTEQ control commands from embedded SQL statements.
*   Identify and categorize various BTEQ control commands.
*   Extract raw SQL statements from within BTEQ scripts.
*   Leverage other SQL parser modules (`parser-select`, `parser-ddl`, `parser-dml`) to create structured ASTs for embedded SQL.
*   Represent the sequence of commands and SQL statements in a `BteqScript` object, preserving the script's flow.
*   Handle comments within BTEQ scripts.

## Dependencies

This module depends on the `parser-core` module for the `QueryParser` and `SQLQuery` interfaces/classes. It also implicitly depends on other parser modules (like `parser-select`, `parser-ddl`, `parser-dml`) as it uses their `QueryParser` implementations to parse embedded SQL statements.

## How to Use

To use the `BteqScriptParser`, you would instantiate it with a list of all relevant SQL `QueryParser` implementations and then call its `parse` method.

```java
import com.tdtsqlscan.core.QueryParser;
import com.tdtsqlscan.ddl.CreateTableParser;
import com.tdtsqlscan.ddl.DropTableParser;
import com.tdtsqlscan.dml.InsertParser;
import com.tdtsqlscan.dml.UpdateParser;
import com.tdtsqlscan.dml.DeleteParser;
import com.tdtsqlscan.etl.BteqCommand;
import com.tdtsqlscan.etl.BteqControlCommand;
import com.tdtsqlscan.etl.BteqScript;
import com.tdtsqlscan.etl.BteqScriptParser;
import com.tdtsqlscan.etl.BteqSqlCommand;
import com.tdtsqlscan.select.SelectParser;

import java.util.Arrays;
import java.util.List;

public class BteqParserExample {
    public static void main(String[] args) {
        String bteqScriptContent = """
.LOGON server/user,password;
.SET ERRORLEVEL 3;

SELECT col1, col2 FROM my_table WHERE id = 1;

.IF ACTIVITYCOUNT = 0 THEN .GOTO NO_ROWS;

INSERT INTO another_table (c1) VALUES ('test');

.LABEL NO_ROWS;
.LOGOFF;
""";

        List<QueryParser> sqlParsers = Arrays.asList(
                new SelectParser(),
                new CreateTableParser(),
                new DropTableParser(),
                new InsertParser(),
                new UpdateParser(),
                new DeleteParser()
        );

        BteqScriptParser bteqParser = new BteqScriptParser(sqlParsers);
        BteqScript parsedScript = bteqParser.parse(bteqScriptContent, "example_script.bteq");

        System.out.println("Parsed BTEQ Script: " + parsedScript.getScriptName());
        for (BteqCommand command : parsedScript.getCommands()) {
            if (command instanceof BteqControlCommand) {
                BteqControlCommand ctrlCmd = (BteqControlCommand) command;
                System.out.println("  Control Command: " + ctrlCmd.getType() + " - " + ctrlCmd.getRawText());
            } else if (command instanceof BteqSqlCommand) {
                BteqSqlCommand sqlCmd = (BteqSqlCommand) command;
                System.out.println("  SQL Command: " + sqlCmd.getQuery().getType() + " - " + sqlCmd.getRawText());
            } else {
                System.out.println("  Other Command: " + command.getRawText());
            }
        }
    }
}
```

## Supported Features & Limitations

El módulo `parser-etl` está diseñado para reconocer y procesar los siguientes tipos de comandos BTEQ, categorizados por `BteqCommandType`:

*   `LOGON`: Para iniciar sesión en Teradata.
*   `SET`: Para configurar opciones de sesión BTEQ.
*   `IF`, `ENDIF`: Para control de flujo condicional.
*   `GOTO`, `LABEL`: Para control de flujo incondicional.
*   `SQL`: Representa una sentencia SQL incrustada que será delegada a los `QueryParser` registrados.

**Limitaciones Actuales:**

Actualmente, el módulo puede tratar otros comandos BTEQ como comandos de control genéricos (`BteqControlCommand`) si no están explícitamente mapeados a un `BteqCommandType` específico. Sin embargo, no se realiza un análisis semántico profundo de todos los comandos BTEQ. Las siguientes características o comandos pueden no ser completamente analizados o su impacto en el flujo de datos no se modela explícitamente:

*   **Comandos de Exportación/Importación:** `.EXPORT`, `.IMPORT`.
*   **Bucle y Repetición:** `.REPEAT`.
*   **Ejecución de Archivos:** `.RUN FILE`.
*   **Manejo de Errores BTEQ Avanzado:** Aunque se reconoce `.SET ERRORLEVEL`, la lógica de manejo de errores más compleja dentro de BTEQ (ej. `FAILURE`, `SUCCESS` en `.IF`) puede no ser completamente interpretada para el análisis de flujo.
*   **Variables BTEQ:** El uso y la resolución de variables definidas dentro de scripts BTEQ.

El análisis del flujo de control (`.IF`, `.GOTO`, `.LABEL`) se limita a la identificación de estos comandos; la construcción de un grafo de flujo de control detallado a partir de ellos es una mejora futura potencial.
