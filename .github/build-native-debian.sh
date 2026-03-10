#!/usr/bin/env bash

set -ex

cd "$(dirname "$(dirname "$0")")"

DEBIAN_VERSION=$(grep '^VERSION_ID=' /etc/os-release | cut -d'"' -f2)
if [[ "$DEBIAN_VERSION" == "10" ]]; then
  echo "deb http://archive.debian.org/debian buster main" > /etc/apt/sources.list
  echo "deb http://archive.debian.org/debian-security buster/updates main" >> /etc/apt/sources.list
  echo "deb http://archive.debian.org/debian buster-updates main" >> /etc/apt/sources.list
fi

apt-get update -y

apt-get install -y wget apt-transport-https gpg

if [[ "$(uname -m)" == "riscv64" ]] || [[ "$(uname -m)" == "armv7l" ]]; then
    # JDK 8 is not available on RISC-V 64 or ARMv5 (reported as armv7l in uname)
    jdk_package=default-jdk
else
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
    echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list
    apt-get update -y
    jdk_package=temurin-8-jdk
fi

apt-get install -y $jdk_package

apt-get install -y --no-install-recommends make gcc libc6-dev texinfo
# Needs to be split, otherwise a newer version of OpenJDK is pulled
apt-get install -y --no-install-recommends ant
rm archive/*
ant jar && ant archive-platform-jar

