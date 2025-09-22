# app-cli Module Documentation

## Purpose

The `app-cli` module is designed to provide a Command-Line Interface (CLI) for interacting with the TDT SQL Scan project's core functionalities. Its primary purpose is to allow users to execute analysis tasks programmatically, making it suitable for batch processing, scripting, and integration into automated workflows. This would include tasks such as parsing SQL/BTEQ scripts, generating data lineage graphs, and exporting results without requiring a graphical user interface.

## Current Status

As of the current project structure, the `app-cli` module is present as a Maven module (`pom.xml`) but does not yet contain any Java source code or concrete implementations for CLI functionalities. It serves as a placeholder for future development.

## Intended Functionality (Future Development)

In its complete form, this module is expected to provide:

*   **Script Parsing:** Commands to parse individual SQL or BTEQ files and display a summary of identified statements and objects.
*   **Graph Generation:** Commands to generate data flow or control flow graphs from parsed scripts, potentially outputting them in various formats (e.g., JSON, DOT, image files).
*   **Data Lineage:** Commands to analyze and report on data lineage, showing dependencies between tables and columns.
*   **Configuration:** Options to configure parsing rules, output formats, and other operational parameters via command-line arguments or configuration files.
*   **Batch Processing:** The ability to process multiple files or directories of scripts.

## Dependencies

Once implemented, this module would heavily depend on the `parser-core`, `parser-select`, `parser-ddl`, `parser-dml`, `parser-etl`, and `graph` modules to perform its core analysis tasks. It would also likely depend on the `exporters` module for outputting results in various formats.

## How to Use (Future)

Once implemented, users would execute the CLI application from their terminal, providing commands and arguments.

```bash
# Conceptual examples (future implementation)
# java -jar app-cli-<version>.jar parse --file my_script.bteq
# java -jar app-cli-<version>.jar analyze-lineage --dir ./sql_scripts --output json > lineage.json
# java -jar app-cli-<version>.jar generate-graph --file complex_etl.bteq --format png --output graph.png
```

## Consideraciones de Diseño para la Implementación

Para la implementación del módulo `app-cli`, se tendrán en cuenta las siguientes consideraciones de diseño:

*   **Framework CLI:** Se evaluarán frameworks como Picocli o JCommander para construir una interfaz de línea de comandos robusta y fácil de usar, que permita la definición declarativa de comandos, subcomandos y opciones.
*   **Manejo de Errores y Códigos de Salida:** La aplicación CLI manejará los errores de forma elegante, proporcionando mensajes claros al usuario y utilizando códigos de salida estándar (ej. `0` para éxito, `1` para errores generales, etc.) para facilitar la integración en scripts.
*   **Verbosidad y Logging:** Se ofrecerán opciones para controlar la verbosidad de la salida (ej. `--verbose`, `--quiet`) y la dirección de los logs (consola, archivo), permitiendo a los usuarios ajustar el nivel de detalle según sus necesidades.
*   **Opciones de Entrada/Salida:** Se proporcionarán opciones flexibles para especificar archivos de entrada (individuales, directorios, búsqueda recursiva) y formatos de salida, aprovechando el módulo `exporters` para generar resultados en diversos formatos.
*   **Seguridad:** Si la CLI requiere el manejo de información sensible (ej. credenciales de base de datos), se implementarán mecanismos seguros para su entrada y gestión, evitando exponerla en el historial de comandos o en archivos de configuración no protegidos.
