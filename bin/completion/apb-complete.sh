#!/bin/sh

_apb() 
{
    cur="${COMP_WORDS[COMP_CWORD]}"
    # this is necessary for bash to crrectly handle ':'
    COMP_WORDBREAKS=${COMP_WORDBREAKS//:}
    COMPREPLY=( $(apb --complete $COMP_CWORD ${COMP_WORDS[*]}) )
    return 0
}
complete -F _apb apb
export _apb
