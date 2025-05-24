# Fetching wait-for-it.sh
FROM alpine:latest as waitforit

WORKDIR /app

ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /app/wait-for-it.sh

RUN chmod +x /app/wait-for-it.sh

# Build
FROM gradle:8.7-jdk17 as build

WORKDIR /home/gradle/src

COPY . /home/gradle/src

RUN gradle clean build -x test --no-daemon

# Final image
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=waitforit /app/wait-for-it.sh /app/wait-for-it.sh

COPY --from=build /home/gradle/src/build/libs/auth-service-0.0.1-SNAPSHOT.jar /app

CMD ["./wait-for-it.sh", "service-registry:8761", "--", "java", "-jar", "auth-service-0.0.1-SNAPSHOT.jar"]
