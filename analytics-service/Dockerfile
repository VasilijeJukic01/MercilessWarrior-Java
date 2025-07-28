FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /home/gradle/src

COPY . .

RUN gradle installDist --no-daemon

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /home/gradle/src/build/install/analytics-service/ /app/

RUN mkdir -p /app/data/lake && mkdir -p /app/data/checkpoints

ENTRYPOINT ["/app/bin/analytics-service"]