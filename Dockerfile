FROM java:8

MAINTAINER Laurens Van Houtven <_@lvh.io>

ENV CDN b25518159d104e8997f8-d5f47ab458856b6ee0c2780ddf5e291c.ssl.cf5.rackcdn.com
ENV LIBSODIUM_DEB libsodium-1.0.10_amd64.deb
RUN wget https://$CDN/$LIBSODIUM_DEB
RUN dpkg -i $LIBSODIUM_DEB

RUN mkdir -p /usr/src/icecap
COPY target/uberjar/icecap-*-standalone.jar /usr/src/icecap/icecap.jar

WORKDIR /usr/src/icecap
CMD ["java", "-jar", "icecap.jar"]
