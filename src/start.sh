#!/usr/bin/env bash

# templates meant to be resolved at build or install time
HAZELCAST_VERSION=${hazelcast_version}

# display help for this command
function helper() {
    echo "Usage:  $CMD start [options ...]"
    echo
    echo "Start a Hazelcast member."
    echo
    echo "Options:"
    echo "  -c or --config <file>"
    echo "        Use <file> for Hazelcast configuration."
    echo "        If <file> is '-', read the standard input."
    echo
    echo "  -cn or --cluster-name <name>"
    echo "        Use the specified cluster <name> (default: 'dev')."
    echo
    echo "  -cp or --cluster-password <password>"
    echo "        Use the specified cluster <password> (default: 'password')."
    echo
    echo "  -fg or --foreground"
    echo "        Run in the foreground."
    echo
    echo "  -i or --interface <interface>"
    echo "        Bind to the specified <interface> (default: bind to all interfaces)."
    echo
    echo "  -j or --jar <path>"
    echo "        Add <path> to Hazelcast class path."
    echo
    echo "  -p or --port <port>"
    echo "        Bind to the specified <port> (default: 5701)."
    echo
    echo "  -v or --verbose"
    echo "        Show extra info about running environment."
    echo
    echo "  -J or --JAVA_OPTS <options>"
    echo "        Specify additional Java <options>."
}

# echo available options
function optionlist() {
    echo -v --verbose -j --jar -c --config -J --JAVA_OPTS -p --port -i --interface -cn --cluster-name -cp --cluster-password -fg --foreground
}

# echo available commands; ID for Hazelcast member IDs
function commandlist() {
    :
}

#
source $(dirname "$0")/utils.sh

#
mkdir -p "${PID_BASE_DIR}"
mkdir -p "${LOG_BASE_DIR}"

#
declare VERBOSE CP CONF
declare CONF_PORT CONF_IF CONF_IF_ENABLED CONF_BIND_ANY CONF_GROUP_NAME CONF_GROUP_PASSWORD CONF_FOREGROUND
function default_config() {
    CONF_PORT="${CONF_PORT:-5701}"
    CONF_IF="${CONF_IF:-}"
    CONF_IF_ENABLED="${CONF_IF_ENABLED:-false}"
    CONF_BIND_ANY="${CONF_BIND_ANY:-true}"
    CONF_GROUP_NAME="${CONF_GROUP_NAME:-dev}"
    CONF_GROUP_PASSWORD="${CONF_GROUP_PASSWORD:-dev-pass}"
    CONF="${CONF:-${CONF_DIR}/hazelcast.xml}"
}

function check_config_file() {
    if [[ ${CONF} = '-' ]] ; then
        return 0
    elif [[ ! -f ${CONF} ]] ; then
        echo "Error: ${CONF} not found!"
        exit 1
    fi
}

# parse options
while (( "$#" ))
do
    case "$1" in
        -v | --verbose)
            VERBOSE=1
            shift
            ;;
        -j | --jar)
            if [[ -z "$2" ]] ; then
                printf "Error: missing <path> after %s\n\n" "$1"
                helper && exit 1
            fi
            CP="${CP}:$2"
            shift 2
            ;;
        -J | --JAVA_OPTS)
            if [[ -z "$2" ]] ; then
                printf "Error: missing <options> after %s\n\n" "$1"
                helper && exit 1
            fi
            JAVA_OPTS="${JAVA_OPTS} $2"
            shift 2
            ;;
        -c | --config)
            if [[ -z "$2" ]] ; then
                printf "Error: missing <file> after %s\n\n" "$1"
                helper && exit 1
            fi
            if [ "${CONF}" ]; then
                printf "Error: More than one config file.\n\n"
                helper && exit 1
            fi
            CONF="$2"
            check_config_file
            shift 2
            ;;
        -p | --port)
            if [[ -z "$2" ]] ; then
                printf "Error: missing <port> after %s\n\n" "$1"
                helper && exit 1
            fi
            CONF_PORT="$2"
            shift 2
            ;;
        -i | --interface)
            if [[ -z "$2" ]] ; then
                printf "Error: missing <interface> after %s\n\n" "$1"
                helper && exit 1
            fi
            CONF_IF="$2"
            CONF_IF_ENABLED="true"
            CONF_BIND_ANY="false"
            shift 2
            ;;
        -cn | --cluster-name)
            if [[ -z "$2" ]] ; then
                printf "Error: missing <name> after %s\n\n" "$1"
                helper && exit 1
            fi
            CONF_GROUP_NAME="$2"
            shift 2
            ;;
        -cp | --cluster-password)
            if [[ -z "$2" ]] ; then
                printf "Error: missing <password> after %s\n\n" "$1"
                helper && exit 1
            fi
            CONF_GROUP_PASSWORD="$2"
            shift 2
            ;;
        -fg | --foreground)
            CONF_FOREGROUND=1
            shift
            ;;
        *)
            printf "Invalid argument: %s\n\n" "$1"
            helper && exit 1
    esac
