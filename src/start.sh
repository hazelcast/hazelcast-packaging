#!/bin/sh

# templates meant to be resolved at build or install time
HAZELCAST_VERSION=${hazelcast_version}
VAR_RUN_DIR="${var_run}"

#
PRG="$0"
PRGDIR=`dirname "$PRG"`
mkdir -p "${VAR_RUN_DIR}/hazelcast"
PIDDIR=$(mktemp -d "${VAR_RUN_DIR}/hazelcast/XXXX")
HZ_ID=${PIDDIR: -4}
PID_FILE="${PIDDIR}/hazelcast.pid"

if [ $JAVA_HOME ]
then
	echo "JAVA_HOME found at $JAVA_HOME"
	RUN_JAVA=$JAVA_HOME/bin/java
else
	echo "JAVA_HOME environment variable not available"
    RUN_JAVA=`which java 2>/dev/null`
fi

if [ -z $RUN_JAVA ]
then
    echo "Java could not be found in your system!"
    echo "Please install Java 1.6 or higher in your PATH or set JAVA_HOME appropriately"
    exit 1
fi

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

echo "########################################"
echo "# RUN_JAVA=$RUN_JAVA"
echo "# JAVA_OPTS=$JAVA_OPTS"
echo "# Starting now...."
echo "########################################"

PID=$(cat "${PID_FILE}" 2>/dev/null);
if [ -z "${PID}" ]; then
    echo "PID for Hazelcast instance is written to location: {$PID_FILE}"
    $RUN_JAVA -server $JAVA_OPTS com.hazelcast.core.server.StartServer &
    HZ_PID=$!
    echo ${HZ_PID} > ${PID_FILE}
    echo "PID: ${HZ_PID}"
    echo "Member ID: ${HZ_ID}"
else
    echo "Another Hazelcast instance (PID=${PID}) is already started in this folder"
    exit 1
fi
