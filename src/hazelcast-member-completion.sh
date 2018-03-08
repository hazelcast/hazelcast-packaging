#!/usr/bin/env bash

function __hazelcast_member() {
    local CMD=$1

    # current word being autocompleted
    local cur=${COMP_WORDS[COMP_CWORD]}
    local verb=_
    [[ $COMP_CWORD -gt 1 ]] && verb=${COMP_WORDS[1]}

    COMPREPLY=() ;

    # get the list of all commands
    local COMMANDS=( $("${CMD}" --commandlist) )

    OPTIONS___=$($CMD --optionlist)

    # mapping command => options
    for i in "${COMMANDS[@]}"
    do
        eval "OPTIONS__$i=\$(\"${CMD}\" $i --optionlist)"
    done

    if [[ ${cur} == -*  ]]; then
        COMPREPLY=( $(compgen -W "$(eval echo \${OPTIONS__$verb} 2>/dev/null)" -- ${cur}) )
    elif [[ "${COMMANDS[*]}" == *"$verb"* ]] ; then
        local objects=$("${CMD}" ${verb} --commandlist 2>/dev/null)
        if [[ $objects == 'ID' ]] ; then
            objects=$("${CMD}" list ${cur})
            COMPREPLY=( $(compgen -W "${objects}" -X "!${cur}*") )
        else
            COMPREPLY=( $(compgen -W "${objects}" -- ${cur}) )
        fi
    else
        COMPREPLY=( $(compgen -W "${COMMANDS[*]}" -- ${cur}) )
    fi
}

complete -F __hazelcast_member hazelcast-member
