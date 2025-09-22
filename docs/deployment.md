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

Para desplegar la aplicación TDT SQL Scan en un entorno de producción, es crucial configurar adecuadamente los parámetros sensibles y específicos del entorno. La aplicación está diseñada para leer configuraciones desde `application-production.properties` y, preferentemente, desde variables de entorno, lo que permite una gestión segura y flexible sin modificar el código o los archivos de configuración directamente en el contenedor o servidor.

### Configuración de Base de Datos (PostgreSQL)

En producción, se recomienda encarecidamente utilizar una base de datos PostgreSQL externa en lugar de la base de datos H2 en memoria utilizada para desarrollo. La configuración se realiza a través de las siguientes propiedades de Spring Boot, que pueden ser sobrescritas mediante variables de entorno:

*   **`spring.datasource.url`**: La URL de conexión a la base de datos PostgreSQL. Ejemplo:
    `jdbc:postgresql://your_db_host:5432/your_db_name`
*   **`spring.datasource.username`**: El nombre de usuario para la conexión a la base de datos.
*   **`spring.datasource.password`**: La contraseña para el usuario de la base de datos.
*   **`spring.jpa.hibernate.ddl-auto`**: Se recomienda establecerlo en `validate` o `none` en producción para evitar la creación o modificación automática del esquema de la base de datos por parte de Hibernate. La gestión del esquema debe realizarse mediante migraciones controladas (ej. Flyway, Liquibase).

**Ejemplo de configuración con variables de entorno (para Docker o sistema operativo):**

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/tdtsqlscan_prod"
export SPRING_DATASOURCE_USERNAME="tdtuser"
export SPRING_DATASOURCE_PASSWORD="your_secure_password"
export SPRING_JPA_HIBERNATE_DDL_AUTO="validate"
```

### Configuración del Servidor de Correo Electrónico

La aplicación puede requerir un servidor de correo para funcionalidades como la verificación de correo electrónico o la recuperación de contraseña. La configuración se realiza a través de las propiedades de Spring Mail, también sobrescribibles por variables de entorno:

*   **`spring.mail.host`**: El host del servidor SMTP.
*   **`spring.mail.port`**: El puerto del servidor SMTP (ej. 587 para TLS).
*   **`spring.mail.username`**: El nombre de usuario para autenticarse en el servidor SMTP.
*   **`spring.mail.password`**: La contraseña para el usuario SMTP.
*   **`spring.mail.properties.mail.smtp.auth`**: `true` si el servidor SMTP requiere autenticación.
*   **`spring.mail.properties.mail.smtp.starttls.enable`**: `true` para habilitar STARTTLS.

**Ejemplo de configuración con variables de entorno:**

```bash
export SPRING_MAIL_HOST="smtp.your-email-provider.com"
export SPRING_MAIL_PORT="587"
export SPRING_MAIL_USERNAME="your_email@example.com"
export SPRING_MAIL_PASSWORD="your_email_password"
export SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH="true"
export SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE="true"
```

### Otras Consideraciones

*   **Proxy Inverso**: Se recomienda colocar la aplicación detrás de un proxy inverso (ej. Nginx, Apache HTTP Server) para manejar la terminación SSL/TLS, el balanceo de carga y servir recursos estáticos de manera más eficiente.
*   **HTTPS**: Asegurar que todo el tráfico hacia y desde la aplicación se realice a través de HTTPS para proteger los datos en tránsito.
*   **Asignación de Recursos**: Monitorear y ajustar la asignación de CPU y memoria a la JVM de la aplicación según la carga de trabajo esperada.
*   **Logging**: Configurar el logging para que los logs se escriban en un sistema de gestión de logs centralizado (ej. ELK stack, Splunk) en lugar de solo en el disco local del servidor.

## Integración CI/CD

La automatización del proceso de construcción, prueba y despliegue es fundamental para garantizar la entrega continua y la calidad del software. A continuación, se presenta un ejemplo básico de un pipeline de CI/CD utilizando GitHub Actions para construir la imagen Docker de la aplicación web y publicarla en un registro de contenedores.

### Ejemplo de GitHub Actions para Docker Build y Push

Este flujo de trabajo (`.github/workflows/docker-publish.yml`) se activará en cada push a la rama `main` y realizará los siguientes pasos:

1.  **Checkout del Código**: Obtiene la última versión del código fuente.
2.  **Configuración de Java**: Establece el entorno Java necesario.
3.  **Login en Docker Hub**: Autentica en Docker Hub utilizando secretos de GitHub para el nombre de usuario y el token de acceso personal.
4.  **Construcción de la Imagen Docker**: Construye la imagen Docker de la aplicación `app-web`.
5.  **Publicación de la Imagen Docker**: Sube la imagen construida a Docker Hub.

```yaml
name: Docker Image CI

on: 
  push:
    branches:
      - main

env:
  DOCKER_IMAGE_NAME: tdtsqlscan-web
  DOCKER_REGISTRY: docker.io/your_dockerhub_username # Reemplaza con tu usuario de Docker Hub

jobs:
  build-and-push-docker-image:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Log in to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Build Docker image
        run: docker build -t ${{ env.DOCKER_REGISTRY }}/${{ env.DOCKER_IMAGE_NAME }}:latest -f Dockerfile .

      - name: Push Docker image
        run: docker push ${{ env.DOCKER_REGISTRY }}/${{ env.DOCKER_IMAGE_NAME }}:latest

      - name: Clean up Docker images
        run: docker rmi ${{ env.DOCKER_REGISTRY }}/${{ env.DOCKER_IMAGE_NAME }}:latest
```

**Notas Importantes:**

*   **`your_dockerhub_username`**: Debes reemplazar esto con tu nombre de usuario real de Docker Hub.
*   **`secrets.DOCKER_USERNAME` y `secrets.DOCKER_PASSWORD`**: Estas son variables secretas de GitHub Actions que deben configurarse en la configuración de tu repositorio. `DOCKER_USERNAME` será tu nombre de usuario de Docker Hub y `DOCKER_PASSWORD` un token de acceso personal de Docker Hub (no tu contraseña de cuenta).
*   **`Dockerfile`**: Asegúrate de que el `Dockerfile` en la raíz del proyecto esté configurado correctamente para construir la aplicación `app-web`.
*   **Versiones**: Se recomienda usar etiquetas de versión más específicas en lugar de `latest` para las imágenes Docker en producción.

Este es un punto de partida. Un pipeline de CI/CD completo incluiría pasos adicionales como la ejecución de pruebas unitarias y de integración, análisis de calidad de código, escaneo de seguridad de imágenes Docker y despliegues automatizados a entornos de staging y producción.

## Post-Despliegue Steps

*   **Monitoring:** Set up monitoring for your application instances.
*   **Logging:** Ensure logs are being collected and stored appropriately.
*   **Security:** Review and configure security settings as per your organization's policies.
