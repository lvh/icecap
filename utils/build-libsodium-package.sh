#!/usr/bin/env bash

set -e
set -x

LIBSODIUM_VERSION="1.0.8"

docker build -t build-libsodium-deb - <<EOF
FROM debian

MAINTAINER Laurens Van Houtven <_@lvh.io>

ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update
RUN apt-get -y install ruby-dev gcc build-essential libtool autotools-dev automake checkinstall check git yasm

ENV LIBSODIUM_VERSION $LIBSODIUM_VERSION

RUN gem install fpm

RUN mkdir /tmp/build-libsodium
WORKDIR /tmp/build-libsodium

RUN git clone https://github.com/jedisct1/libsodium.git
WORKDIR /tmp/build-libsodium/libsodium
RUN git checkout tags/$LIBSODIUM_VERSION
RUN ./autogen.sh
RUN ./configure --prefix=/usr
RUN make check

RUN mkdir sodium-install
RUN make install DESTDIR=\$(readlink -e sodium-install)

RUN fpm -s dir -t deb \
    -n libsodium -v $LIBSODIUM_VERSION \
    -C sodium-install \
    --deb-shlibs="libsodium 13 libsodium (>= $LIBSODIUM_VERSION)" \
    -p libsodium-VERSION_ARCH.deb \
    usr/lib
EOF

PKG_NAME="libsodium-${LIBSODIUM_VERSION}_amd64.deb"
docker run -i build-libsodium-deb cat $PKG_NAME > $PKG_NAME
docker rmi build-libsodium-deb
