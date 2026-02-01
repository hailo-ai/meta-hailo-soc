#!/bin/bash

set -e

source /etc/boot_definitions.sh

# Script options
declare -i F_HELP=0
declare -i F_FIRMWARE=0
declare -i F_FW_ENV=0
declare -i F_ROOTFS=0

function usage()
{
    echo "Get device name used for booting in current boot"
    echo "This script is part of the SWupdate logic - only supports SDIO based rootfs, and QSPI/eMMC based firmware."
    echo "Usage: [OPTIONS]"
    echo "  -h|--help: show help"
    echo "  -f|--firmware: get device name used for firmware files in current boot [/dev/{mtdblock0,mmcblk0boot0,mmcblk1boot0}]"
    echo "  -e|--fw-env: get device name used for firmware environment files in current boot [/dev/{mtd0,mmcblk0boot0,mmcblk1boot0}]"
    echo "  -r|--rootfs: get device name used for root filesystem in current boot [/dev/{mmcblk0/mmcblk1}]"
    echo ""

    return 0
}

# receives parameter is_env
# if is_env is 1, return device that will be used for /etc/fw_env.config (when using mtd, character device)
# if is_env is 0, return device that will be used for accessing firmware files (when using mtd, block device)
function get_firmware_device()
{
    local is_env="$1"
    boot_source=$(cat "/sys/devices/soc0/boot_info/active_boot_image_storage")
    case "$boot_source" in
        $BOOT_SOURCE_SPI_FLASH)
            if [ "$is_env" = "1" ]; then
                echo "mtd0"
            else
                echo "mtdblock0"
            fi
            ;;
        $BOOT_SOURCE_EMMC0)
            echo "mmcblk0boot0"
            ;;
        $BOOT_SOURCE_EMMC1)
            echo "mmcblk1boot0"
            ;;
        *)
            echo "Error: Unsupported boot source value [$boot_source] in active_boot_image_storage file"
            return 1
            ;;
    esac

    return 0
}

function get_rootfs_device()
{
    local MACHINE_NAME=$(cat /sys/devices/soc0/machine 2>/dev/null || logger -s "unknown")
    local root_dev=""
    
    if [ "$MACHINE_NAME" = "Hailo-10h" ]; then
        echo "$root_dev"
        return 0
    fi

    root_dev=$(grep -o 'root=/dev/mmcblk[01]' /proc/cmdline | cut -d'/' -f 3)
    if [ -z "$root_dev" ]; then
        echo "Error: Could not find root device in kernel cmdline. Perhaps you are not using SDIO based rootfs?"
        return 1
    fi
    echo "$root_dev"

    return 0
}

function main()
{
    if [ $F_HELP -eq 1 ]; then
        usage && return 0
    fi

    local selected_flags=$((F_FIRMWARE + F_FW_ENV + F_ROOTFS))
    if [ $selected_flags -ne 1 ]; then
        echo "Error: select exactly one of -f, -e, or -r."
        return 1
    fi

    if [ $F_FIRMWARE -eq 1 ]; then
        get_firmware_device 0
    fi
    if [ $F_FW_ENV -eq 1 ]; then
        get_firmware_device 1
    fi
    if [ $F_ROOTFS -eq 1 ]; then
        get_rootfs_device
    fi

    return 0
}

OPTS_SHORT="hfre"
OPTS_LONG="help,firmware,rootfs,fw-env"

PARSED_OPTIONS=$(getopt -n "$0" -o $OPTS_SHORT -l $OPTS_LONG -- "$@") || exit 1
eval set -- "$PARSED_OPTIONS"

while true; do
    case "$1" in
        --help|-h) F_HELP=1; shift 1;;
        --firmware|-f) F_FIRMWARE=1; shift 1;;
        --rootfs|-r) F_ROOTFS=1; shift 1;;
        --fw-env|-e) F_FW_ENV=1; shift 1;;
        -- ) shift; break;;
        *) echo "Argument [$1] not handled"; shift; break;;
    esac
done

main
exit $?