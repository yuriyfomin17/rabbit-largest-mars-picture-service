FROM amazoncorretto:17-alpine

COPY target/rabbit-largest-mars-picture-service-0.0.1-SNAPSHOT.jar rabbit-largest-mars-picture-service.jar

ENTRYPOINT ["java", "-jar", "rabbit-largest-mars-picture-service.jar"]

