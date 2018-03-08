#!/usr/bin/env bash

# display help for this command
function helper() {
    echo "Usage:  $CMD help COMMAND"
    echo
    echo "Display help text for the given COMMAND."
    echo
    echo "Type '$CMD' with no options to display a list of supported COMMANDs."
}

# echo available options
function optionlist() {
    :
}

# echo available commands; ID for Hazelcast member IDs
function commandlist() {
    k$(dirname "$0")/${CMD} --commandlist
}

#
. $(dirname "$0")/utils.sh

$(dirname "$0")/${CMD} "$1" --help
