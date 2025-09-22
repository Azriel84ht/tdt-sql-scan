# TDT SQL Scan

## Project Description

TDT SQL Scan is a comprehensive tool designed for the in-depth analysis of SQL scripts, with a particular focus on Teradata BTEQ scripts. It helps users understand data flow, identify dependencies, and visualize the relationships within complex SQL codebases. The project offers both a user-friendly web interface for interactive analysis and a robust command-line interface (CLI) for automated processing and integration into CI/CD pipelines.

## Features

*   **Advanced SQL Parsing:** Capable of parsing various SQL dialects, including Data Definition Language (DDL), Data Manipulation Language (DML), SELECT statements, and Teradata BTEQ specific commands.
*   **Data Flow and Dependency Graph Generation:** Automatically generates graphical representations of data lineage and dependencies between tables, views, and other database objects.
*   **Web Application:** Provides an intuitive web interface for uploading SQL/BTEQ scripts, visualizing the generated graphs, and exploring analysis results.
*   **Command-Line Interface (CLI):** Offers a powerful CLI for batch processing, scripting, and integrating SQL analysis into automated workflows.
*   **Modular Architecture:** Designed with a clear separation of concerns, making it extensible and maintainable.

## Technologies Used

*   **Core Language:** Java 8
*   **Build Tool:** Apache Maven
*   **Web Framework:** Spring Boot (for the `app-web` module)
*   **Code Quality:**
    *   Checkstyle (with Google Java Format)
    *   Spotless (for code formatting)

## Project Structure

The project is organized into several modules, each responsible for a specific aspect of the SQL scanning and analysis process:

*   **`parser-core`**: Contains the foundational components for SQL parsing, including common utilities, abstract syntax tree (AST) elements, and basic SQL constructs.
*   **`parser-select`**: Dedicated to parsing `SELECT` statements and extracting relevant information such as columns, tables, joins, and conditions.
*   **`parser-ddl`**: Handles the parsing of Data Definition Language (DDL) statements like `CREATE TABLE`, `DROP TABLE`, and `CREATE INDEX`.
*   **`parser-dml`**: Manages the parsing of Data Manipulation Language (DML) statements, including `INSERT`, `UPDATE`, and `DELETE`.
*   **`parser-etl`**: Specifically designed for parsing ETL-related scripts, such as Teradata BTEQ scripts, to understand their control flow and embedded SQL commands.
*   **`scanner`**: Likely responsible for lexical analysis and tokenization of SQL input before parsing.
*   **`graph`**: Provides the data structures and algorithms for building and manipulating graph representations of SQL dependencies and data flow.
*   **`exporters`**: Contains logic for exporting the analysis results and generated graphs into various formats (e.g., JSON, image files).
*   **`app-cli`**: The command-line interface application, allowing users to perform analysis tasks directly from the terminal.
*   **`app-web`**: The web application that provides a graphical user interface for interacting with the SQL analysis functionalities.

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 8 or higher
*   Apache Maven 3.x

### Building the Project

To build the entire project, navigate to the root directory (`tdt-sql-scan/`) and execute the following Maven command:

```bash
mvn clean install
```

This command will compile all modules, run tests, and package the applications into JAR files in their respective `target/` directories.

### Running the Web Application

After building, you can run the web application from the `app-web` module:

```bash
java -jar app-web/target/app-web-<version>.jar
```

Replace `<version>` with the actual version number (e.g., `0.4.1`). Once started, the web application will typically be accessible in your browser at `http://localhost:8080` (or another port if configured).

### Running the CLI Application

To use the command-line interface, execute the JAR from the `app-cli` module:

```bash
java -jar app-cli/target/app-cli-<version>.jar [command] [options]
```

Replace `<version>` with the actual version number. Refer to the CLI's help output for available commands and options:

```bash
java -jar app-cli/target/app-cli-<version>.jar --help
```

## Usage

### Web Application

1.  Navigate to the web application URL (e.g., `http://localhost:8080`).
2.  Upload your SQL or BTEQ script using the provided interface.
3.  View the generated data flow and dependency graphs.
4.  Explore the analysis results and identified relationships.

### CLI Application

The CLI allows for programmatic analysis. Here are some hypothetical examples (please refer to the actual CLI help for precise commands):

*   **Analyze a single SQL file:**
    ```bash
    java -jar app-cli/target/app-cli-<version>.jar analyze-sql -f /path/to/your/script.sql
    ```
*   **Analyze a BTEQ script and export graph to JSON:**
    ```bash
    java -jar app-cli/target/app-cli-<version>.jar analyze-bteq -f /path/to/your/bteq_script.bteq -o graph.json --format json
    ```
*   **Scan a directory for SQL files and report dependencies:**
    ```bash
    java -jar app-cli/target/app-cli-<version>.jar scan-directory -d /path/to/sql/folder
    ```

## Contributing

We welcome contributions to the TDT SQL Scan project! Please feel free to fork the repository, make your changes, and submit a pull request. Ensure your code adheres to the project's coding standards (Checkstyle and Spotless).

## License

This project is licensed under the [MIT License](LICENSE).