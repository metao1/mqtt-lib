FROM openjdk:11-slim

VOLUME /tmp
RUN mkdir log
RUN touch log/spring.log
RUN mkdir /opt/metao
COPY build/libs/mqtt.jar /opt/metao/mqtt.jar

ENV SPRING_PROFILES_ACTIVE=dev
CMD ["-jar", "/opt/metao/mqtt.jar" ]
ENTRYPOINT ["java", "-jar", "/opt/metao/mqtt.jar"]
CMD ["java", "-Dspring.data.mongodb.uri=mongodb://springboot-mongo:27017/mqtt","-Djava.security.egd=file:/dev/./urandom","-jar","./opt/metao/mqtt.jar"]

EXPOSE 1883