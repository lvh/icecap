#!/usr/bin/env bash
# Installs libsodium from git source, quietly.
sudo apt-get update -qq
sudo apt-get -y install build-essential libtool autotools-dev\
     automake checkinstall check git yasm

git clone git://github.com/jedisct1/libsodium.git
cd libsodium
git checkout tags/0.7.0
./autogen.sh
./configure && make check

sudo make install
sudo ldconfig
