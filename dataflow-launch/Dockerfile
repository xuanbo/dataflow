FROM openjdk:8-jdk-alpine

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' > /etc/timezone

RUN mkdir /apps
RUN mkdir /apps/tmp
RUN mkdir /apps/spark-tmp
RUN mkdir /apps/spark-warehouse

COPY dataflow-launch/target/dataflow-launch-1.0.0-SNAPSHOT.jar /apps/dataflow.jar

WORKDIR /apps

EXPOSE 9090
EXPOSE 4040

ENV JAVA_OPTS="\
-Xms4g \
-Xmx4g \
"

ENTRYPOINT java ${JAVA_OPTS} -Djava.io.tmpdir=/apps/tmp -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -jar /apps/dataflow.jar