#!/usr/bin/env bash

#
. $(dirname "$0")/utils.sh

#
PRG="$0"
find_HID_LIST "$1"

for hid in "${HID_LIST[@]}"
do
    echo $hid
done
