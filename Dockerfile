#Compile and Package application
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

#This is a container for running application
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/test-classes/email-writer-0.0.1-SNAPSHOT.jar. email-writer.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","email-writer.jar"]