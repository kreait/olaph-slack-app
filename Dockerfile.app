FROM openjdk:15-alpine

VOLUME /tmp

COPY ["build/libs/*.jar","/app/application.jar"]

WORKDIR /app

EXPOSE 8080

CMD ["sh","-c","java -XX:MaxRAMPercentage=90 -jar application.jar"]
