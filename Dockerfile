FROM ubuntu:latest

# PROJECT_NAME 填写你的项目名字
ENV PROJECT_NAME anti-poisoning
# PROJECT_HOME 构建成镜像之后，存放的目录位置
ENV PROJECT_HOME /usr/local/${PROJECT_NAME}

RUN mkdir $PROJECT_HOME && mkdir $PROJECT_HOME/logs
RUN apt-get update && apt-get install -y python3 python3-pip openjdk-17-jdk rpm 7zip unzip rubygems && \
    pip3 install pyyaml yara_python

ADD anti-poisoning-0.0.1-SNAPSHOT.jar $PROJECT_HOME/$PROJECT_NAME.jar
ADD tools.zip $PROJECT_HOME/
RUN unzip -d $PROJECT_HOME/ $PROJECT_HOME/tools.zip

# 设置编码
ENV LANG C.UTF-8
# 设置时区
ENV TZ=Asia/Shanghai


#ARG JAR_FILE
#COPY ${JAR_FILE} $PROJECT_HOME
WORKDIR $PROJECT_HOME/tools/SoftwareSupplyChainSecurity-v1/

ENTRYPOINT /usr/bin/java -jar -Xms1536m -Xmx1536m $PROJECT_HOME/$PROJECT_NAME.jar
