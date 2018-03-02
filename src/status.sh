#!/usr/bin/env bash

#
. $(dirname "$0")/pid-utils.sh

#
PRG="$0"
find_HID_LIST "$1"

#echo "HID_LIST: ${HID_LIST[@]}"

printf "%-8s%-8s%-8s\n" "ID" "PID" "STATUS"
for hid in "${HID_LIST[@]}"
do
    if read_PID $hid; then
       ps -p "${PID}" > /dev/null
       STATUS=$?
       if [[ STATUS -eq 0 ]]; then
           printf "%-8s%-8s%s\n" "$hid" "$PID" "Running"
       else
           printf "%-8s%-8s%s\n" "$hid" "$PID" "Not running. Please remove ${PID_DIR}"
       fi
    fi
done

