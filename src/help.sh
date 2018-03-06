#!/usr/bin/env bash

#
function help() {
    echo "Usage:  hazelcast-member help COMMAND"
    echo
    echo "Display help text for the given COMMAND."
    echo
    echo "Type hazelcast-member with no options to display a list of supported COMMANDs."
}

#
. $(dirname "$0")/utils.sh

$(dirname "$0")/hazelcast-member "$1" --help
