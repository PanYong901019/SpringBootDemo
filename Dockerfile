LABEL maintainer="panyong <pan_panyong@sina.com>"
LABEL description="Spring Boot Application"

FROM openanolis/anolisos:latest
RUN dnf update -y && dnf install -y java-25-openjdk tzdata iproute net-tools curl && dnf clean all && rm -rf /var/cache/dnf
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone

ENV JAVA_HOME=/usr/lib/jvm/java-25-openjdk
ENV PATH=$JAVA_HOME/bin:$PATH
ENV APP_HOME=/app
ENV SERVER_PORT=1205

WORKDIR $APP_HOME
COPY target/*.jar app.jar

# 创建非 root 用户运行应用
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown appuser:appuser app.jar
USER appuser

# 暴露端口
EXPOSE $SERVER_PORT
# 启动命令
ENTRYPOINT ["java", "-Xbootclasspath/a:./config/", "-jar", "app.jar"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 CMD curl -f http://localhost:$SERVER_PORT/heartbeat || exit 1
