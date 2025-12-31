FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache \
    bash \
    curl \
    tzdata

RUN addgroup -S runtimegroup \
 && adduser -S runtimeuser -G runtimegroup

WORKDIR /app

COPY target/safecube-backend.jar app.jar

RUN chown -R runtimeuser:runtimegroup /app

USER runtimeuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
