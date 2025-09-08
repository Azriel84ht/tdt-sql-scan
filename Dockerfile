# Stage 1: Build Frontend
FROM node:20 AS frontend
WORKDIR /app/app-web
COPY app-web/package.json ./
RUN npm install
COPY app-web/ ./
RUN npm run build:scss

# Stage 2: Build Backend
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY . .
COPY --from=frontend /app/app-web/src/main/resources/static/css/style.css ./app-web/src/main/resources/static/css/style.css
RUN mvn clean package -DskipTests

# Stage 3: Final Image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/app-web/target/app-web-*.jar ./app.jar
EXPOSE 8080
CMD ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]
