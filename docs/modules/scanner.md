# scanner Module Documentation (Conceptual)

## Purpose

In the context of compiler design, a "scanner" or "lexical analyzer" is responsible for breaking down a stream of characters (like an SQL script) into a stream of tokens. These tokens are the smallest meaningful units in the language (e.g., keywords, identifiers, operators, literals).

While the TDT SQL Scan project's architecture document mentions a `scanner` as a core component, it is not implemented as a standalone Maven module or a distinct class named `*Scanner.java`.

Instead, the lexical analysis and tokenization functionalities are integrated into the `parser-core` module, primarily through the `SQLParserUtils` utility class. This class provides a set of static methods that perform the necessary operations to identify and extract meaningful segments (tokens) from raw SQL strings, which are then used by the various SQL parsers.

## Key Components

The conceptual `scanner` functionality is primarily provided by the utility class `com.tdtsqlscan.core.SQLParserUtils` within the `parser-core` module. This class encapsulates the methods responsible for lexical analysis and tokenization.

## Functionality (Provided by `parser-core/SQLParserUtils`)

The lexical analysis capabilities are provided by the `com.tdtsqlscan.core.SQLParserUtils` class, which includes methods such as:

*   `extractBetweenKeywords(String sql, String startKeyword, String endKeyword)`: Extracts content between specified keywords, handling nested structures.
*   `extractAfterKeyword(String sql, String keyword, String endKeyword)`: Extracts content after a keyword up to an optional end keyword.
*   `splitTopLevel(String input, String delimiter)`: Splits a string by a delimiter, respecting parentheses to avoid splitting within nested expressions.
*   `findTopLevelKeyword(String sql, String keyword, int startIndex)`: Finds the first occurrence of a keyword at the top level (outside parentheses).
*   `getFirstWord(String s)`: Extracts the first word from a string, cleaning trailing punctuation.
*   `extractTableFromExpression(String expression)`: Extracts the base table name from an expression that might include aliases.
*   `extractTableName(String sql, String keyword)`: Extracts a table name following a specific keyword.

These methods collectively perform the tokenization and initial structural breakdown that a dedicated scanner module would typically provide. They are fundamental for the subsequent parsing stages in modules like `parser-select`, `parser-ddl`, and `parser-dml`.

## Dependencies

The conceptual `scanner` functionality is part of the `parser-core` module and thus has no external dependencies beyond standard Java libraries.

## How to Use

The functionalities of the conceptual `scanner` are implicitly used by all `QueryParser` implementations across the `parser-*` modules. Developers do not directly interact with a `scanner` component but rather utilize the `SQLParserUtils` methods within their parser implementations.

## Supported Features & Limitations

**Supported Features:**

*   **Keyword-based Extraction:** Ability to extract content between or after specific keywords, handling nested parentheses.
*   **Top-Level Keyword Identification:** Can find keywords that are not nested within parentheses.
*   **Table Name Extraction:** Utility methods for extracting table names from expressions and after specific keywords.
*   **String Splitting:** Splits strings by delimiters while respecting nested structures.

**Limitations:**

*   **Not a Full Lexical Analyzer:** This is not a traditional, standalone lexical analyzer that generates a stream of tokens based on a grammar. It's a collection of utility methods for string manipulation and keyword-based extraction.
*   **Limited Error Recovery:** Error handling is primarily through exceptions for malformed input, rather than robust error recovery mechanisms typical of a dedicated scanner.
*   **SQL Dialect Specificity:** While designed for SQL, its keyword-based approach might require adjustments for highly divergent SQL dialects.
*   **No Explicit Token Stream:** Does not produce an explicit, iterable token stream, which might limit certain advanced parsing techniques.

## Justificación de la Integración

La decisión de integrar la funcionalidad de escaneo directamente en el módulo `parser-core` a través de `SQLParserUtils` se tomó por varias razones:

*   **Simplicidad y Cohesión:** Para un proyecto de este tamaño y alcance inicial, mantener la lógica de escaneo y las utilidades básicas de parsing en un solo lugar reduce la sobrecarga de gestión de módulos y promueve una mayor cohesión entre las operaciones léxicas y sintácticas fundamentales.
*   **Evitar la Duplicación:** Las operaciones de tokenización y extracción de subcadenas son a menudo muy específicas del contexto del parsing SQL. Al centralizarlas, se evita la duplicación de lógica en múltiples módulos de parser.
*   **Rendimiento:** La integración directa puede ofrecer ligeras ventajas de rendimiento al reducir la sobrecarga de llamadas entre módulos o la creación de objetos intermedios que un escáner independiente podría generar.

Si en el futuro el proyecto requiriera un análisis léxico mucho más complejo (ej. soporte para múltiples dialectos SQL con reglas de tokenización muy diferentes, o la necesidad de construir un árbol de tokens explícito), se podría considerar la extracción de un módulo `scanner` dedicado.
