#!/bin/sh

set -ex

cd "$(dirname "$(dirname "$0")")"

apt-get update -y
apt-get install -y --no-install-recommends openjdk-8-jdk-headless ant make gcc libc6-dev texinfo
rm archive/*
ant jar && ant archive-platform-jar

