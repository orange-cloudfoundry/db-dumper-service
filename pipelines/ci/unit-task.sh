#!/usr/bin/env sh

set -e
cd db-dumper-service

mkdir -p src/main/resources/binaries/mongodb/bin
echo "" > src/main/resources/binaries/mongodb/bin/mongodump
echo "" > src/main/resources/binaries/mongodb/bin/mongorestore

mkdir -p src/main/resources/binaries/mysql/bin
echo "" > src/main/resources/binaries/mysql/bin/mysql
echo "" > src/main/resources/binaries/mysql/bin/mysqldump

mkdir -p src/main/resources/binaries/postgresql/bin
echo "" > src/main/resources/binaries/postgresql/bin/pg_dump
echo "" > src/main/resources/binaries/postgresql/bin/psql

mkdir -p src/main/resources/binaries/redis/bin
echo "" > src/main/resources/binaries/redis/bin/rutil

chmod -R +x src/main/resources/binaries

export JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:///dev/urandom"

mvn -B test