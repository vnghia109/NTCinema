FROM openjdk:8-jdk
VOLUME /tmp
EXPOSE 8080
ADD target/NT_cinema-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]