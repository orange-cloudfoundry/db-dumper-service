#!/usr/bin/env sh

cd db-dumper-travis/travis-logs

for entry in *
do
  echo "------------------$entry-------------------"
  echo "-------------------------------------------"
  cat "$entry"
  echo "-------------------------------------------"
done
