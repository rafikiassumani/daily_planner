FROM openjdk:17-jdk-slim
WORKDIR /client
EXPOSE 8080
ADD ./build/libs/client.jar .
CMD ["java", "-jar", "client.jar"]