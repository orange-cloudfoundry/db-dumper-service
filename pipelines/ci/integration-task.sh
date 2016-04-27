#!/usr/bin/env sh

set -e
cd db-dumper-service

bin/install-binaries

mvn integration-test failsafe:verify