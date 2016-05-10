#!/usr/bin/env bash

set -e
cd db-dumper-service

bin/install-binaries
export JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:///dev/urandom"

mvn -B test -Dtest.groups=acceptance-tests