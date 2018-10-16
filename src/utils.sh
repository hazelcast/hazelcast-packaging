#!/usr/bin/env bash

# templates meant to be resolved at build or install time
VAR_DIR="${var}"
ETC_DIR="${etc}"

#
VAR_DIR="${VAR_DIR:-/var}"
ETC_DIR="${ETC_DIR:-/etc}"

PID_BASE_DIR="${VAR_DIR}/run/hazelcast"
LOG_BASE_DIR="${VAR_DIR}/log/hazelcast"
CONF_DIR="${ETC_DIR}/hazelcast"

#
function find_HID() {
    local PATTERN="${PID_BASE_DIR}/$1*"
    local NUM_ENTRIES=$(ls -d ${PATTERN} 2>/dev/null | wc -l | xargs)
    if [ ${NUM_ENTRIES} -gt 1 ] ; then
        echo "Error: Ambiguous command: $NUM_ENTRIES Hazelcast members$([[ ! -z $1 ]] && echo " matching $1")"
        return 1
    elif [ ${NUM_ENTRIES} -eq 0 ] ; then
        echo "Error: No Hazelcast member$([[ ! -z $1 ]] && echo " matching $1")"
        return 1
    fi
    local ENTRY=$(ls -d ${PATTERN} 2>/dev/null)
    HID=$(basename $ENTRY)
}

#
function find_HID_LIST() {
    local ARGS=$@
    if [ $# -eq 0 ] ; then
        ARGS=("?")
    fi
    HID_LIST=()
    for i in ${ARGS[@]} ; do
        local PATTERN="${PID_BASE_DIR}/$i*"
        local DIR_ENTRIES=$(ls -d ${PATTERN} 2>/dev/null)
        for ENTRY in ${DIR_ENTRIES} ; do
            HID_LIST+=($(basename $ENTRY))
        done
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
    HID=$(get_MOBYNAME)
    PID_DIR="${PID_BASE_DIR}/${HID}"
    mkdir -m 0700 "${PID_BASE_DIR}/${HID}"
    if [ $? -ne 0 ] ; then
        echo "Error: Can't create temp directory"
        exit 1
    fi
    PID_FILE="${PID_DIR}/hazelcast.pid"
    LOG_DIR="${LOG_BASE_DIR}/$HID"
    mkdir -m 0700 -p ${LOG_DIR}
    LOG_FILE="${LOG_DIR}/hazelcast.log"
}

#
function find_RUN_JAVA() {
    if [ $JAVA_HOME ]
    then
        RUN_JAVA=$JAVA_HOME/bin/java
    else
        RUN_JAVA=`which java 2>/dev/null`
    fi

    if [ -z $RUN_JAVA ]
    then
        echo "Error: Java not found. Please install Java 1.6 or higher in your PATH or set JAVA_HOME appropriately"
        exit 1
    fi
}

#
function get_MOBYNAME() {
    find_RUN_JAVA
    ${RUN_JAVA} -jar "$HAZELCAST_HOME/lib/mobynames-1.0.jar"
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
        helper
        exit 0
        ;;
    --commandlist)
        commandlist
        exit 0
        ;;
    --optionlist)
        optionlist
        exit 0
        ;;
esac
