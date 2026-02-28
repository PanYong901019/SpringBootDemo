FROM openanolis/anolisos:latest

LABEL maintainer="panyong <pan_panyong@sina.com>"
LABEL description="Spring Boot Application"

RUN dnf update -y && dnf install -y tzdata curl wget tar && dnf clean all && rm -rf /var/cache/dnf
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone
RUN mkdir -p /tmp/java-download/ && wget https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.tar.gz -O /tmp/java-download/java.tar.gz && tar -xzf /tmp/java-download/java.tar.gz -C /tmp/java-download/ && mv /tmp/java-download/jdk* /opt/java && rm -rf /tmp/java-download

ENV JAVA_HOME=/opt/java
ENV PATH=$JAVA_HOME/bin:$PATH
ENV APP_HOME=/app
ENV SERVER_PORT=1205

WORKDIR $APP_HOME
COPY target/*.jar app.jar

# 暴露端口
EXPOSE $SERVER_PORT
# 启动命令
ENTRYPOINT ["java", "-Xbootclasspath/a:./config/", "-jar", "app.jar"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 CMD curl -f http://localhost:$SERVER_PORT/heartbeat || exit 1
