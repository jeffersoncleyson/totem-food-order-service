FROM maven:3.9.2-eclipse-temurin-17-alpine AS build
COPY totem-food-order-backend /usr/src/app/totem-food-order-backend
COPY totem-food-order-application /usr/src/app/totem-food-order-application
COPY totem-food-order-domain /usr/src/app/totem-food-order-domain
COPY totem-food-order-framework /usr/src/app/totem-food-order-framework
COPY pom.xml /usr/src/app/pom.xml
RUN mvn -f /usr/src/app/pom.xml clean package

FROM openjdk:17.0.2-slim-buster
LABEL maintainer="Totem Food Service"
WORKDIR /opt/app
COPY --from=build /usr/src/app/totem-food-order-backend/target/*.jar totem-food-order-service.jar
ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:8787", "-jar","/opt/app/totem-food-order-service.jar"]