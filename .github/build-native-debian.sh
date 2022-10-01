#!/bin/sh

set -ex

cd "$(dirname "$(dirname "$0")")"

# Add stretch for Java 8
cat <<END > /etc/apt/sources.list.d/stretch.list
deb http://deb.debian.org/debian stretch main
deb http://security.debian.org/debian-security stretch/updates main
END

apt-get update -y
apt-get install -y --no-install-recommends openjdk-8-jdk-headless make gcc libc6-dev texinfo
# Needs to be split, otherwise a newer version of OpenJDK is pulled
apt-get install -y --no-install-recommends ant
rm archive/*
ant jar && ant archive-platform-jar

