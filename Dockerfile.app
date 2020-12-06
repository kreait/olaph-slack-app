FROM openjdk:15-alpine

VOLUME /tmp

COPY ["build/libs/*.jar","/app/application.jar"]

WORKDIR /app

CMD ["sh","-c","java -XX:MaxRAMPercentage=90 -jar -Dserver.port=$PORT $JAVA_OPTS application.jar"]
