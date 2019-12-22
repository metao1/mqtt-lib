FROM maven:3-jdk-8-slim
VOLUME /tmp
RUN mkdir log
RUN touch log/spring.log
RUN mkdir /opt/metao
COPY build/libs/mqtt.jar /opt/metao/mqtt.jar


COPY ./runbroker.sh /

RUN chmod +x /runbroker.sh
#
ENTRYPOINT ["./runbroker.sh", "/opt/metao/mqtt.jar"]

EXPOSE 1883
EXPOSE 1884
