# --- Frontend build stage ---
FROM maven:3.9.6-eclipse-temurin-21 AS frontend
RUN apt-get update -y && \
    apt-get install -y nodejs npm
WORKDIR /app
COPY app-web/package.json ./
RUN npm install
COPY app-web/ ./
RUN npm run build

# --- Backend build stage ---
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY . .
COPY --from=frontend /app/dist/style.css ./app-web/src/main/resources/static/css/style.css
RUN mvn clean package -DskipTests

# --- Final stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/app-web/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]
