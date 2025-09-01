FROM maven:3.9.9-amazoncorretto-17 AS builder

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -Dquarkus.package.type=fast-jar

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests \
    -Dquarkus.package.type=fast-jar \
    -Dquarkus.native.container-build=false

FROM amazoncorretto:17-alpine
WORKDIR /deployments

RUN addgroup -S appuser && adduser -S appuser -G appuser
COPY --from=builder --chown=appuser:appuser /app/target/quarkus-app/ /deployments/
USER appuser
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]