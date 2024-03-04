#!/bin/bash

set -e

# Script options
declare -i F_HELP=0
declare -i F_BATCH=0
declare -i F_DUAL=0
declare -i F_DONT_SWITCH=0
declare F_SERVER=""
declare F_REMOTE_FILENAME=""
declare F_LOCAL_FILENAME=""
declare -i F_LOGS_PORT=12345

declare -r SINGLE_MODE_MSG="The system will go to reboot and SW update will executed automatically upon next boot.
                    You can trace SW update logs by executing 'nc -u -l -k 12345' from your Host (you can change the port via -p)."

function usage()
{
    echo "Run SW update."
    echo "Usage: [OPTIONS]"
    echo "  -h|--help: show help"
    echo "  -b|--batch: batch mode"
    echo "  -d|--dual: dual (A/B) mode"
    echo "  -s|--server IP: TFTP server IP address to fetch update file from"
    echo "  -r|--remote-file FILE: .swu filename to fetch from TFTP server"
    echo "  -l|--local-file PATH: (dual mode only) local .swu file path"
    echo "  -p|--logs-port PORT: (single mode only) UDP port to send logs to. default is 12345"
    echo "  -o|--dont-switch: (dual mode only) Don't switch to the updated image after update"
    echo ""
    echo "Note: In dual mode, the update is done directly from linux, and reboot is required afterwards to boot to updated image."
    echo "Note: In single mode, $SINGLE_MODE_MSG"
    echo "Estimated SW update duration: 2-3 minutes"

    return 0
}

function single_mode()
{
    echo "$SINGLE_MODE_MSG"

    # Prepare SW update for next system boot
    if [[ -n "${F_SERVER}" ]]; then
        fw_setenv serverip "${F_SERVER}"
    fi
    if [[ -n "${F_REMOTE_FILENAME}" ]]; then
        fw_setenv swupdate_update_filename "${F_REMOTE_FILENAME}"
    fi
    fw_setenv swupdate_server_udp_logging_port "${F_LOGS_PORT}"
    fw_setenv bootmenu_0 "Autodetect=run boot_swupdate_mmc"
    fw_setenv bootdelay 0
    echo "Rebooting is about to start..."
    reboot

}

function set_update_copy()
{
    read -r current_copy < <(/etc/get_sw_image.sh --boot)
    if [ "${current_copy}" = "a" ]; then
        update_copy="b"
    else
        update_copy="a"
    fi
}

function dual_mode()
{
    if [[ -n "${F_REMOTE_FILENAME}" ]]; then
        if [[ -z "${F_SERVER}" ]]; then
            echo "Missing server IP for TFTP download"
            return 1
        fi
        if [[ -n "${F_LOCAL_FILENAME}" ]]; then
            echo "error: please specify either local or remote file, not both"
            return 1
        fi
        cd /tmp
        echo "Downloading ${F_REMOTE_FILENAME} from ${F_SERVER} via TFTP..."
        tftp -g -r "${F_REMOTE_FILENAME}" "${F_SERVER}"
        F_LOCAL_FILENAME="/tmp/${F_REMOTE_FILENAME}"
    else
        if [[ -z "${F_LOCAL_FILENAME}" ]]; then
            echo "error: please specify either local or remote file"
            return 1
        fi
    fi
    set_update_copy
    swupdate -i "${F_LOCAL_FILENAME}" -v -m -M -e "stable,copy-${update_copy}"

    if [ ${F_DONT_SWITCH} -eq 0 ]; then
        /etc/set_sw_image.sh "${update_copy}"
    fi
    echo "SWUpdate finished."
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
    if [ $F_DUAL -eq 1 ]; then
        dual_mode
    else
        single_mode
    fi

    return 0
}

echo "run_swupdate: Start execution"
OPTS_SHORT="hbds:r:l:po"
OPTS_LONG="help,batch,dual,server:,remote-file:,local-file:,logs-port:,dont-switch"

PARSED_OPTIONS=$(getopt -n "$0" -o $OPTS_SHORT -l $OPTS_LONG -- "$@")
# Bad option flags, abort...
[ $? -ne 0 ] && exit 1
eval set -- "$PARSED_OPTIONS"

while true; do
    case "$1" in
        --help|-h) F_HELP=1; shift 1;;
        --batch|-b) F_BATCH=1; shift 1;;
        --dual|-d) F_DUAL=1; shift 1;;
        --server|-s) F_SERVER="$2"; shift 2;;
        --remote-file|-r) F_REMOTE_FILENAME="$2"; shift 2;;
        --local-file|-l) F_LOCAL_FILENAME="$2"; shift 2;;
        --logs-port|-p) F_LOGS_PORT="$2"; shift 2;;
        --dont-switch|-o) F_DONT_SWITCH=1; shift 1;;
        -- ) shift; break;;
        *) echo "Argument [$1] not handled"; shift; break;;
    esac
done

main
exit
