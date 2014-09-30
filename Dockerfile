FROM dockerfile/java:oracle-java8

MAINTAINER Laurens Van Houtven <_@lvh.io>

ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update
RUN apt-get -y upgrade

RUN mkdir -p /usr/src/icecap
COPY . /usr/src/icecap

WORKDIR /usr/src/icecap

RUN ./utils/get-lein.sh
RUN ./utils/get-libsodium.sh

ENV LEIN_ROOT 1
RUN lein uberjar
RUN mv target/uberjar/icecap*standalone.jar icecap-standalone.jar

CMD ["java", "-jar", "icecap-standalone.jar"]
