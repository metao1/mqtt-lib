FROM openjdk:11-slim

VOLUME /tmp
RUN mkdir log
RUN touch log/spring.log
COPY build/libs/mqtt.jar /mqtt.jar

ENV SPRING_PROFILES_ACTIVE=dev
CMD [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /mqtt.jar" ]
ENTRYPOINT ["java", "-jar", "/mqtt.jar"]