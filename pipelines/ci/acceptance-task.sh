#!/usr/bin/env bash

set -e
cd db-dumper-service
bin/install-binaries
export JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:///dev/urandom"

mvn test -q -Dtest.groups=acceptance-tests