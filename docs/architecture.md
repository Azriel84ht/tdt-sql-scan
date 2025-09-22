# Project Architecture

The TDT SQL Scan project is designed with a modular and layered architecture to facilitate robust SQL analysis, maintainability, and extensibility. Its primary goal is to parse various SQL dialects, particularly Teradata BTEQ scripts, to build a comprehensive understanding of data flow and dependencies, which can then be visualized or exported.

## Modular Design Principles

The project is structured into several independent Maven modules. This modularity promotes:

*   **Separation of Concerns:** Each module focuses on a specific set of functionalities, reducing complexity.
*   **Reusability:** Core parsing and graph components can be reused across different applications (CLI, Web).
*   **Maintainability:** Changes in one module are less likely to impact others, simplifying development and debugging.
*   **Scalability:** Individual components can be developed and tested in isolation.

## Core Components and Layers

The architecture can be broadly categorized into the following logical layers:

### 1. Parsing Layer

This layer is responsible for the lexical analysis and syntactic parsing of SQL and BTEQ scripts. It transforms raw script text into structured, in-memory representations.

*   **`scanner`**: Acts as the lexical analyzer, breaking down the input script into a stream of tokens. While not explicitly detailed in the initial structure, it's a fundamental component for any parser.
*   **`parser-core`**: Provides the foundational elements for all parsers, including common SQL constructs, utility functions, and potentially the base Abstract Syntax Tree (AST) nodes.
*   **`parser-select`**: Specializes in parsing `SELECT` statements, extracting clauses like `FROM`, `WHERE`, `GROUP BY`, `ORDER BY`, and identifying tables, columns, and joins.
*   **`parser-ddl`**: Handles Data Definition Language (DDL) statements such as `CREATE TABLE`, `DROP TABLE`, `CREATE INDEX`, capturing schema modifications.
*   **`parser-dml`**: Focuses on Data Manipulation Language (DML) statements like `INSERT`, `UPDATE`, and `DELETE`, identifying affected tables and data operations.
*   **`parser-etl`**: Specifically designed to parse ETL scripts, particularly Teradata BTEQ. It understands BTEQ commands, control flow, and extracts embedded SQL statements for further analysis by other parsers.

### 2. Graph Representation Layer

Once the SQL scripts are parsed, their inherent relationships and data flow are modeled using graph data structures.

*   **`graph`**: This module defines the core graph data structures (`Node`, `Edge`, `Graph`) used to represent the dependencies and data lineage extracted from the SQL scripts. Nodes typically represent tables, columns, or processes, while edges represent data flow or relationships.

### 3. Application Layers

These layers provide the user interfaces for interacting with the core parsing and graph functionalities.

*   **`app-cli`**: The Command-Line Interface application. It allows users to execute analysis tasks programmatically, making it suitable for batch processing, scripting, and integration into automated workflows.
*   **`app-web`**: The Web Application. It offers a graphical user interface for interactive analysis, allowing users to upload scripts, visualize generated graphs, and explore results through a web browser.

### 4. Utility and Output Layer

This layer handles the generation of various output formats from the analyzed data.

*   **`exporters`**: Responsible for converting the in-memory graph representations and analysis results into different output formats, such as JSON, XML, or potentially image formats for graph visualization.

## Data Flow Overview

A typical data flow within the TDT SQL Scan system would involve:

1.  **Input:** A user provides an SQL or BTEQ script via the `app-cli` or `app-web`.
2.  **Scanning & Parsing:** The script is fed through the `scanner` and then processed by the relevant parsers (`parser-etl`, `parser-select`, `parser-ddl`, `parser-dml`).
3.  **Graph Construction:** The parsed information is used to build and populate a `Graph` structure, capturing relationships and data movements.
4.  **Analysis & Output:** The constructed graph can then be analyzed by the application layers, or transformed into various output formats by the `exporters` module for reporting or further processing.

## Key Technologies

*   **Java 8:** The primary programming language, chosen for its robustness, ecosystem, and performance.
*   **Apache Maven:** Used for project management, dependency management, and the build lifecycle, enforcing the modular structure.
*   **Spring Boot:** Utilized by the `app-web` module to rapidly develop and deploy the web application, leveraging its convention-over-configuration approach.

## Manejo de Errores y Resiliencia

Esta sección describirá cómo la arquitectura del proyecto maneja los errores en las diferentes capas (parsing, graph, aplicación) y las estrategias implementadas para asegurar la resiliencia del sistema. Se detallarán los mecanismos de logging, la propagación de excepciones y las políticas de reintento o fallback, si las hubiera.

## Aspectos de Seguridad

Dada la presencia de una aplicación web (`app-web`), esta sección abordará las consideraciones de seguridad a nivel arquitectónico. Incluirá detalles sobre la validación de entradas, la autenticación y autorización de usuarios, la protección contra vulnerabilidades comunes (ej. inyección SQL, XSS), y la gestión de secretos.

## Estrategia de Pruebas

Esta sección explicará el enfoque de pruebas adoptado para el proyecto. Se detallarán los tipos de pruebas (unitarias, de integración, funcionales, etc.), las herramientas utilizadas y cómo se garantiza la cobertura y la calidad del código en cada módulo. Se describirá cómo las pruebas contribuyen a la estabilidad y fiabilidad de la arquitectura modular.
