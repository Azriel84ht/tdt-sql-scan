# TDT SQL Scan

`tdt-sql-scan` es un proyecto Java modular cuyo objetivo es construir un analizador sintáctico (parser) capaz de procesar sentencias SQL en texto plano, transformarlas en estructuras de objetos y facilitar su análisis, validación y visualización.

---

## 🎯 Objetivo

Este software tiene como propósito principal convertir consultas SQL escritas como texto (`String`) en objetos Java estructurados que representen su estructura sintáctica. Esto permite analizar, inspeccionar o transformar consultas SQL de forma programática, facilitando tareas como:

- Validación y análisis de consultas.
- Detección de errores o patrones en el uso de SQL.
- Visualización o conversión de consultas a otros lenguajes/interfaz.
- Apoyo en herramientas de refactorización, auditoría o seguridad.

---

## 🧱 Estructura actual del proyecto

El proyecto se encuentra organizado de forma modular para facilitar su mantenimiento y escalabilidad:

tdt-sql-scan/
├── parser-core → Componentes genéricos: utilidades, tipos comunes (SQLCondition, SQLTableRef, etc.)
├── parser-select → Parser para consultas SELECT y subcláusulas asociadas (FROM, JOIN, etc.)
├── parser-ddl → Parser para sentencias de definición de datos (CREATE TABLE, etc.)
├── parser-dml → Parser para sentencias de manipulación de datos (INSERT, UPDATE, DELETE)



---

## ✅ Progreso actual

A fecha de entrega de esta versión:

- `parser-core` contiene utilidades funcionales (`SQLParserUtils`) y estructuras de datos como `SQLQuery`, `SQLCondition`, `SQLAssignment`, `SQLTableRef`, `SQLJoin`, etc.
- `parser-select` implementa `SelectParser`, que transforma sentencias SELECT completas en instancias de `SelectQuery`.
- `parser-ddl` incluye el parser para `CREATE TABLE` con su clase `CreateTableQuery`.
- `parser-dml` ha sido parcialmente implementado. Se han definido clases `UpdateParser`, `DeleteParser` e `InsertParser`, y estructuras asociadas como `UpdateQuery` o `InsertQuery`. Algunos parsers están pendientes de pulido o finalización.

---

## 🛠️ Compilación

Este proyecto utiliza Maven Wrapper para su compilación. Puedes compilar todos los módulos con:

```bash
./mvnw clean compile
```
O compilar un módulo específico, como parser-select, usando:
```bash
./mvnw -pl parser-select -am clean compile
```

🔜 Próximos pasos sugeridos
Terminar y testear los parsers InsertParser, UpdateParser y DeleteParser.

Añadir una batería de tests unitarios para cada parser.

Incluir soporte para subconsultas, alias complejos, expresiones en columnas y condiciones compuestas.

Implementar un sistema de visualización o exportación de las estructuras resultantes.

Validar compatibilidad multibase de datos (MySQL, PostgreSQL, etc.).

🧑‍💻 Contribución
Este proyecto está abierto a contribuciones. Si recoges este repositorio, puedes continuar desarrollando nuevos parsers o ampliar la lógica existente.

📄 Licencia
Por determinar (añadir licencia apropiada si se desea liberar).
