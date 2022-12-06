# 要求使用openeuler作为基础镜像
FROM openeuler/openeuler:22.03-lts

# PROJECT_NAME 填写你的项目名字
ENV PROJECT_NAME anti-poisoning
# PROJECT_HOME 构建成镜像之后，存放的目录位置
ENV PROJECT_HOME /usr/local/${PROJECT_NAME}

RUN rm -f $PROJECT_HOME
RUN mkdir $PROJECT_HOME
RUN yum update -y && yum install -y python3 python3-pip python3-devel gcc java-1.8.0-openjdk rpm rubygems git maven mongo-java-driver curl curl-devel && \
    pip3 install pyyaml wheel yara_python
VOLUME /tmp

# 拉取代码
RUN git clone -b dev http://source.openeuler.sh/openMajun/anti-poisoning.git $PROJECT_HOME
WORKDIR $PROJECT_HOME/
RUN mvn clean install -s setting-new.xml

# 设置编码
ENV LANG C.UTF-8
# 设置时区
ENV TZ=Asia/Shanghai

WORKDIR $PROJECT_HOME/tools/SoftwareSupplyChainSecurity-v1/

ENTRYPOINT /usr/bin/java -jar -Xms1536m -Xmx1536m $PROJECT_HOME/target/$PROJECT_NAME-0.0.1-SNAPSHOT.jar --logging.file.name=$PROJECT_HOME/tools/SoftwareSupplyChainSecurity-v1/service.out
