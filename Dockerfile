# --- Fase de Construcción (Build Stage) ---
# Usamos una imagen base de OpenJDK 21
FROM openjdk:21-jdk-slim AS build

# Variables de entorno para Maven
ENV MAVEN_VERSION 3.9.11
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_BIN_URL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz

# Instalamos wget, desempaquetamos Maven y lo limpiamos
RUN apt-get update && \
    apt-get install -y wget && \
    wget -q -O /tmp/maven.tar.gz ${MAVEN_BIN_URL} && \
    tar -xzf /tmp/maven.tar.gz -C /usr/share && \
    mv /usr/share/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
    rm /tmp/maven.tar.gz && \
    apt-get purge -y --auto-remove wget && \
    rm -rf /var/lib/apt/lists/*

# Agregamos Maven al PATH
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos todo el código fuente del proyecto primero
COPY . .

# Ahora que todos los módulos están presentes, compilamos el proyecto.
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