done

# config options or config file
if [ -n "$CONF" -a '(' "$CONF_PORT" -o "$CONF_IF" -o "$CONF_GROUP_NAME" -o "$CONF_GROUP_PASSWORD" ')' ]; then
    printf "Error: Config option(s) and config file are mutually exclusive.\n\n"
    helper && exit 1
fi

find_RUN_JAVA

#### you can enable following variables by uncommenting them

#### minimum heap size
# MIN_HEAP_SIZE=1G

#### maximum heap size
# MAX_HEAP_SIZE=1G


if [ "x$MIN_HEAP_SIZE" != "x" ]; then
	JAVA_OPTS="$JAVA_OPTS -Xms${MIN_HEAP_SIZE}"
fi

if [ "x$MAX_HEAP_SIZE" != "x" ]; then
	JAVA_OPTS="$JAVA_OPTS -Xmx${MAX_HEAP_SIZE}"
fi

export CLASSPATH="$HAZELCAST_HOME/lib/hazelcast-all-${HAZELCAST_VERSION}.jar"
if [ ${CP} ]; then
	CLASSPATH="${CLASSPATH}${CP}"
fi

default_config

check_config_file

make_HID

JAVA_OPTS="$JAVA_OPTS -Dgroup.name=${CONF_GROUP_NAME}"
JAVA_OPTS="$JAVA_OPTS -Dgroup.password=${CONF_GROUP_PASSWORD}"
JAVA_OPTS="$JAVA_OPTS -Dnetwork.interface=${CONF_IF}"
JAVA_OPTS="$JAVA_OPTS -Dinterfaces.enabled=${CONF_IF_ENABLED}"
JAVA_OPTS="$JAVA_OPTS -Dbind.any=${CONF_BIND_ANY}"
JAVA_OPTS="$JAVA_OPTS -Dnetwork.port=${CONF_PORT}"

if [[ ${CONF} = '-' ]]; then
    CONF=$(mktemp -q "${PID_DIR}/${CMD}.XXXXXX")
    if [ $? -ne 0 ]; then
           echo "Error: Can't create temp file"
           exit 1
    fi
    cat >${CONF}
fi

if [ ${CONF} ]; then
    JAVA_OPTS="$JAVA_OPTS -Dhazelcast.config=${CONF}"
fi

#
if [ ${VERBOSE} ] ; then
    echo "Java executable: $RUN_JAVA"
    echo "JAVA_OPTS=$JAVA_OPTS"
    echo "CLASSPATH=$CLASSPATH"
    [ ${CONF} ] && echo "Hazelcast config: $CONF"
fi

PID=$(cat "${PID_FILE}" 2>/dev/null);
if [ -z "${PID}" ]; then
    echo "ID:  ${HID}"
    [ ${VERBOSE} ] && echo "PID file for this Hazelcast member: $PID_FILE"
    [ ${VERBOSE} -a ! ${CONF_FOREGROUND} ] && echo "Permanent logfile for this Hazelcast member: $LOG_FILE"
    HZ_CMD="${RUN_JAVA} -server ${JAVA_OPTS} com.hazelcast.core.server.StartServer"
    [ ${VERBOSE} ] && echo "Command line: $HZ_CMD"
    if [ ${CONF_FOREGROUND} ] ; then
        set -m
        ${HZ_CMD} &
    else
        nohup ${HZ_CMD} >>"${LOG_FILE}" 2>&1 &
    fi
    HZ_PID=$!
    echo ${HZ_PID} > ${PID_FILE}
    if [ ${CONF_FOREGROUND} ] ; then
        fg %- >/dev/null
    else
        [ ${VERBOSE} ] && echo "PID: ${HZ_PID}"
    fi
else
    echo "Error: Another Hazelcast instance (PID=${PID}) is already started in this folder"
    exit 1
fi

exit 0
