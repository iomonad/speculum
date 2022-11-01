FROM openjdk:19-jdk-alpine3.14

MAINTAINER iomonad <iomonad@riseup.net>

RUN apk add --update-cache \
    libstdc++ tzdata \
  && rm -rf /var/cache/apk/*

ENV TZ Europe/Paris
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /
COPY target/*/*-standalone.jar speculum.jar

CMD java  -showversion -XshowSettings:vm -jar speculum.jar
