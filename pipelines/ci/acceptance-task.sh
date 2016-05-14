#!/usr/bin/env bash

set -e
cd db-dumper-service

sed -i "s|http://nyc2\.mirrors\.digitalocean\.com/mariadb|http://mariadb\.kisiek\.net|" bin/install-binaries
bin/install-binaries
export JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:///dev/./urandom"

mvn test -q -Dtest.groups=acceptance-tests