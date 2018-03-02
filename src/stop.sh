#!/usr/bin/env bash

#
. $(dirname "$0")/pid-utils.sh

#
PRG="$0"
find_HID "$1" || exit 1

if read_PID "$HID" ; then
   kill -15 "${PID}"
   rm "${PID_FILE}"
   rmdir "${PID_DIR}"
   echo "Hazelcast instance $HID with PID ${PID} shut down"
   exit 0
fi
