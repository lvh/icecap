FROM dockerfile/java:oracle-java8

MAINTAINER Laurens Van Houtven <_@lvh.io>

ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update
RUN apt-get -y upgrade

RUN mkdir -p /usr/src/icecap
COPY . /usr/src/icecap

WORKDIR /usr/src/icecap

RUN ./utils/get-lein.sh

RUN wget https://b25518159d104e8997f8-d5f47ab458856b6ee0c2780ddf5e291c.ssl.cf5.rackcdn.com/libsodium-1.0.0_amd64.deb
RUN dpkg -i libsodium-1.0.0_amd64.deb

ENV LEIN_ROOT 1
RUN lein uberjar
RUN mv target/uberjar/icecap*standalone.jar icecap-standalone.jar

CMD ["java", "-jar", "icecap-standalone.jar"]
