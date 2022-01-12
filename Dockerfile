FROM openjdk:16-jdk-slim

COPY build/libs/campbot-*-all.jar /usr/local/lib/campbot.jar

RUN mkdir /bot
RUN mkdir /data
WORKDIR /bot

ENTRYPOINT ["java", "-Dorg.slf4j.simpleLogger.log.campbot=info", "-Xms2G", "-Xmx2G", "-jar", "/usr/local/lib/campbot.jar"]