#!/bin/bash

set -e


# Script options
declare -i F_HELP=0
declare -i F_BOOT=0
declare -i F_NEXT=0

function usage()
{
    echo "Get SW image used for boot."
    echo "Prints 'a' for image A, or 'b' for image B."
    echo "Usage: [OPTIONS]"
    echo "  -h|--help: show help"
    echo "  -b|--boot: get image used for current boot"
    echo "  -n|--next: get image used for next boot (from SCU bootloader configuration)"
    echo ""

    return 0
}

function get_boot_copy()
{
    read -r qspi_flash_ab_offset < /sys/devices/soc0/qspi_flash_ab_offset
    if [ $((qspi_flash_ab_offset)) -eq 0 ]; then
        copy="a"
    else
        copy="b"
    fi
}

function get_scu_bl_copy()
{
    # read the field from QSPI flash
    scu_bl_qspi_flash_ab_offset=$(dd if=/dev/mtdblock0 bs=1 count=4 skip=$((0x6000)) 2>/dev/null | hexdump -e '"%x"')
    if [ "$scu_bl_qspi_flash_ab_offset" = "0" ]; then
        copy="a"
    else
        copy="b"
    fi
}

function main()
{
    if [ $F_HELP -eq 1 ]; then
        usage && return 0
    fi

    if [[ $F_BOOT -eq 1 && $F_NEXT -eq 1 ]]; then
        echo "Error: -b and -n are mutually exclusive."
        return 1
    fi

    if [[ $F_BOOT -eq 0 && $F_NEXT -eq 0 ]]; then
        echo "Error: select -b or -n."
        return 1
    fi

    if [ $F_BOOT -eq 1 ]; then
        get_boot_copy
    fi
    if [ $F_NEXT -eq 1 ]; then
        get_scu_bl_copy
    fi

    echo ${copy}
    return 0
}

OPTS_SHORT="hbn"
OPTS_LONG="help,boot,next"

PARSED_OPTIONS=$(getopt -n "$0" -o $OPTS_SHORT -l $OPTS_LONG -- "$@")
# Bad option flags, abort...
[ $? -ne 0 ] && exit 1
eval set -- "$PARSED_OPTIONS"

while true; do
    case "$1" in
        --help|-h) F_HELP=1; shift 1;;
        --boot|-b) F_BOOT=1; shift 1;;
        --next|-n) F_NEXT=1; shift 1;;
        -- ) shift; break;;
        *) echo "Argument [$1] not handled"; shift; break;;
    esac
done

main
exit
