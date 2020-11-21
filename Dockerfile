FROM adoptopenjdk:11-jdk-hotspot as builder

LABEL stage=builder
ADD . /code/

RUN mkdir -p /opt/metao

RUN    apt-get update \
    && apt-get install build-essential -y \
    && apt-get install dos2unix -y \
    && cd /code/ \
    && dos2unix gradlew \
    && chmod +x gradlew \
    && ./gradlew clean build -x test \
    && cp build/libs/*.jar /opt/metao/mqtt.jar


EXPOSE 1883
EXPOSE 1884
ENTRYPOINT ["./code/runbroker.sh", "/opt/metao/mqtt.jar"]