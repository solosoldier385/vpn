# --- Build Stage ---
# 使用包含 Maven 和 Java 17 的镜像
FROM maven:3.9-eclipse-temurin-17 AS builder

# --- 第 1 步: 复制并安装父 POM ---
WORKDIR /build
COPY pom.xml ./pom.xml
RUN mvn install -N -DskipTests

# --- 第 2 步: 复制、构建并安装 common-core ---
WORKDIR /build
COPY common-core/pom.xml ./common-core/
COPY common-core/src ./common-core/src/
WORKDIR /build/common-core
# *** 在此处的 mvn 命令添加 -f pom.xml ***
RUN mvn -f pom.xml clean install -DskipTests

# --- 第 3 步: 复制、构建并安装 common-data ---
WORKDIR /build
COPY common-data/pom.xml ./common-data/
COPY common-data/src ./common-data/src/
WORKDIR /build/common-data
# *** 在此处的 mvn 命令添加 -f pom.xml ***
RUN mvn -f pom.xml clean install -DskipTests

# --- 第 4 步: 复制并打包 user-service ---
WORKDIR /build
COPY user-service/pom.xml ./user-service/
COPY user-service/src ./user-service/src/
WORKDIR /build/user-service
# *** 在此处的 mvn 命令添加 -f pom.xml ***
RUN mvn -f pom.xml clean package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /build/user-service/target/user-service-1.0-SNAPSHOT.jar ./app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]