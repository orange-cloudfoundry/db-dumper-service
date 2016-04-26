#!/usr/bin/env sh

set -e
cd db-dumper-service

bin/install-binaries

chmod -R +x src/main/resources/binaries

mvn test