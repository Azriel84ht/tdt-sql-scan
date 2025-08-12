# TDT SQL Scan

`tdt-sql-scan` es un proyecto Java modular cuyo objetivo es construir un analizador sintáctico (parser) capaz de procesar scripts BTEQ de Teradata, transformarlos en un grafo de flujo de datos y visualizarlo en una interfaz web.

---

## 🎯 Objetivo

Este software tiene como propósito principal convertir scripts BTEQ en un diagrama de flujo interactivo. Esto permite a los desarrolladores y analistas de datos entender fácilmente las dependencias y el flujo de operaciones en scripts complejos, facilitando tareas como:

- **Análisis de Flujo de Datos:** Visualizar cómo los datos se mueven entre tablas a través de sentencias `INSERT`, `UPDATE`, etc.
- **Depuración y Optimización:** Identificar cuellos de botella o lógica incorrecta en los scripts.
- **Documentación:** Generar documentación visual de procesos ETL complejos.

---

## 🧱 Estructura del proyecto

El proyecto se encuentra organizado de forma modular para facilitar su mantenimiento y escalabilidad:

-   `parser-core`: Componentes genéricos para el análisis de SQL.
-   `parser-select`: Parser para consultas `SELECT`.
-   `parser-ddl`: Parser para sentencias de definición de datos (`CREATE TABLE`, `DROP TABLE`).
-   `parser-dml`: Parser para sentencias de manipulación de datos (`INSERT`, `UPDATE`, `DELETE`).
-   `parser-etl`: Parser para la estructura de scripts BTEQ, que combina los parsers de SQL.
-   `graph`: Clases para la representación de la estructura del grafo (Nodos y Aristas).
-   `app-web`: Aplicación web Spring Boot que expone una API REST y sirve la interfaz de visualización.

---

## ✨ Funcionalidades Principales

-   **Análisis de Scripts BTEQ:** Soporte para los comandos más comunes de BTEQ y sentencias SQL de Teradata.
-   **Visualización de Grafos:** Genera un diagrama de flujo de datos donde:
    -   Los nodos representan comandos BTEQ o sentencias SQL.
    -   Las sentencias que operan sobre la misma tabla se alinean en **carriles horizontales** para una fácil identificación.
    -   Los comandos de control de BTEQ se muestran en un carril superior.
    -   Las sentencias `SELECT` se muestran en el carril superior, ya que no modifican tablas.
    -   Las sentencias `INSERT` no generan nodos extra, para una vista más limpia.
    -   El diagrama generado es **estático y no editable**, para preservar la representación fiel del script.
-   **Interfaz Web Interactiva:** Permite subir uno o más ficheros de script BTEQ y visualizarlos al instante.

---

## 🛠️ Compilación y Ejecución

Este proyecto utiliza Maven Wrapper para su compilación.

1.  **Compilar el proyecto:**
    ```bash
    ./mvnw clean install
    ```
2.  **Ejecutar la aplicación web:**
    ```bash
    java -jar app-web/target/app-web-1.0-SNAPSHOT.jar
    ```
3.  Accede a `http://localhost:8080` en tu navegador para usar la aplicación.

---
## 📄 Licencia
Por determinar (añadir licencia apropiada si se desea liberar).
