# Parser Service

## Resumen

El Parser Service es un microservicio sin estado diseñado como el núcleo de análisis sintáctico de la plataforma TDT SQL Scan. Su principal responsabilidad es procesar scripts SQL, con un enfoque especializado en Teradata BTEQ, utilizando la potencia de ANTLR 4 para la generación de un Árbol de Sintaxis Abstracto (AST).

Este servicio recibe contenido de script a través de su API REST, lo descompone en sentencias individuales y devuelve una estructura de datos detallada que puede ser utilizada por otros servicios o frontends para visualización, análisis de linaje de datos o cualquier otra finalidad.

## Stack Tecnológico

*   **Java**: 17
*   **Framework**: Spring Boot 3
*   **Parser Generator**: ANTLR 4
*   **Seguridad**: Spring Security con OAuth2 Resource Server para la validación de tokens JWT.
*   **Documentación de API**: Springdoc OpenAPI (Swagger UI)

## API Endpoints

El servicio expone los siguientes endpoints. Para obtener una descripción detallada de los modelos de petición y respuesta, por favor, consulta la especificación OpenAPI (Swagger UI) que se levanta junto con el servicio.

| Método HTTP | Ruta                       | Descripción                          | Protegido |
|-------------|----------------------------|--------------------------------------|-----------|
| `POST`      | `/api/v1/parse/poc`        | Analiza un script BTEQ completo.     | Sí        |
| `POST`      | `/api/v1/parse/select`     | Analiza una única sentencia SELECT.  | Sí        |

## Configuración

La configuración del Parser Service se gestiona de forma centralizada a través del **Config Server**. Al arrancar, el servicio se conecta al servidor de configuración para obtener sus propiedades.

El fichero clave para esta conexión es `bootstrap.properties`, que contiene:
*   `spring.application.name`: Define el nombre de la aplicación (`parser-service`) que se usará para buscar su fichero de configuración en el `config-server`.
*   `spring.cloud.config.uri`: La URI del `config-server` (ej. `http://localhost:8888`).

## Cómo Ejecutarlo Localmente

### Prerrequisitos
1.  Tener JDK 17 o superior instalado.
2.  Tener Apache Maven instalado.
3.  Asegurarse de que el **Config Server** esté corriendo, ya que el Parser Service depende de él para arrancar.

### Ejecución con Maven
Puedes ejecutar el servicio directamente con el plugin de Spring Boot de Maven:
```bash
mvn spring-boot:run
```

### Deshabilitar la Seguridad para Pruebas
Para desarrollo y pruebas locales, puede ser útil deshabilitar la seguridad y no requerir un token de autenticación. Para ello, se puede activar el perfil `dev-unsecured`:
```bash
mvn spring-boot:run -Dspring.profiles.active=dev-unsecured
```
Esto cargará la configuración definida en `application-dev-unsecured.properties`, que deshabilita la validación de tokens JWT.
