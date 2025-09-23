# Development Guidelines

This document outlines the guidelines for developing within the TDT SQL Scan project using Docker.

## Setting up Your Development Environment with Docker

The entire development environment is orchestrated using Docker Compose, which radically simplifies the setup process.

1.  **Prerequisites:**
    *   **Docker and Docker Compose:** Ensure you have Docker Desktop (for Mac/Windows) or Docker Engine with Docker Compose (for Linux) installed. No other dependencies like Java, Node, or Maven are required on your host machine.

2.  **Cloning the Repository:**
    ```bash
    git clone https://github.com/your-repo/tdt-sql-scan.git
    cd tdt-sql-scan
    ```

3.  **Building and Running the Entire Application:**
    To build the Docker images for all services and start the application stack, run the following command from the root of the project:
    ```bash
    docker-compose up --build
    ```
    This command will:
    - Build the images for each microservice (`config-server`, `user-service`, `parser-service`, `api-gateway`) and the `frontend-spa`.
    - Start a container for each service, including the `postgres` database.
    - Display the aggregated logs from all services in your terminal.

4.  **Stopping the Application:**
    To stop and remove all the running containers, networks, and volumes defined in the `docker-compose.yml`, simply press `Ctrl+C` in the terminal where `docker-compose up` is running, or run the following command from the project root in another terminal:
    ```bash
    docker-compose down
    ```

## Accessing the Services

Once the application is running, you can access the different parts of the system at the following URLs:

*   **API Gateway:** `http://localhost:8080`
*   **Frontend Application:** `http://localhost:5173`
*   **Config Server:** `http://localhost:8888`
*   **User Service:** `http://localhost:8081`
*   **Parser Service:** `http://localhost:8082`

## Code Style and Formatting

*   **Java Code:** Adhere to standard Java coding conventions. Use your IDE's auto-formatter with default settings or configure it to match the project's `.editorconfig` file.
*   **SQL:** Follow consistent formatting for SQL queries within the code.
*   **Markdown:** Use clear and concise Markdown for documentation.

## Testing

While the Docker Compose setup does not run tests by default, you can still run them within the individual service containers or locally if you choose to set up a local Java/Maven environment.

To run tests for a specific service (e.g., `user-service`):
```bash
docker-compose run user-service mvn test
```

## Contributing

1.  Fork the repository.
2.  Create a new branch for your feature or bug fix: `git checkout -b feature/your-feature-name` or `git checkout -b bugfix/issue-description`.
3.  Make your changes, ensuring they adhere to the code style and pass all tests.
4.  Commit your changes with a clear and descriptive commit message.
5.  Push your branch to your fork.
6.  Open a Pull Request to the `main` branch of the original repository.

## Project Structure

The project is organized into several Maven modules, each responsible for a specific part of the system. Refer to the [Project Architecture](architecture.md) and [Module Documentation](modules/README.md) for more details.
