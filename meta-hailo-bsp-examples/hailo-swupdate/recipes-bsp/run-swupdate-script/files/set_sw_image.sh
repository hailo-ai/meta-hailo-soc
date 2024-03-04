#!/bin/bash

set -e


function num_to_uint32le() {
    local n=$(($1))
    printf "%b" $(printf '\\x%02x\\x%02x\\x%02x\\x%02x' \
                        $(($n & 0xFF)) \
                        $((($n & 0xFF00) >> 8)) \
                        $((($n & 0xFF0000) >> 16)) \
                        $((($n & 0xFF000000) >> 24)))
}

function edit_qspi_flash_ab_offset()
{
    qspi_flash_ab_offset=$1
    scu_bl_config_file="/tmp/scu_bl_config.bin"

    dd if=/dev/mtdblock0 of=${scu_bl_config_file} bs=4096 count=1 skip=6 2>/dev/null

    num_to_uint32le ${qspi_flash_ab_offset} | dd conv=notrunc of=${scu_bl_config_file} 2>/dev/null

    dd if=${scu_bl_config_file} of=/dev/mtdblock0 bs=4096 count=1 seek=6 2>/dev/null
}

function usage()
{
    echo "Set SW image used for next boot in SCU bootloader configuration in QSPI flash."
    echo "Usage: set_sw_image.sh [a/b]"
    echo ""

    return 0
}

if [ $# -ne 1 ]; then
    usage
    exit 1
fi

next_boot_copy=$1

if [[ ${next_boot_copy} != "a" && ${next_boot_copy} != "b" ]]; then
    usage
    exit 1
fi

if [ ${next_boot_copy} = "a" ]; then
    edit_qspi_flash_ab_offset 0
fi

if [ ${next_boot_copy} = "b" ]; then
    edit_qspi_flash_ab_offset 0x78000
fi
