FROM openjdk:8-jre-alpine

ARG version

COPY ./build/libs/herbarium-api.jar /root/herbarium-api.jar
COPY ./firebase_service_account_credentials.json /root/firebase_service_account_credentials.json

WORKDIR /root

ENV VERSION=$version

CMD ["java", "-server", "-Xms4g", "-Xmx4g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "herbarium-api.jar"]