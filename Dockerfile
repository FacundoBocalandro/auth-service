FROM openjdk:11-jre-slim
EXPOSE 8080
ADD target/scala-**/auth-service-assembly-0.1.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]