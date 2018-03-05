#!/usr/bin/env bash

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
