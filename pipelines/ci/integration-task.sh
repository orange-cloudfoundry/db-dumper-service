#!/usr/bin/env sh

set -e
cd db-dumper-service

bin/install-binaries

mvn clean integration-test failsafe:verify