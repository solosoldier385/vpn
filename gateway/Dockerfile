# --- Build Stage ---
# 使用包含 Maven 和 Java 17 的镜像
FROM maven:3.9-eclipse-temurin-17 AS builder

# --- 第 1 步: 复制并安装父 POM ---
WORKDIR /build
# 只复制父 POM 文件
COPY pom.xml ./pom.xml
# 运行 mvn install -N 只安装父 POM 本身到本地仓库 (-N = non-recursive)
RUN mvn install -N -DskipTests

# --- 第 2 步: 复制、构建并安装 common-core ---
# gateway 依赖 common-core
WORKDIR /build
# 复制 common-core (POM + 源代码)
COPY common-core/pom.xml ./common-core/
COPY common-core/src ./common-core/src/
# 进入 common-core 目录并执行 install
WORKDIR /build/common-core
RUN mvn -f pom.xml clean install -DskipTests

# --- 第 3 步: 复制、构建并安装 common-data (可选但保持一致性) ---
# 虽然 gateway 的 pom.xml 没有直接列出 common-data，但可能存在间接依赖或为了构建流程一致性包含它
WORKDIR /build
# 复制 common-data (POM + 源代码)
COPY common-data/pom.xml ./common-data/
COPY common-data/src ./common-data/src/
# 进入 common-data 目录并执行 install
WORKDIR /build/common-data
RUN mvn -f pom.xml clean install -DskipTests

# --- 第 4 步: 复制并打包 gateway ---
WORKDIR /build
# 复制 gateway 模块 (POM + 源代码)
COPY gateway/pom.xml ./gateway/
COPY gateway/src ./gateway/src/
# 进入 gateway 目录并执行 package
WORKDIR /build/gateway
RUN mvn -f pom.xml clean package -DskipTests

# --- Runtime Stage ---
# 使用精简的 JRE 17 镜像作为最终运行环境
FROM eclipse-temurin:17-jre-focal

# 设置应用工作目录
WORKDIR /app

# 从构建阶段复制最终生成的 gateway JAR 文件
# 根据 gateway/pom.xml, artifactId 是 gateway, version 是 1.0-SNAPSHOT
COPY --from=builder /build/gateway/target/gateway-1.0-SNAPSHOT.jar ./app.jar

# 暴露 gateway 监听的端口 (根据 bootstrap.yml 是 8080)
EXPOSE 8080

# 设置容器启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]