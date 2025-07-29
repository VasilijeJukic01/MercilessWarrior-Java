FROM alpine:latest as waitforit

WORKDIR /app

ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /app/wait-for-it.sh

RUN chmod +x /app/wait-for-it.sh

# Build
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /home/gradle/src

COPY . .

RUN gradle installDist --no-daemon

# Final image
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /home/gradle/src/build/install/analytics-service/ /app/

COPY --from=waitforit /app/wait-for-it.sh /app/wait-for-it.sh

RUN mkdir -p /app/data/lake && mkdir -p /app/data/checkpoints

ENTRYPOINT ["./wait-for-it.sh", "kafka:9092", "--timeout=60", "--", "/app/bin/analytics-service"]