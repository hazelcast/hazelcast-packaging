#!/usr/bin/env bash

# display help for this command
function helper() {
    echo "Usage:  $CMD list [ID_PREFIX ...]"
    echo
    echo "Print IDs of started Hazelcast members."
    help_ID_PREFIX
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
source $(dirname "$0")/utils.sh

#
PRG="$0"
find_HID_LIST "$@"

for hid in "${HID_LIST[@]}"
do
    echo $hid
done
