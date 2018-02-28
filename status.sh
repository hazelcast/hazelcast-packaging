#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
#HAZELCAST_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
PID_FILE=$HAZELCAST_HOME/hazelcast_instance.pid

if [ ! -f "${PID_FILE}" ]; then
    echo "No hazelcast instance is running."
    exit 0
fi

PID=$(cat "${PID_FILE}");
if [ -z "${PID}" ]; then
    echo "No hazelcast instance is running."
    exit 0
else
   ps -p "${PID}" > /dev/null
   STATUS=$?
   if [[ STATUS -eq 0 ]]; then
       echo "Hazelcast Instance with PID ${PID} is running."
   else
       echo "Hazelcast Instance with PID ${PID} is NOT running. Please remove ${PID_FILE}"
   fi
   exit 0
fi
