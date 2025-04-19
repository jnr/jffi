#!/usr/bin/env bash

set -ex

cd "$(dirname "$(dirname "$0")")"

# Add stretch for Java 8
#cat <<END > /etc/apt/sources.list.d/stretch.list
#deb http://deb.debian.org/debian stretch main
#deb http://security.debian.org/debian-security stretch/updates main
#END

apt-get update -y

apt-get install -y wget apt-transport-https gpg
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list

apt-get update -y
if [[ "$CROSS_ARCH" == "riscv64" ]]; then
    # JDK 8 is not available on RISC-V 64
    apt-get install -y temurin-17-jdk
else
    apt-get install -y temurin-8-jdk
fi

apt-get install -y --no-install-recommends make gcc libc6-dev texinfo
# Needs to be split, otherwise a newer version of OpenJDK is pulled
apt-get install -y --no-install-recommends ant
rm archive/*
ant jar && ant archive-platform-jar

