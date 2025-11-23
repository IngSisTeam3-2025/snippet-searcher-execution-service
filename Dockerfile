FROM gradle:8.7.0-jdk17 AS builder
WORKDIR /app
COPY . .
COPY token.txt /token.txt
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

RUN mkdir -p /usr/local/newrelic

ADD ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/usr/local/newrelic/newrelic.jar", "-jar", "app.jar"]