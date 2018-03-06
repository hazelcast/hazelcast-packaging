#!/usr/bin/env bash

#
function help() {
    echo "Usage:  hazelcast-member logs ID [-f] [-n #]"
    echo
    echo "Display the logs for Hazelcast member with the given ID."
    echo
    echo "Options:"
    echo "  -f    The -f option causes logs to not stop when end of file is reached, but rather to wait for additional data to be appended to the input.  The -f option is ignored if the standard input is a pipe, but not if it is a FIFO."
    echo "  -n #  Display the specified number of lines."
    help_single_ID
}

#
. $(dirname "$0")/utils.sh

#
PRG="$0"

# get first argument as HID pattern, if not an option
if [[ "${1:0:1}" != '-' ]] ; then
    ARG=$1
    shift
fi

find_HID "$ARG" || exit 1

find_LOG_FILE $HID

tail "$@" "$LOG_FILE"
