#!/usr/bin/env bash

# templates meant to be resolved at build or install time
VAR_DIR="${var}"

#
VAR_DIR="${VAR_DIR:-/var}"

VAR_RUN_DIR="${VAR_DIR}/run"
PID_BASE_DIR="${VAR_RUN_DIR}/hazelcast"
VAR_LOG_DIR="${VAR_DIR}/log"
LOG_BASE_DIR="${VAR_LOG_DIR}/hazelcast"

#
function find_HID() {
    local PATTERN="${VAR_RUN_DIR}/hazelcast/$1*"
    local NUM_ENTRIES=$(ls -d ${PATTERN} 2>/dev/null | wc -l | xargs)
    if [ ${NUM_ENTRIES} -gt 1 ] ; then
        echo "Error: Ambiguous command: $NUM_ENTRIES Hazelcast members$([[ ! -z $1 ]] && echo " matching $1")"
        return 1
    elif [ ${NUM_ENTRIES} -eq 0 ] ; then
        echo "Error: No Hazelcast member$([[ ! -z $1 ]] && echo " matching $1")"
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
    PID_DIR="${PID_BASE_DIR}/$1"
    PID_FILE="${PID_DIR}/hazelcast.pid"
}

#
function find_LOG_FILE() {
    LOG_DIR="${LOG_BASE_DIR}/$1"
    LOG_FILE="${LOG_DIR}/hazelcast.log"
}

#
function read_PID() {
    find_PID_FILE $1
    if [ ! -f "${PID_FILE}" ]; then
        echo "$1            Error: No PID file for Hazelcast member in $PID_DIR"
        return 1
    fi

    PID=$(cat "${PID_FILE}" 2>/dev/null);
    if [ -z "${PID}" ]; then
        echo "$1            Error: Cannot read PID for Hazelcast member from $PID_FILE"
        return 1
    fi
}

#
function make_HID() {
    PID_DIR=$(mktemp -d "${PID_BASE_DIR}/XXXX")
    HID=${PID_DIR: -4}
    PID_FILE="${PID_DIR}/hazelcast.pid"
    LOG_DIR="${LOG_BASE_DIR}/$HID"
    mkdir -p ${LOG_DIR}
    LOG_FILE="${LOG_DIR}/hazelcast.log"
}

#
function help_single_ID() {
    echo
    echo "ID can be omitted when a single Hazelcast member was started."
    echo "It can be shortened to any unambiguous prefix of a Hazelcast member ID."
}

#
function help_ID_PREFIX() {
    echo
    echo "ID_PREFIX selects Hazelcast member IDs, it can be a glob pattern."
    echo "If left out, all started Hazelcast members will be selected."
}

#
case "$1" in
    --help | -h)
        help
        exit 0;;
esac
