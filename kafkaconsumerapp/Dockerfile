FROM openjdk:17-jdk-slim
WORKDIR /kafkaconsumerapp
EXPOSE 8087
ADD ./build/libs/kafkaconsumerapp.jar .
CMD ["java", "-jar", "kafkaconsumerapp.jar"]