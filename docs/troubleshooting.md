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

## Resolución de Problemas de Bases de Datos

Los problemas de conexión o interacción con la base de datos son comunes. Aquí se abordan soluciones para las bases de datos H2 (usada en desarrollo) y PostgreSQL (recomendada para producción).

### Problemas de Conexión con H2 Database (Desarrollo)

**Problema:** La aplicación no puede conectarse a la base de datos H2 o la consola H2 no es accesible.

**Solución:**
1.  **Verificar URL de Conexión:** Asegúrate de que la URL de conexión en `application.properties` sea correcta. Para una base de datos en memoria, suele ser `jdbc:h2:mem:testdb`. Para una base de datos basada en archivo, `jdbc:h2:file:./data/testdb`.
2.  **Consola H2:** Si usas la consola H2, verifica que esté habilitada en `application.properties` (`spring.h2.console.enabled=true`) y que la ruta de acceso (`spring.h2.console.path=/h2-console`) no esté bloqueada por la configuración de seguridad de Spring Security (revisa `SecurityConfig.java`).
3.  **Múltiples Instancias:** Si usas H2 en modo en memoria, cada vez que la aplicación se reinicia, la base de datos se reinicia. Asegúrate de no tener múltiples instancias de la aplicación intentando usar la misma base de datos en memoria.

### Problemas de Conexión con PostgreSQL (Producción)

**Problema:** La aplicación no puede conectarse a la base de datos PostgreSQL externa.

**Solución:**
1.  **Credenciales y URL:** Revisa que `spring.datasource.url`, `spring.datasource.username` y `spring.datasource.password` en `application-production.properties` o las variables de entorno correspondientes sean correctas y coincidan con la configuración de tu servidor PostgreSQL.
    *   **Formato URL:** `jdbc:postgresql://<host>:<puerto>/<nombre_base_datos>`
2.  **Servidor PostgreSQL Activo:** Asegúrate de que el servicio de PostgreSQL esté en ejecución en el host especificado.
3.  **Acceso a la Red/Firewall:** Verifica que el servidor donde se ejecuta la aplicación tenga acceso de red al host y puerto de PostgreSQL. Los firewalls (tanto en el servidor de la aplicación como en el de la base de datos) pueden estar bloqueando la conexión.
4.  **Configuración `pg_hba.conf`:** En PostgreSQL, el archivo `pg_hba.conf` controla qué hosts pueden conectarse y con qué métodos de autenticación. Asegúrate de que la dirección IP del servidor de tu aplicación esté permitida y que el método de autenticación sea compatible (ej. `md5`, `scram-sha-256`).
5.  **Base de Datos Existente:** Confirma que la base de datos especificada en la URL (`your_db_name`) realmente existe en el servidor PostgreSQL.
6.  **Pool de Conexiones:** Si usas un pool de conexiones (ej. HikariCP, por defecto en Spring Boot), revisa los logs para ver si hay errores relacionados con el pool al intentar establecer conexiones iniciales.

### Errores de Esquema

**Problema:** La aplicación lanza errores relacionados con tablas o columnas inexistentes, o tipos de datos incorrectos.

**Solución:**
1.  **Sincronización de Esquema:** Asegúrate de que el esquema de la base de datos (H2 o PostgreSQL) esté sincronizado con el modelo de entidades de la aplicación. En producción, esto se gestiona idealmente con herramientas de migración de base de datos (Flyway, Liquibase).
2.  **`spring.jpa.hibernate.ddl-auto`:** En desarrollo, `update` o `create-drop` pueden ayudar a que Hibernate gestione el esquema. En producción, se recomienda `validate` o `none` para evitar cambios automáticos y no deseados.

### Problemas de Rendimiento de Consultas

**Problema:** Las operaciones de la base de datos son lentas.

**Solución:**
1.  **Activar Logging de SQL:** Aumenta el nivel de logging para Hibernate/JPA para ver las consultas SQL generadas y sus tiempos de ejecución.
2.  **Analizar Consultas:** Utiliza herramientas de análisis de rendimiento de la base de datos (ej. `EXPLAIN ANALYZE` en PostgreSQL) para identificar cuellos de botella en consultas específicas.
3.  **Índices:** Asegúrate de que los índices adecuados estén definidos en las columnas utilizadas en cláusulas `WHERE`, `JOIN` y `ORDER BY`.

## Niveles de Logging y Salida de Depuración

El sistema de logging es una herramienta invaluable para diagnosticar problemas en la aplicación. Spring Boot utiliza `Logback` por defecto, y los niveles de logging se pueden configurar fácilmente.

### Cómo Cambiar los Niveles de Logging

Los niveles de logging se configuran en el archivo `application.properties` (o `application-production.properties` para entornos de producción). Puedes establecer el nivel de logging global o para paquetes específicos.

**Niveles de Logging Disponibles:**

*   `TRACE`: El nivel más detallado, muestra información muy granular.
*   `DEBUG`: Información detallada útil para la depuración.
*   `INFO`: Mensajes informativos que resaltan el progreso de la aplicación (nivel por defecto).
*   `WARN`: Mensajes de advertencia sobre situaciones potencialmente problemáticas.
*   `ERROR`: Mensajes de error que indican problemas que impiden el funcionamiento normal.

**Ejemplos de Configuración en `application.properties`:**

1.  **Cambiar el nivel de logging global a DEBUG:**
    ```properties
    logging.level.root=DEBUG
    ```

2.  **Cambiar el nivel de logging para un paquete específico (ej. los parsers) a TRACE:**
    ```properties
    logging.level.com.tdtsqlscan.parser=TRACE
    ```

3.  **Cambiar el nivel de logging para Spring Security a DEBUG (útil para problemas de autenticación/autorización):**
    ```properties
    logging.level.org.springframework.security=DEBUG
    ```

4.  **Mostrar las sentencias SQL generadas por Hibernate (útil para depurar problemas de base de datos):**
    ```properties
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true
    logging.level.org.hibernate.SQL=DEBUG
    logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
    ```

### Uso de Variables de Entorno

Para entornos de producción, es preferible configurar los niveles de logging mediante variables de entorno para evitar modificar el archivo `application.properties` directamente. Puedes usar el prefijo `LOGGING_LEVEL_` seguido del nombre del logger (separado por guiones bajos en lugar de puntos).

**Ejemplo con variables de entorno:**

```bash
export LOGGING_LEVEL_ROOT=DEBUG
export LOGGING_LEVEL_COM_TDTSQLSCAN_PARSER=TRACE
```

Al ajustar los niveles de logging, puedes obtener una visión más profunda del comportamiento interno de la aplicación, lo que facilita la identificación y resolución de problemas.

## Reporting Issues

If you encounter an issue not covered here, please:

1.  **Search Existing Issues:** Check the project's issue tracker (e.g., GitHub Issues) to see if the problem has already been reported.
2.  **Provide Details:** When reporting a new issue, include:
    *   A clear and concise description of the problem.
    *   Steps to reproduce the issue.
    *   Any error messages or stack traces.
    *   Your operating system and Java version.
    *   The version of the TDT SQL Scan project you are using.
