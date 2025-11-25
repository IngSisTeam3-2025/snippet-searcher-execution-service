FROM gradle:8.7.0-jdk17 AS builder

WORKDIR /app

ARG GPR_USER
ARG GPR_KEY

RUN mkdir -p /root/.gradle && \
    echo "gpr.user=${GPR_USER}" >> /root/.gradle/gradle.properties && \
    echo "gpr.key=${GPR_KEY}" >> /root/.gradle/gradle.properties

COPY . .

RUN chmod +x gradlew

# LA LÍNEA CLAVE
RUN ./gradlew build bootJar --no-daemon

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

RUN mkdir -p /usr/local/newrelic

ADD ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/usr/local/newrelic/newrelic.jar", "-jar", "app.jar"]
