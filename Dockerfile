# --- Fase de Construcción (Build Stage) ---
# Usamos la imagen oficial de Maven con OpenJDK 21 que ha sido verificada por el usuario.
FROM maven:3.9.6-eclipse-temurin-21 AS frontend

# Install Node.js
RUN apt-get update && \
    apt-get install -y curl gnupg && \
    curl -sL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

WORKDIR /app
COPY app-web/ ./app-web/
WORKDIR /app/app-web
RUN npm install
RUN chmod +x ./node_modules/tailwindcss/lib/cli.js
RUN npm run build

# --- ETAPA 2: Construcción del Backend (Java) ---
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY . .
# --- LÍNEA MODIFICADA ---
# Copia el CSS generado desde la etapa del frontend a la carpeta de recursos estáticos del backend.
COPY --from=frontend /app/app-web/dist/style.css ./app-web/src/main/resources/static/css/style.css
# Compilamos el proyecto.
RUN mvn clean package -DskipTests
# --- Fase de Ejecución (Run Stage) ---
# Usamos una imagen de Eclipse Temurin con JRE 21 para mantener la consistencia.
FROM eclipse-temurin:21-jre

# Establecemos el directorio de trabajo
WORKDIR /app
COPY --from=backend-build /app/app-web/target/*.jar ./app.jar
EXPOSE 8080
# Activa el perfil 'production' para que no se cargue el CDN de Tailwind
CMD ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]
