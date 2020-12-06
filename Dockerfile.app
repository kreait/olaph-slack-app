FROM openjdk:15-alpine

VOLUME /tmp

COPY ["build/libs/*.jar","/app/application.jar"]

WORKDIR /app

CMD ["sh","-c","java -XX:MaxRAMPercentage=90 -jar $JAVA_OPTS application.jar"]
