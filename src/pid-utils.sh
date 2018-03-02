#!/usr/bin/env bash

# templates meant to be resolved at build or install time
VAR_RUN_DIR="${var_run}"

#
VAR_RUN_DIR="${VAR_RUN_DIR:-/var/run}"

#
function find_HID() {
    local PATTERN="${VAR_RUN_DIR}/hazelcast/$1*"
    local NUM_ENTRIES=$(ls -d ${PATTERN} 2>/dev/null | wc -l | xargs)
    if [ ${NUM_ENTRIES} -gt 1 ] ; then
        echo "Ambiguous command: $NUM_ENTRIES Hazelcast instances$([[ ! -z $1 ]] && echo " matching $1")"
        return 1
    elif [ ${NUM_ENTRIES} -eq 0 ] ; then
        echo "No Hazelcast instance$([[ ! -z $1 ]] && echo " matching $1")"
        return 1
    fi
    local ENTRY=$(ls -d ${PATTERN} 2>/dev/null)
    HID=${ENTRY: -4}
}

#
function find_HID_LIST() {
    local PATTERN="${VAR_RUN_DIR}/hazelcast/$1*"
    local DIR_ENTRIES=$(ls -d ${PATTERN} 2>/dev/null)
    HID_LIST=()
    for ENTRY in ${DIR_ENTRIES} ; do
          HID_LIST+=(${ENTRY: -4})
    done
}

#
function find_PID_FILE() {
    PID_DIR="${VAR_RUN_DIR}/hazelcast/$1"
    PID_FILE="${PID_DIR}/hazelcast.pid"
}

#
function read_PID() {
    find_PID_FILE $1
    if [ ! -f "${PID_FILE}" ]; then
        echo "$1:   No PID file for Hazelcast instance in $PID_DIR"
        return 1
    fi

    PID=$(cat "${PID_FILE}" 2>/dev/null);
    if [ -z "${PID}" ]; then
        echo "$1:   Cannot read PID for Hazelcast instance from $PID_FILE"
        return 1
    fi
}
