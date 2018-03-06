#!/usr/bin/env bash

#
function help() {
    echo "Usage:  hazelcast-member stop ID"
    echo
    echo "Stop the Hazelcast member with the given ID."
    help_single_ID
}

#
. $(dirname "$0")/utils.sh

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
