# ==========================================
# ETAPA 1: COMPILACIÓN
# ==========================================

# Imagen que contiene Maven + Java 21.
FROM maven:3.9-eclipse-temurin-21 AS build

# Carpeta interna de trabajo.
WORKDIR /app

# Copiamos primero pom.xml.
COPY pom.xml .

# Descargamos dependencias.
RUN mvn dependency:go-offline

# Copiamos el código.
COPY src ./src

# Generamos el .jar.
#
# Los tests ya los ejecutamos previamente,
# así que no necesitamos ejecutarlos
# durante la construcción de la imagen.
RUN mvn clean package -DskipTests


# ==========================================
# ETAPA 2: EJECUCIÓN
# ==========================================

# Imagen más pequeña que solo contiene Java.
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiamos el .jar generado arriba.
COPY --from=build /app/target/*.jar app.jar

# Puerto utilizado por Inventory.
EXPOSE 8082

# Arrancamos Spring Boot.
ENTRYPOINT ["java", "-jar", "app.jar"]