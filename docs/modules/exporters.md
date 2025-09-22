# exporters Module Documentation

## Purpose

The `exporters` module is intended to be responsible for converting the in-memory graph representations and analysis results into various output formats. This module would provide functionalities to serialize the parsed SQL structures and their derived graphs into formats suitable for reporting, further processing by other tools, or visualization.

## Current Status

As of the current project structure, the `exporters` module is present as a Maven module (`pom.xml`) but does not yet contain any Java source code or concrete implementations for export functionalities. It serves as a placeholder for future development.

## Planned Functionality

In its complete form, this module is expected to support:

*   **JSON Export:** Converting `Graph` objects, `SQLQuery` objects, and other parsed data into JSON format for API consumption or storage.
*   **XML Export:** Providing XML representations of the parsed data.
*   **CSV Export:** Exporting tabular data (e.g., lists of tables, columns, dependencies) into CSV format.
*   **Image Export:** Potentially integrating with graph visualization libraries to export graphs as image files (e.g., PNG, SVG).
*   **Custom Formats:** Supporting specialized output formats as required by specific integrations or reporting needs.

## Dependencies

Once implemented, this module would likely depend on the `graph` module for accessing `Node`, `Edge`, and `Graph` objects, and potentially on other `parser-*` modules for accessing specific `SQLQuery` types. It might also introduce external dependencies for JSON/XML serialization (e.g., Jackson, JAXB) or image generation libraries.

## How to Use (Future)

Once implemented, users would typically interact with exporter services or classes, providing a parsed `BteqScript` or `Graph` object and specifying the desired output format and destination.

```java
// Conceptual example (future implementation)
// import com.tdtsqlscan.exporters.GraphExporter;
// import com.tdtsqlscan.graph.Graph;

// public class ExportExample {
//     public static void main(String[] args) {
//         Graph myGraph = ...; // Assume a graph has been constructed
//         GraphExporter exporter = new GraphExporter();
//         exporter.exportToJson(myGraph, "./output/graph.json");
//         exporter.exportToPng(myGraph, "./output/graph.png");
//     }
// }
```

## Supported Features & Limitations

**Current Status:**

*   **Supported Features:** As a placeholder module, `exporters` currently does not support any specific features.
*   **Limitations:** The module is not yet implemented and therefore cannot perform any export operations.

**Planned Features:**

*   **JSON Export:** Support for converting `Graph` objects and parsed data into JSON format.
*   **XML Export:** Support for providing XML representations of parsed data.
*   **CSV Export:** Support for exporting tabular data into CSV format.
*   **Image Export:** Integration with graph visualization libraries to export graphs as image files.
*   **Custom Formats:** Support for specialized output formats.

## Consideraciones de Diseño para Extensibilidad

Para facilitar la adición de nuevos formatos de exportación en el futuro, el módulo `exporters` se diseñará con la extensibilidad en mente. Esto probablemente implicará:

*   **Interfaces de Exportación:** Definir interfaces genéricas (ej. `GraphExporter`, `SqlScriptExporter`) que los exportadores específicos de formato implementarán. Esto permitirá un enfoque polimórfico para manejar diferentes tipos de exportación.
*   **Fábricas de Exportadores:** Implementar fábricas o registradores que puedan proporcionar la instancia de exportador adecuada basada en el formato de salida solicitado (ej. `ExporterFactory.getExporter("json")`).
*   **Configuración Flexible:** Permitir que los exportadores sean configurados con opciones específicas (ej. rutas de salida, opciones de formato, inclusión/exclusión de metadatos) a través de parámetros o un objeto de configuración.

Este enfoque asegurará que el módulo pueda crecer y adaptarse a nuevas necesidades de exportación sin requerir cambios significativos en la lógica central.
