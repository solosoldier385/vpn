# --- Build Stage ---
# 使用包含 Maven 和 Java 17 的镜像
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置构建阶段的工作目录
WORKDIR /build

# --- 第 1 步: 复制并安装父 POM ---
# 父 POM 相对于当前模块目录 (vpn/auth-service/) 的路径是 ../pom.xml
# **重要**: 此 Dockerfile 假设您在 `vpn/auth-service/` 目录下执行 `docker build .` 命令。
# 如果不是，以下所有 `../` 开头的相对路径都会不正确。
COPY ../pom.xml ./pom.xml
# 在 /build 目录下运行，确保 target 是父 POM
RUN mvn install -N -DskipTests

# --- 第 2 步: 复制、构建并安装 common-core ---
# common-core 相对于当前模块目录的路径是 ../common-core
COPY ../common-core/pom.xml ./common-core/pom.xml
COPY ../common-core/src ./common-core/src/
WORKDIR /build/common-core
RUN mvn -f pom.xml clean install -DskipTests

# --- 第 3 步: 复制、构建并安装 common-data ---
WORKDIR /build
# common-data 相对于当前模块目录的路径是 ../common-data
COPY ../common-data/pom.xml ./common-data/pom.xml

# !!!!! V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V V !!!!!
# !!!!! ERROR HAPPENS ON THE NEXT LINE: "failed to compute cache key... /common-data/src: not found" !!!!!
# !!!!! 请再次严格检查以下几点:
# !!!!! 1. 您执行 `docker build` 命令时，终端的当前工作目录【必须】是 `vpn/auth-service/`。
# !!!!!    例如: `cd /path/to/your/project/vpn/auth-service/` 然后执行 `docker build -t yourimage .`
# !!!!! 2. 检查 `vpn/auth-service/` 目录中是否有 `.dockerignore` 文件。
# !!!!! 3. 检查 `vpn/` 目录 (父目录) 中是否有 `.dockerignore` 文件。
# !!!!!    如果存在 `.dockerignore` 文件，请确保其内容没有意外地排除 `common-data/src` 目录或其内容。
# !!!!!    常见的错误排除模式可能包括: `common-data/`, `**/src`, `src/` 等。
# !!!!! 4. 再次确认您本地文件系统中 `vpn/common-data/src` 目录确实存在并且里面有文件。
# !!!!! ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ !!!!!
COPY ../common-data/src ./common-data/src/
WORKDIR /build/common-data
RUN mvn -f pom.xml clean install -DskipTests

# --- 第 4 步: 复制并打包 auth-service ---
WORKDIR /build
# auth-service 的 pom.xml 和 src 相对于当前模块目录 (构建上下文的根) 就是 pom.xml 和 src
COPY pom.xml ./auth-service/pom.xml
COPY src ./auth-service/src/
# 进入 auth-service 目录并执行 package
WORKDIR /build/auth-service
RUN mvn -f pom.xml clean package -DskipTests # 使用 /build/auth-service/pom.xml

# --- Runtime Stage ---
# 使用精简的 JRE 17 镜像作为最终运行环境
FROM eclipse-temurin:17-jre-focal

# 设置应用工作目录
WORKDIR /app

# 从构建阶段复制最终生成的 auth-service JAR 文件
# 根据 auth-service/pom.xml, artifactId 是 auth-service, version 是 1.0-SNAPSHOT
COPY --from=builder /build/auth-service/target/auth-service-1.0-SNAPSHOT.jar ./app.jar

# 暴露 auth-service 监听的端口 (根据 bootstrap.yml 是 8081)
EXPOSE 8081

# 设置容器启动命令 (建议添加 JVM 参数，并通过环境变量管理)
# ENV JAVA_OPTS="-Duser.timezone=Asia/Shanghai -Xms256m -Xmx512m"
# ENTRYPOINT ["java", ${JAVA_OPTS}, "-jar", "app.jar"]
ENTRYPOINT ["java", "-jar", "app.jar"]
