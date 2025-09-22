# Troubleshooting Guide

This guide provides solutions to common issues encountered while developing, building, or running the TDT SQL Scan project.

## General Issues

### Maven Build Failures

**Problem:** `mvn clean install` fails with compilation errors or dependency issues.

**Solution:**
1.  **Clean Maven Cache:** Sometimes, corrupted local Maven repositories can cause issues. Try cleaning your local repository:
    ```bash
    rm -rf ~/.m2/repository
    ```
    Then try building again.
2.  **Update Maven:** Ensure you are using a recent version of Maven (3.6.0 or higher).
3.  **Check Internet Connection:** Dependency downloads require an active internet connection.
4.  **Review Error Messages:** Carefully read the error messages in the console. They often point to the specific problem (e.g., missing dependency, compilation error in a specific file).

### "java.lang.OutOfMemoryError: Java heap space"

**Problem:** The application or Maven build crashes with an OutOfMemoryError.

**Solution:** Increase the maximum heap space for the Java Virtual Machine (JVM).

*   **For Maven:** Set the `MAVEN_OPTS` environment variable:
    ```bash
    export MAVEN_OPTS="-Xmx2G -XX:MaxPermSize=512M"
    # Then run your Maven command
    mvn clean install
    ```
*   **For Running the Application (e.g., app-web):**
    ```bash
    java -Xmx2G -jar app-web-<version>.jar
    ```
    Adjust `2G` (2 Gigabytes) as needed based on your system's memory and application requirements.

## Web Application (`app-web`) Issues

### Application Fails to Start

**Problem:** The `app-web` application does not start, or logs show errors during startup.

**Solution:**
1.  **Check Logs:** Examine the console output or application logs (`app-web/logs/` if configured) for error messages. Common issues include:
    *   **Port Already in Use:** If another application is using port 8080 (or the configured port), the application will fail to bind. Change the port in `application.properties` or via command-line argument (`--server.port=XXXX`).
    *   **Database Connection Issues:** If the application relies on a database, ensure the database is running and accessible, and that connection properties in `application.properties` are correct.
    *   **Configuration Errors:** Incorrect or missing configuration properties can prevent startup.

### Static Resources (CSS/JS/Images) Not Loading

**Problem:** The web interface loads, but styling, JavaScript functionality, or images are missing.

**Solution:**
1.  **Clear Browser Cache:** Your browser might be serving old cached resources.
2.  **Verify Paths:** Ensure that the paths to CSS, JavaScript, and image files in your HTML templates are correct and relative to the `static` directory (`app-web/src/main/resources/static`).
3.  **Check Server Logs:** Look for 404 (Not Found) errors in the server logs, which indicate that the application cannot find the requested resources.

## CLI Application (`app-cli`) Issues

### Command Not Found or Incorrect Usage

**Problem:** When running `java -jar app-cli-<version>.jar`, the command is not recognized or arguments are misinterpreted.

**Solution:**
1.  **Check `app-cli` Documentation:** Refer to the specific documentation for `app-cli` (once available) to understand the correct command syntax and available options.
2.  **Verify JAR Integrity:** Ensure the JAR file was built correctly and is not corrupted.

## Resolución de Problemas Específicos de Bases de Datos

Esta sección abordará problemas comunes relacionados con la conexión y operación de bases de datos. Incluirá:

*   **Problemas de Conexión:** Credenciales incorrectas, base de datos no iniciada, problemas de red o firewall.
*   **Errores de Esquema:** Discrepancias entre el esquema esperado por la aplicación y el esquema real de la base de datos.
*   **Problemas de Rendimiento de Consultas:** Cómo identificar y optimizar consultas lentas.

## Niveles de Logging y Salida de Depuración

Para diagnosticar problemas más complejos, es útil ajustar los niveles de logging de la aplicación. Esta sección explicará cómo configurar los niveles de logging (ej. `DEBUG`, `TRACE`) en `application.properties` o mediante argumentos de línea de comandos para obtener información más detallada sobre el comportamiento interno de la aplicación.

## Reporting Issues

If you encounter an issue not covered here, please:

1.  **Search Existing Issues:** Check the project's issue tracker (e.g., GitHub Issues) to see if the problem has already been reported.
2.  **Provide Details:** When reporting a new issue, include:
    *   A clear and concise description of the problem.
    *   Steps to reproduce the issue.
    *   Any error messages or stack traces.
    *   Your operating system and Java version.
    *   The version of the TDT SQL Scan project you are using.
