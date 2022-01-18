#!/bin/bash

attempts=0
max_attempts=30
until $(curl --output /dev/null --silent --fail "http://localhost:8081/health"); do
  if [ ${attempts} -eq ${max_attempts} ];then
      echo "Hazelcast MC not responding"
      cat hz-mc.log
      ps -aux
      exit 1
  fi
  printf '.'
  attempts=$(($attempts+1))
  sleep 2
done
