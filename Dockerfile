# --- Fase de Construcción (Build Stage) ---
# Usamos la imagen oficial de Maven con OpenJDK 21 que ha sido verificada por el usuario.
FROM maven:3.9-eclipse-temurin-21 AS build

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app
COPY app-web/package.json ./
RUN npm install
COPY app-web/ ./
RUN npm run build

# --- ETAPA 2: Construcción del Backend (Java) ---
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY . .
# --- LÍNEA MODIFICADA ---
# Copia el CSS generado desde la etapa del frontend a la carpeta de recursos estáticos del backend.
COPY --from=frontend /app/dist/style.css ./app-web/src/main/resources/static/css/style.css
RUN mvn clean package -DskipTests

# Compilamos el proyecto.
RUN mvn clean package -DskipTests


# --- Fase de Ejecución (Run Stage) ---
# Usamos una imagen de Eclipse Temurin con JRE 21 para mantener la consistencia.
FROM eclipse-temurin:21-jre

# Establecemos el directorio de trabajo
WORKDIR /app
COPY --from=build /app/app-web/target/*.jar ./app.jar
EXPOSE 8080
# Activa el perfil 'production' para que no se cargue el CDN de Tailwind
CMD ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]
