#!/bin/sh

_apb() 
{
    cur="${COMP_WORDS[COMP_CWORD]}"
    COMPREPLY=( $(apb --complete $COMP_CWORD ${COMP_WORDS[*]}) )
    return 0
}
complete -F _apb apb
export _apb
