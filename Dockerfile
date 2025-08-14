# --- Fase de Construcción (Build Stage) ---
# Usamos una imagen oficial de Maven con Java 8 para compilar el proyecto.
FROM maven:3.8-openjdk-8 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos todo el código fuente del proyecto primero
COPY . .

# Ahora que todos los módulos están presentes, compilamos el proyecto.
# Usamos el comando 'mvn' de la imagen base para mayor robustez.
RUN ["mvn", "clean", "package", "-DskipTests"]


# --- Fase de Ejecución (Run Stage) ---
# Usamos una imagen ligera de Java 8 para ejecutar la aplicación
FROM openjdk:8-jre-slim

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos solo el JAR compilado desde la fase de construcción
COPY --from=build /app/app-web/target/app-web-*.jar ./app.jar

# El puerto en el que escuchará la aplicación
EXPOSE 8080

# El comando para arrancar la aplicación
CMD ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]
