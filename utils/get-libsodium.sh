#!/usr/bin/env bash
# Installs libsodium from git source, quietly.

sudo apt-get -qq -y install \
     build-essential libtool autotools-dev \
     automake checkinstall check git yasm

git clone https://github.com/jedisct1/libsodium.git
cd libsodium
git checkout tags/1.0.0
./autogen.sh
./configure && make check

sudo make install
sudo ldconfig

sudo apt-get -qq -y remove \
     build-essential libtool autotools-dev \
     automake checkinstall check git yasm
