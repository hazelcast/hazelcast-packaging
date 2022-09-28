#!/bin/bash

attempts=0
max_attempts=30
until $(curl --silent --fail "127.0.0.1:5701/hazelcast/health/ready"); do
  if [ ${attempts} -eq ${max_attempts} ];then
      echo "Hazelcast not responding"
      cat hz.log
      ps aux
      exit 1
  fi
  printf '.'
  attempts=$(($attempts+1))
  sleep 2
done