# Build
FROM gradle:7.4.0-jdk17 as build

WORKDIR /home/gradle/src

COPY ci-config.gradle.kts ./..
COPY service-registry/. /home/gradle/src

RUN gradle clean build --no-daemon

# Final image
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/service-registry-0.0.1-SNAPSHOT.jar /app

CMD ["java", "-jar", "service-registry-0.0.1-SNAPSHOT.jar"]