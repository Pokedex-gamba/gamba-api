FROM openjdk:17-oracle

ARG JAR_FILE=target/*.jar

EXPOSE 8889/tcp

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
