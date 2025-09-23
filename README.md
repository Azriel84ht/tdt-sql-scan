# TDT SQL Scan

## Project Description

TDT SQL Scan is a comprehensive tool designed for the in-depth analysis of SQL scripts, with a particular focus on Teradata BTEQ scripts. It helps users understand data flow, identify dependencies, and visualize the relationships within complex SQL codebases. The project offers both a user-friendly web interface for interactive analysis and a robust command-line interface (CLI) for automated processing and integration into CI/CD pipelines.

## Features

*   **Advanced SQL Parsing:** A microservice-based architecture for parsing various SQL dialects, including DDL, DML, SELECT statements, and Teradata BTEQ commands.
*   **Data Flow and Dependency Graph Generation:** The frontend SPA, powered by React, now handles the generation and visualization of data lineage and dependency graphs.
*   **React-based Single Page Application (SPA):** A modern, intuitive user interface for uploading SQL/BTEQ scripts, visualizing graphs, and exploring analysis results.
*   **Command-Line Interface (CLI):** Offers a powerful CLI for batch processing, scripting, and integrating SQL analysis into automated workflows.
*   **Microservices Architecture:** The backend functionality is decomposed into independent services for parsing, user management, and API gateway routing, enhancing scalability and maintainability.

## Technologies Used

*   **Core Language:** Java 17
*   **Backend Framework:** Spring Boot 3
*   **Frontend Framework:** React
*   **Build Tool:** Apache Maven
*   **Code Quality:**
    *   Checkstyle (with Google Java Format)
    *   Spotless (for code formatting)

## Project Structure

The project has been migrated to a microservices architecture:

*   **`parser-service`**: A dedicated service for parsing all types of SQL and BTEQ scripts.
*   **`user-service`**: Manages user authentication and authorization.
*   **`api-gateway`**: A single entry point for all client requests, routing them to the appropriate backend service.
*   **`config-server`**: Centralized configuration management for all microservices.
*   **`frontend-spa`**: A Single Page Application built with React that provides the user interface.
*   **`app-cli`**: The command-line interface application for terminal-based analysis.

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Apache Maven 3.x
*   Node.js and npm (for the frontend-spa)

### Building the Project

To build the entire project, navigate to the root directory and execute the following Maven command:

```bash
mvn clean install
```

This command will compile all backend modules and package them into JAR files.

### Running the Ecosystem

To run the full application, you need to start the microservices and the frontend SPA. Refer to the `docs/deployment.md` for detailed instructions on running the services.

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

### Web Application (SPA)

1.  Ensure the backend services and the `frontend-spa` are running.
2.  Navigate to the frontend application URL (typically `http://localhost:5173`).
3.  Upload your SQL or BTEQ script using the interface.
4.  View the generated data flow and dependency graphs.

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