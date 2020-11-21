FROM adoptopenjdk:11-jdk-hotspot as builder

LABEL stage=builder
ADD . /code/

RUN    addgroup mqtt \
    && apt-get update \
    && apt-get install build-essential -y \
    && apt-get install dos2unix -y \
    && adduser --system --disabled-password --disabled-login mqtt \
    && adduser mqtt mqtt \
    && mkdir -p /opt/metao \
    && chown -R mqtt:mqtt /opt/metao \
    && chown -R mqtt:mqtt /code

RUN    cd /code/ \
    && dos2unix gradlew \
    && chmod +x gradlew

USER mqtt

RUN cd /code/ \
    && rm -rf build \
    && sleep 1 \
    && ./gradlew clean build -x test \
    && cp build/libs/*.jar /opt/metao/mqtt.jar

FROM adoptopenjdk:11-jre-hotspot

RUN    addgroup mqtt \
    && apt-get update \
    && apt-get install build-essential -y \
    && adduser --system --disabled-password --disabled-login mqtt \
    && adduser mqtt mqtt \
    && mkdir -p /opt/metao \
    && mkdir -p /var/log/metao \
    && chown -R mqtt:mqtt /var/log/metao \
    && touch /var/log/metao/mqtt.log \
    && chown -R mqtt:mqtt /opt/metao

COPY --from=builder /opt/metao/mqtt.jar /opt/metao
COPY ./runbroker.sh /

USER mqtt

EXPOSE 1883

ENTRYPOINT ["./runbroker.sh", "/opt/metao/mqtt.jar"]