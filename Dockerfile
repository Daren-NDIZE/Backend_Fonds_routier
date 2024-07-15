From openjdk:17
ADD target/Fonds_routier-0.0.1-SNAPSHOT.jar Fonds_routier.jar
ENTRYPOINT ["java","-jar","Fonds_routier.jar"]