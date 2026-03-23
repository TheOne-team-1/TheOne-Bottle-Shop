FROM eclipse-temurin:21-jre-alpine
LABEL authors="kyjtheyj"
WORKDIR /app
COPY build/libs/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]