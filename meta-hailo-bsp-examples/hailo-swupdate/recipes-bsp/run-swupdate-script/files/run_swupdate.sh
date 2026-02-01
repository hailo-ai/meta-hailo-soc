#!/bin/bash

# set -e TODO

# exit codes:
declare -i EXIT_SUCCESS=0
declare -i EXIT_GENERAL_ERROR=1
declare -i EXIT_DEVICE_DISCONNECTED=2
declare -i EXIT_SWUPDATE_ERROR=3
declare -i EXIT_LOCAL_SWU_IMAGE_NOT_SPECIFIED=4
declare -i EXIT_LOCAL_SWU_IMAGE_NOT_EXIST=5
declare -i EXIT_CUSTOMER_PUBKEY_NOT_EXIST=6
declare -i EXIT_SET_BOOT_PARTITION_ERROR=7

# Constants
declare -r MIN_FIRMWARE_SIZE_BYTES=$((16 * 1024 * 1024))  # 16MB minimum for dual A/B mode

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

    /etc/set_sw_image.sh remote_update

    echo "Rebooting is about to start..."
    reboot

    return 0
}

function set_update_copy()
{
    read -r current_copy < <(/etc/get_sw_image.sh --boot)
    if [ "${current_copy}" = "a" ]; then
        update_copy="b"
    else
        update_copy="a"
    fi

    return 0
}

function dual_mode()
{
    CMA_NON_REUSABLE_VALUE=$(cat /proc/sys/vm/cma_non_reusable)
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
        # The following is required for 2GB boards
        echo "Changing CMA to reusable for update process..."
        echo 0 > /proc/sys/vm/cma_non_reusable
        echo "Downloading ${F_REMOTE_FILENAME} from ${F_SERVER} via TFTP..."
        tftp -g -r "${F_REMOTE_FILENAME}" "${F_SERVER}"
        F_LOCAL_FILENAME="/tmp/${F_REMOTE_FILENAME}"
    else
        if [[ -z "${F_LOCAL_FILENAME}" ]]; then
            echo "error: please specify either local or remote file"
            return $EXIT_LOCAL_SWU_IMAGE_NOT_SPECIFIED
        fi
        if [ ! -f "${F_LOCAL_FILENAME}" ]; then
            echo "error: local SWU image file ${F_LOCAL_FILENAME} does not exist"
            return $EXIT_LOCAL_SWU_IMAGE_NOT_EXIST
        fi
    fi
    set_update_copy
    echo FILESYSTEM_DEVICE=$(/etc/get_boot_dev.sh --rootfs) > /tmp/swupdate.cfg
    FIRMWARE_DEVICE=$(/etc/get_boot_dev.sh --firmware)
    echo FIRMWARE_DEVICE=$FIRMWARE_DEVICE >> /tmp/swupdate.cfg
    echo FW_ENV_DEVICE=$(/etc/get_boot_dev.sh --fw-env) >> /tmp/swupdate.cfg

    # Check firmware size only for Hailo-10h machines to determine A/B vs single copy mode
    MACHINE_NAME=$(cat /sys/devices/soc0/machine 2>/dev/null || echo "unknown")
    if [ "$MACHINE_NAME" = "Hailo-10h" ]; then
        SIZE_BYTES=$(blockdev --getsize64 /dev/$FIRMWARE_DEVICE 2>/dev/null || echo 0)
        if [ "$SIZE_BYTES" -lt $MIN_FIRMWARE_SIZE_BYTES ]; then
            update_copy="a"
            echo "Small firmware partition detected on Hailo-10h, using single copy mode"
        fi
    fi

    if [ "$MACHINE_NAME" = "Hailo-10h" ]; then
        [ ! -e /etc/customer_pubkey.pem ] && {
            echo "Customer public key file /etc/customer_pubkey.pem not found!"
            return $EXIT_CUSTOMER_PUBKEY_NOT_EXIST
        }
        swupdate -i "${F_LOCAL_FILENAME}" -k /etc/customer_pubkey.pem -v -m -M -e "stable,copy-${update_copy}"
        [ $? -ne 0 ] && {
            echo "SWUpdate failed during main update!"
            return $EXIT_SWUPDATE_ERROR
        }
    else
        swupdate -i "${F_LOCAL_FILENAME}" -v -m -M -e "stable,copy-${update_copy}"
    fi

    if [ ${F_DONT_SWITCH} -eq 0 ]; then
        /etc/set_sw_image.sh "${update_copy}"
        [ $? -ne 0 ] && {
            echo "Failed to set next boot image to ${update_copy}!"
            return $EXIT_SET_BOOT_PARTITION_ERROR
        }

        # Run the script which will cause reset of scratchpad register
        /etc/init.d/hailo_linux_init.sh 99
    fi

    echo "Restoring CMA to original reusable mode..."
    echo "${CMA_NON_REUSABLE_VALUE}" > /proc/sys/vm/cma_non_reusable

    echo "Removing SWUpdate temporary files..."
    rm "${F_LOCAL_FILENAME}"

    echo "SWUpdate finished."

    return 0
}


function main()
{
    local exit_code=0
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
        exit_code=$?
    else
        single_mode
        exit_code=$?
    fi

    return $exit_code
}

echo "run_swupdate: Start execution"
OPTS_SHORT="hbds:r:l:po"
OPTS_LONG="help,batch,dual,server:,remote-file:,local-file:,logs-port:,dont-switch"

PARSED_OPTIONS=$(getopt -n "$0" -o $OPTS_SHORT -l $OPTS_LONG -- "$@") || exit $EXIT_GENERAL_ERROR
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
exit $?
