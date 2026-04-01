
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Tận dụng Cache để build nhanh hơn trên Render
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# GIAI ĐOẠN 2: CHẠY ỨNG DỤNG
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 1. Cấu hình múi giờ Việt Nam
RUN apk add --no-cache tzdata
ENV TZ=Asia/Ho_Chi_Minh

# 2. Tạo User an toàn (Không dùng root)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 3. Copy file JAR từ giai đoạn build
COPY --from=builder /build/target/*.jar app.jar

# 4. QUAN TRỌNG: ÉP RAM CHO JAVA
ENV JAVA_OPTS="-Xmx256m -Xss512k -XX:MaxMetaspaceSize=128m"

EXPOSE 8080

# Chạy ứng dụng với các tùy chọn tối ưu RAM
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]