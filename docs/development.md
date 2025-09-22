# Development Guidelines

This document outlines the guidelines and best practices for developing within the TDT SQL Scan project.

## Setting up Your Development Environment

1.  **Prerequisites:**
    *   Java Development Kit (JDK) 8 or higher.
    *   Apache Maven 3.6.0 or higher.
    *   Git.
    *   An Integrated Development Environment (IDE) such as IntelliJ IDEA, Eclipse, or VS Code with Java extensions.

2.  **Cloning the Repository:**
    ```bash
    git clone https://github.com/your-repo/tdt-sql-scan.git
    cd tdt-sql-scan
    ```

3.  **Building the Project:**
    The project uses Maven. You can build the entire project from the root directory:
    ```bash
    mvn clean install
    ```
    This command compiles the code, runs tests, and packages the modules.

4.  **Importing into IDE:**
    Most IDEs can import Maven projects directly.
    *   **IntelliJ IDEA:** Open -> Navigate to the `tdt-sql-scan` directory -> Select `pom.xml` -> Open as Project.
    *   **Eclipse:** File -> Import -> Maven -> Existing Maven Projects -> Browse to `tdt-sql-scan` directory.

## Code Style and Formatting

*   **Java Code:** Adhere to standard Java coding conventions. Use your IDE's auto-formatter with default settings or configure it to match the project's `.editorconfig` file.
*   **SQL:** Follow consistent formatting for SQL queries within the code.
*   **Markdown:** Use clear and concise Markdown for documentation.

## Testing

*   **Unit Tests:** Write unit tests for new features and bug fixes. Place them in the `src/test/java` directory of the respective module.
*   **Running Tests:**
    ```bash
    mvn test
    ```
    To skip tests during build:
    ```bash
    mvn clean install -DskipTests
    ```

## Local Server Setup (for `app-web`)

Para ejecutar la aplicación web localmente, navega al módulo `app-web` y utiliza el siguiente comando de Maven:

```bash
mvn spring-boot:run
```

Esto iniciará el servidor web en el puerto configurado (por defecto, 8080). Podrás acceder a la aplicación en `http://localhost:8080`.

## Guías de Depuración

Para depurar el proyecto, puedes configurar tu IDE para adjuntar un depurador a la aplicación en ejecución. Para la `app-web`, puedes iniciarla en modo depuración directamente desde tu IDE o adjuntar un depurador remoto si la ejecutas a través de Maven con las opciones de depuración adecuadas. Se recomienda configurar puntos de interrupción en las áreas de código relevantes para inspeccionar el flujo de ejecución y los valores de las variables.

## Contributing

1.  Fork the repository.
2.  Create a new branch for your feature or bug fix: `git checkout -b feature/your-feature-name` or `git checkout -b bugfix/issue-description`.
3.  Make your changes, ensuring they adhere to the code style and pass all tests.
4.  Commit your changes with a clear and descriptive commit message.
5.  Push your branch to your fork.
6.  Open a Pull Request to the `main` branch of the original repository.

## Project Structure

The project is organized into several Maven modules, each responsible for a specific part of the system. Refer to the [Project Architecture](architecture.md) and [Module Documentation](modules/README.md) for more details.
