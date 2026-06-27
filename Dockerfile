FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/data \
    && chown -R spring:spring /app

USER spring:spring

COPY --from=build /app/target/feature-flags-api-*.jar app.jar

EXPOSE 8080

ENV SERVER_PORT=8080 \
    SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/featureflags;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE \
    SPRING_H2_CONSOLE_ENABLED=false

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
