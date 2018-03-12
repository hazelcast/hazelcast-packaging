#!/usr/bin/env bash

# display help for this command
function helper() {
    echo "Usage:  $CMD stop ID"
    echo
    echo "Stop the Hazelcast member with the given ID."
    help_single_ID
}

# echo available options
function optionlist() {
    :
}

# echo available commands; ID for Hazelcast member IDs
function commandlist() {
    echo ID
}

#
. $(dirname "$0")/utils.sh

#
PRG="$0"
find_HID "$1" || exit 1

if read_PID "$HID" ; then
   kill -15 "${PID}"
   rm "${PID_FILE}"
   rm -fr "${PID_DIR}"
   echo "Hazelcast instance $HID with PID ${PID} shut down"
   exit 0
fi
