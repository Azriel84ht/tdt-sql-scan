# --- Fase de Construcción (Build Stage) ---
# Usamos una imagen de Maven con OpenJDK 21 del repositorio de csanchez.
FROM csanchez/maven:eclipse-temurin-21-noble AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos todo el código fuente del proyecto primero
COPY . .

# Compilamos el proyecto.
RUN mvn clean package -DskipTests


# --- Fase de Ejecución (Run Stage) ---
# Usamos una imagen ligera de Java 21 para ejecutar la aplicación
FROM openjdk:21-jre-slim

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos solo el JAR compilado desde la fase de construcción
COPY --from=build /app/app-web/target/app-web-*.jar ./app.jar

# El puerto en el que escuchará la aplicación
EXPOSE 8080

# El comando para arrancar la aplicación
CMD ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]
