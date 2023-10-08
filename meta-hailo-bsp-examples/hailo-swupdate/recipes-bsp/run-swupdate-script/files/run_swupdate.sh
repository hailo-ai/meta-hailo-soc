#!/bin/bash

# Script options
declare -i F_HELP=0
declare -i F_BATCH=0

declare -r LOG_MSG="The system will go to reboot and SW update will executed automatically upon next boot.
                    You can trace SW update logs by executing 'nc -u -l -k 12345' from your Host.
                    Estimated SW update duration: 2-3 minutes"

function usage()
{
    echo "Prepare and trigger system for SW update."
    echo "Usage: [OPTIONS]"
    echo "  -h|--help: show help"
    echo "  -b|--batch: batch mode"
    echo "Note: $LOG_MSG"

    return 0
}

function main()
{
    if [ $F_HELP -eq 1 ]; then
        usage && return 0
    fi

    while [ $F_BATCH -eq 0 ]; do
        read -p "You are about to start system installation, continue? (yes/no): " choice
        case "$choice" in
            yes|Y) echo "You chose to continue."; break;;
            no|N) echo "You chose to stop, aborting installation."; return 0;;
            *) echo "Invalid input. Please enter 'yes' or 'no'.";;
        esac
    done

    echo "SW Update: starting..."
    echo "$LOG_MSG"

    # Prepare SW update for next system boot
    fw_setenv bootdelay 0
    fw_setenv bootmenu_0 "Autodetect=run boot_swupdate"
    echo "Rebooting is about to start..."
    reboot

    return 0
}

echo "run_swupdate: Start execution"
OPTS_SHORT="hb"
OPTS_LONG="help,batch,"

PARSED_OPTIONS=$(getopt -n "$0" -o $OPTS_SHORT -l $OPTS_LONG -- "$@")
# Bad option flags, abort...
[ $? -ne 0 ] && exit 1
eval set -- "$PARSED_OPTIONS"

while true; do
    case "$1" in
        --help|-h) F_HELP=1; shift 1;;
        --batch|-b) F_BATCH=1; shift 1;;
        -- ) shift; break;;
        *) echo "Argument [$1] not handled"; shift; break;;
    esac
done

main
echo "run_swupdate: End execution"
exit