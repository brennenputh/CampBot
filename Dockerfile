FROM openjdk:16-jdk-slim

COPY build/libs/CampBot-*-all.jar /usr/local/lib/campbot.jar

RUN mkdir /bot
RUN mkdir /data
WORKDIR /bot

ENTRYPOINT ["java", "-Xms2G", "-Xmx2G", "-jar", "/usr/local/lib/campbot.jar"]