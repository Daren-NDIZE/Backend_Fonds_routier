# From amazoncorretto:17-al2023-jdk
# VOLUME /tmp
# COPY target/Fonds_routier-0.0.1-SNAPSHOT.jar app.jar
# ENTRYPOINT ["java","-jar","/app.jar"]
# EXPOSE 8080

FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/Fonds_routier-0.0.1-SNAPSHOT.jar Fonds_routier.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","Fonds_routier.jar"]