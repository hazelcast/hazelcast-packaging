#!/usr/bin/env bash

# display help for this command
function helper() {
    echo "Usage:  eval \"\$($CMD init -)\""
    echo
    echo "Enable bash autocompletion for $CMD."
}

# echo available options
function optionlist() {
    echo -
}

# echo available commands; ID for Hazelcast member IDs
function commandlist() {
    :
}

#
source $(dirname "$0")/utils.sh

function trycat() {
    [ -f "$1" ] && cat "$1" && exit 0
}

if [[ $1 = '-' ]] ; then
    trycat $(dirname "$0")/hazelcast-member-completion.sh
    trycat $(dirname "$0")/../../etc/bash_completion.d/hazelcast-member
    trycat ${VAR_DIR}/../etc/bash_completion.d/hazelcast-member
    trycat /usr/local/etc/bash_completion.d/hazelcast-member
    trycat /etc/bash_completion.d/hazelcast-member
    exit 1
fi

helper
