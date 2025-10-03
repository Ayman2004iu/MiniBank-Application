
FROM openjdk:17-jdk-alpine

WORKDIR /app


COPY target/MiniBankApp-0.0.1-SNAPSHOT.jar app.jar

COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh && apk add --no-cache mysql-client

EXPOSE 8080

ENTRYPOINT ["/wait-for-it.sh", "db","3306", "--timeout=60", "--", "java", "-jar", "app.jar"]


