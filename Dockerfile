# --- Fase de Construcción (Build Stage) ---
# Usamos una imagen oficial de Maven con Java 8 para compilar el proyecto.
# La versión de Java la he deducido de tu archivo pom.xml.
FROM maven:3.8-openjdk-8 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos los archivos de Maven para descargar dependencias
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Descargamos las dependencias de Maven. Esto se cachea si no cambian.
RUN ./mvnw dependency:go-offline

# Copiamos todo el código fuente del proyecto
COPY . .

# Compilamos la aplicación y empaquetamos el JAR, saltando los tests
RUN ./mvnw clean package -DskipTests


# --- Fase de Ejecución (Run Stage) ---
# Usamos una imagen ligera de Java 8 para ejecutar la aplicación, optimizando el tamaño.
FROM openjdk:8-jre-slim

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos solo el JAR compilado desde la fase de construcción
COPY --from=build /app/app-web/target/app-web-*.jar ./app.jar

# El puerto en el que escuchará la aplicación. Render lo gestionará.
EXPOSE 8080

# El comando para arrancar la aplicación, activando el perfil de producción
CMD ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]