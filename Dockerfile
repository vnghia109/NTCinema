FROM maven:3.8.3-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/NT_cinema-0.0.1-SNAPSHOT.jar NT_cinema.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","NT_cinema.jar"]