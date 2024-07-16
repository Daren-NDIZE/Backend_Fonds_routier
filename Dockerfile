From amazoncorretto:17-al2023-jdk
WORKDIR /app
COPY target/Fonds_routier-0.0.1-SNAPSHOT.jar Fonds_routier.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","Fonds_routier.jar"]