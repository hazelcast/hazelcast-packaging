#!/usr/bin/env bash

#
function help() {
    echo "Usage:  hazelcast-member list [ID_PREFIX]"
    echo
    echo "Print IDs of started Hazelcast members."
    help_ID_PREFIX
}

#
. $(dirname "$0")/utils.sh

#
PRG="$0"
find_HID_LIST "$1"

for hid in "${HID_LIST[@]}"
do
    echo $hid
done
