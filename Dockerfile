# --- ETAPA 1: Construcción del Frontend ---
FROM node:18 AS frontend
WORKDIR /app
COPY app-web/package.json ./
RUN npm install
COPY app-web/ ./
RUN npm run build

# --- ETAPA 2: Construcción del Backend (Java) ---
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY . .
COPY --from=frontend /app/dist/style.css ./app-web/src/main/resources/static/css/style.css
RUN mvn clean package -DskipTests

# --- ETAPA 3: Imagen Final de Ejecución ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/app-web/target/*.jar ./app.jar
EXPOSE 8080
# Activa el perfil 'production' para que no se cargue el CDN de Tailwind
CMD ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]
