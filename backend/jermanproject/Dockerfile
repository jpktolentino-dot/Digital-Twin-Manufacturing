
# Etapa 1: compilar a aplicação
FROM maven:3.10-eclipse-temurin-25 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package


# Etapa 2: executar a aplicação
FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system spring \
    && useradd --system --gid spring --uid 10001 spring

COPY --from=build --chown=10001:10001 \
    /workspace/target/jermanproject-0.0.1-SNAPSHOT.jar \
    /app/app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
