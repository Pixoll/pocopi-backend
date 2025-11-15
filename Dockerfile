FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle gradle

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/app/build \
    ./gradlew bootJar --no-daemon && \
    cp build/libs/*.jar /app/app.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/app.jar app.jar
COPY .env .
COPY images images

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
