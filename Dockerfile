FROM amazoncorretto:17

COPY target/rabbit-largest-mars-picture-service-0.0.1-SNAPSHOT.jar rabbit-largest-mars-picture-service

ENTRYPOINT ["java", "-jar", "rabbit-largest-mars-picture-service"]