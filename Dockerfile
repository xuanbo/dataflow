FROM openjdk:8-jdk-alpine

MAINTAINER xuanbo <1345545983@qq.com>

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' > /etc/timezone

RUN mkdir /apps
RUN mkdir /apps/tmp

COPY dataflow-launch/target/dataflow.jar /apps
COPY dataflow-launch/src/main/resources/application.yaml /apps/application.yaml

WORKDIR /apps

EXPOSE 9090
EXPOSE 4040

ENV JAVA_OPTS="\
-Xmx4g \
-Xms4g \
"

ENTRYPOINT java ${JAVA_OPTS} -Djava.io.tmpdir=/apps/tmp -Djava.security.egd=file:/dev/./urandom -Dspring.config.location=/apps/application.yaml -jar /apps/dataflow.jar