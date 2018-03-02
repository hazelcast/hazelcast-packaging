#!/usr/bin/env bash

#
. $(dirname "$0")/env.sh

#
PRG="$0"
find_HID_LIST "$1"

echo "ID"
for hid in "${HID_LIST[@]}"
do
    echo $hid
done
