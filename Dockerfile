FROM eclipse-temurin:17-jre-alpine
LABEL authors="fakey"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["top", "-b"]