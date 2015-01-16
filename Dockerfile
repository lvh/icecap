FROM dockerfile/java:oracle-java8

MAINTAINER Laurens Van Houtven <_@lvh.io>

ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update
RUN apt-get -y upgrade

RUN mkdir -p /usr/src/icecap
COPY . /usr/src/icecap

WORKDIR /usr/src/icecap

RUN ./utils/get-lein.sh

ENV CDN b25518159d104e8997f8-d5f47ab458856b6ee0c2780ddf5e291c.ssl.cf5.rackcdn.com
ENV LIBSODIUM_DEB libsodium-1.0.2_amd64.deb
RUN wget https://$CDN/$LIBSODIUM_DEB
RUN dpkg -i $LIBSODIUM_DEB

ENV LEIN_ROOT 1
RUN lein uberjar
RUN mv target/uberjar/icecap*standalone.jar icecap-standalone.jar

CMD ["java", "-jar", "icecap-standalone.jar"]
