FROM adoptopenjdk/openjdk17:alpine-jre
LABEL maintainer="IT Company - www.itcompany.com"
WORKDIR /opt/app
COPY target/*.jar totem-food-order-backend.jar
ENTRYPOINT ["java","-jar","/opt/app/totem-food-order-backend.jar"]
