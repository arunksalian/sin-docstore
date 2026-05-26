# ---- build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache dependencies separately from source
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ---- runtime stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S docstore && adduser -S docstore -G docstore

WORKDIR /app
COPY --from=builder /build/target/sin-docstore-*.jar app.jar

USER docstore

EXPOSE 8080 9090

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
