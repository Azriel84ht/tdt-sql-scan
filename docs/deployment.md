# Deployment Guide

This guide provides instructions for deploying the TDT SQL Scan application.

## Prerequisites

*   A Java Runtime Environment (JRE) 8 or higher.
*   Docker (if deploying with Docker).
*   Maven (if building from source).

## Building from Source

If you are deploying from source, first build the project:

```bash
cd /path/to/tdt-sql-scan
mvn clean install -DskipTests
```

This will generate JAR files for the `app-cli` and `app-web` modules in their respective `target/` directories.

## Deployment Options

### 1. Deploying the Web Application (`app-web`)

The `app-web` module is a Spring Boot application that can be run as a standalone JAR.

#### Standalone JAR

1.  Navigate to the `app-web` target directory:
    ```bash
    cd app-web/target
    ```
2.  Run the JAR file:
    ```bash
    java -jar app-web-<version>.jar
    ```
    (Replace `<version>` with the actual version number from your `pom.xml`)

    By default, the application will start on port 8080. You can access it in your browser at `http://localhost:8080`.

#### Configuration

Configuration for the web application is managed via `application.properties` or `application-production.properties` files located in `app-web/src/main/resources/`.

You can override properties using command-line arguments:

```bash
java -jar app-web-<version>.jar --server.port=9090
```

Or by providing an external `application.properties` file:

```bash
java -jar app-web-<version>.jar --spring.config.location=file:/path/to/your/application.properties
```

### 2. Deploying the CLI Application (`app-cli`)

The `app-cli` module is a command-line tool.

#### Executable JAR

1.  Navigate to the `app-cli` target directory:
    ```bash
    cd app-cli/target
    ```
2.  Run the JAR file:
    ```bash
    java -jar app-cli-<version>.jar [arguments]
    ```
    (Replace `<version>` with the actual version number)

    Refer to the `app-cli` documentation for available commands and arguments.

### 3. Docker Deployment (Recommended for Web Application)

The project includes a `Dockerfile` for containerizing the web application.

1.  **Build the Docker Image:**
    From the project root directory:
    ```bash
    docker build -t tdt-sql-scan-web .
    ```

2.  **Run the Docker Container:**
    ```bash
    docker run -p 8080:8080 tdt-sql-scan-web
    ```
    The web application will be accessible at `http://localhost:8080`.

#### Docker Configuration

You can pass environment variables to the Docker container to configure the application:

```bash
docker run -p 8080:8080 -e SERVER_PORT=9090 tdt-sql-scan-web
```

## Consideraciones del Entorno de Producción

Esta sección detallará las configuraciones y mejores prácticas para desplegar la aplicación en un entorno de producción. Incluirá temas como la configuración de un proxy inverso (ej. Nginx, Apache), la habilitación de HTTPS, la gestión segura de las conexiones a bases de datos y las recomendaciones de asignación de recursos (CPU, memoria).

## Integración CI/CD

Aquí se describirá cómo el proceso de despliegue puede integrarse en un pipeline de Integración Continua y Despliegue Continuo. Se pueden mencionar herramientas comunes (ej. Jenkins, GitLab CI, GitHub Actions) y los pasos clave para automatizar la construcción, prueba y despliegue de la aplicación.

## Post-Despliegue Steps

*   **Monitoring:** Set up monitoring for your application instances.
*   **Logging:** Ensure logs are being collected and stored appropriately.
*   **Security:** Review and configure security settings as per your organization's policies.
