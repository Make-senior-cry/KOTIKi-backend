ARG DATABASE_URL
ARG JWT_ACCESS_TOKEN_SECRET
ARG JWT_REFRESH_TOKEN_SECRET
ARG PGDATABASE
ARG PGHOST
ARG PGPASSWORD
ARG PGPORT
ARG PGUSER
ARG SERVER_ADDRESS

FROM gradle:7.3.1-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon


FROM openjdk:17-alpine
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/kotiki.jar
EXPOSE 8080
RUN export PROJECT_PATH=$(pwd)
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "kotiki.jar"]