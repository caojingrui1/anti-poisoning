FROM picoded/ubuntu-openjdk-8-jdk:16.04
LABEL maintainer="CloudSCA"

RUN mkdir -p /opt/app/
ENV APP_DIR /opt/app/
WORKDIR $APP_DIR

ADD ./start.sh $APP_DIR
ARG JAR_FILE=anti-poisoning/target/anti-poisoning-1.0-SNAPSHOT.jar

COPY ${JAR_FILE} $APP_DIR
COPY anti-poisoning/target/classes/*.properties $APP_DIR

ENTRYPOINT ["/bin/bash","start.sh","start"]
