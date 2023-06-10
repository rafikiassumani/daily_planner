FROM openjdk:17-jdk-slim
WORKDIR /app
EXPOSE 8080
ADD ./build/libs/app.jar .
CMD ["java", "-jar", "app.jar"]