# User Service

## Resumen
El User Service es un microservicio con estado, pilar fundamental de la seguridad y la gestión de identidades en la plataforma. Su principal responsabilidad es la gestión completa del ciclo de vida de los usuarios (CRUD: Crear, Leer, Actualizar, Eliminar).

Adicionalmente, actúa como un Servidor de Autenticación centralizado, encargado de validar las credenciales de los usuarios y emitir JSON Web Tokens (JWTs). Estos tokens son utilizados por los servicios cliente para autorizar el acceso a recursos protegidos en todo el ecosistema.

## Stack Tecnológico
*   Java 17
*   Spring Boot 3
*   Spring Data JPA
*   Spring Security
*   PostgreSQL

## API Endpoints
A continuación se presenta un resumen de los endpoints expuestos por el servicio. Para una especificación detallada, por favor consulta la documentación de OpenAPI (Swagger UI) del servicio.

### Gestión de Usuarios
| Método HTTP | Ruta                  | Descripción                      |
|-------------|-----------------------|----------------------------------|
| `GET`       | `/api/v1/users`       | Obtiene una lista de usuarios.   |
| `GET`       | `/api/v1/users/{id}`  | Obtiene un usuario por su ID.    |
| `POST`      | `/api/v1/users`       | Crea un nuevo usuario.           |
| `PUT`       | `/api/v1/users/{id}`  | Actualiza un usuario existente.  |
| `DELETE`    | `/api/v1/users/{id}`  | Elimina un usuario por su ID.    |

### Autenticación
| Método HTTP | Ruta                  | Descripción                               |
|-------------|-----------------------|-------------------------------------------|
| `POST`      | `/api/v1/auth/login`  | Autentica a un usuario y devuelve un JWT. |
| `POST`      | `/api/v1/auth/register`| Registra un nuevo usuario.                |

## Configuración
La configuración del servicio, incluyendo la conexión a la base de datos, los secretos para la firma de JWTs y otras propiedades, se gestiona de forma centralizada a través de nuestro **config-server**.

El fichero clave en este servicio es `bootstrap.properties`, que contiene la información necesaria para que el servicio se conecte al `config-server` al arrancar y recupere su configuración específica.

## Cómo Ejecutarlo Localmente

### Prerrequisitos
1.  **Instancia de PostgreSQL**: Asegúrate de tener una base de datos PostgreSQL en ejecución y accesible.
2.  **Config Server**: El `config-server` debe estar en ejecución, ya que el User Service depende de él para obtener su configuración.

### Ejecución con Maven
Una vez cumplidos los prerrequisitos, puedes levantar el servicio ejecutando el siguiente comando de Maven desde el directorio raíz del módulo `user-service`:
```bash
mvn spring-boot:run
```
