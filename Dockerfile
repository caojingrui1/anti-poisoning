FROM picoded/ubuntu-openjdk-8-jdk:16.04
LABEL maintainer="CloudSCA"

RUN mkdir -p /opt/app/
ENV APP_DIR /opt/app/
WORKDIR $APP_DIR

ADD ./start.sh $APP_DIR
ARG JAR_FILE=target/anti-poisoning-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} $APP_DIR
COPY target/classes/*.yaml $APP_DIR

ENTRYPOINT ["/bin/bash","start.sh","start"]
