# 要求使用openeuler作为基础镜像
FROM openeuler/openeuler:22.03-lts

# PROJECT_NAME 填写你的项目名字
ENV PROJECT_NAME anti-poisoning
# PROJECT_HOME 构建成镜像之后，存放的目录位置
ENV PROJECT_HOME /usr/local/${PROJECT_NAME}

RUN rm -f $PROJECT_HOME
RUN mkdir $PROJECT_HOME
RUN yum update -y && yum install -y python3 python3-pip python3-devel gcc java-1.8.0-openjdk rpm rubygems git maven mongo-java-driver curl curl-devel && \
    pip3 install pyyaml wheel yara_python==4.2.0
VOLUME /tmp

# 拉取代码
RUN git clone -b master http://source.openeuler.sh/openMajun/anti-poisoning.git $PROJECT_HOME
WORKDIR $PROJECT_HOME/
RUN mvn clean install -s setting-new.xml

# install RASP
ARG PUBLIC_USER
ARG PUBLIC_PASSWORD
RUN git clone https://$PUBLIC_USER:$PUBLIC_PASSWORD@github.com/Open-Infra-Ops/plugins  $PROJECT_HOME/plugins \
    && cp $PROJECT_HOME/plugins/armorrasp/rasp.tgz $PROJECT_HOME \
    && chown -R root:root $PROJECT_HOME/rasp.tgz && chmod 755 -R $PROJECT_HOME/rasp.tgz \
    && tar zxf $PROJECT_HOME/rasp.tgz \
    && rm -rf $PROJECT_HOME/plugins 


# 设置编码
ENV LANG C.UTF-8
# 设置时区
ENV TZ=Asia/Shanghai

WORKDIR $PROJECT_HOME/tools/SoftwareSupplyChainSecurity-v1/

ENTRYPOINT /usr/bin/java -javaagent:$PROJECT_HOME/rasp/rasp.jar -jar -Xms2600m -Xmx2600m $PROJECT_HOME/target/$PROJECT_NAME-0.0.1-SNAPSHOT.jar --logging.file.name=$PROJECT_HOME/tools/SoftwareSupplyChainSecurity-v1/service.out
