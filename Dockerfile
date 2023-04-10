FROM openjdk:17-alpine
ADD build/libs/kotiki-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
RUN export PROJECT_PATH=$(pwd)
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]