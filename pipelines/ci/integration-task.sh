#!/usr/bin/env sh

set -e
cd db-dumper-service

bin/install-binaries
export MAVEN_OPTS="${MAVEN_OPTS} -Djava.security.egd=file:/dev/./urandom"
mvn clean integration-test failsafe:verify