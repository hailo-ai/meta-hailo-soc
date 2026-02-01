#!/bin/bash

set -e

# Script options
FIRMWARE_DEV=$(/etc/get_boot_dev.sh --firmware)

# read the scu_bl_cfg file provided as argument to the device
# the size read is 4K bytes
# file index provided as the second argument, either config 1 or 2
function read_scu_bl_cfg_file_from_device()
{
    scu_bl_cfg_filename_to_read=$1
    scu_bl_cfg_file_offset=$2

    dd if=/dev/${FIRMWARE_DEV} of=${scu_bl_cfg_filename_to_read} bs=4096 count=1 skip=${scu_bl_cfg_file_offset} > /dev/null 2>/dev/null

    return 0
}

# function to update the read-only mode of the firmware device
# needed in case of emmc (mmcblk0boot0 / mmcblk1boot0)
function update_read_only_mode()
{
    desired_mode=$1

    if [[ "${FIRMWARE_DEV}" == "mmcblk0boot0" || "${FIRMWARE_DEV}" == "mmcblk1boot0" ]]; then
        echo ${desired_mode} > /sys/block/${FIRMWARE_DEV}/force_ro
        if [[ $(cat /sys/block/${FIRMWARE_DEV}/force_ro) -ne ${desired_mode} ]]; then
            echo "Failed to update force_ro to ${desired_mode} for ${FIRMWARE_DEV}"
            return 1
        fi

    # for flash, no need to change read-only mode
    elif [[ "${FIRMWARE_DEV}" == "mtdblock0" ]]; then
        return 0
    else
        echo "update_read_only_mode: Wrong FIRMWARE_DEV -${FIRMWARE_DEV}"
        return 0
    fi

    return 0
}

# write a scu_bl_cfg file to the device, after enlarging it to 4K bytes - zero-padded
# verify the write has succeeded by reading back the data and comparing it to the original
# Arguments:
# $1 - scu_bl_cfg file name to write
# $2 - scu_bl_cfg file index, either config 1 or 2
# $3 - is truncate needed to 4K bytes
function write_and_confirm_scu_bl_cfg_file_to_device()
{
    input_file=$1
    scu_bl_cfg_file_offset=$2
    need_truncate=$3

    return_value=0

    if [ $scu_bl_cfg_file_offset -ne 5 ] && [ $scu_bl_cfg_file_offset -ne 6 ]; then
        return 1
    fi

    tmp_filename_to_write="/tmp/tmp_scu_bl_cfg_to_write.bin"
    tmp_readback_filename="/tmp/tmp_scu_bl_cfg_readback.bin"

    cp ${input_file} ${tmp_filename_to_write}

    if [ $need_truncate -eq 1 ]; then
        truncate -s 4096 ${tmp_filename_to_write}
    fi

    # enable write permission (if needed), and write the file to the device
    update_read_only_mode 0
    update_mode_result=$?

    if [ ${update_mode_result} -eq 1 ]; then
        echo "failed to disable read-only mode on /dev/${FIRMWARE_DEV}!"
        return 1
    fi

    # write file to the relevant firmware device
    dd if=${tmp_filename_to_write} of=/dev/${FIRMWARE_DEV} bs=4096 count=1 seek=${scu_bl_cfg_file_offset} > /dev/null 2>/dev/null

    # cause all data to be flushed to emmc before disabling write permission
    sync

    # disable write permission (if needed)
    if ! update_read_only_mode 1 ; then
        return 1
    fi

    # read back the file from the device
    if ! read_scu_bl_cfg_file_from_device ${tmp_readback_filename} ${scu_bl_cfg_file_offset}; then
        echo "failed reading back scu_bl_cfg into file ${tmp_readback_filename}!"
        return_value=1
    fi

    if ! cmp -s -n 4096 ${tmp_filename_to_write} ${tmp_readback_filename}; then
        echo "failed to write ${input_file} to /dev/${FIRMWARE_DEV} at offset ${scu_bl_cfg_file_offset}"
        return_value=1
    fi
    
    rm ${tmp_filename_to_write}
    rm ${tmp_readback_filename}

    return $return_value
}

function verify_existing_scu_bl_cfg_validity()
{
    tmp_scu_bl_cfg_1_filename="/tmp/tmp_scu_bl_cfg_1.bin"
    tmp_scu_bl_cfg_2_filename="/tmp/tmp_scu_bl_cfg_2.bin"

    # Initialize the script result variable
    return_value=0

    # Read scu_bl_cfg 1 from flash at offset 0x5000
    if ! read_scu_bl_cfg_file_from_device ${tmp_scu_bl_cfg_1_filename} 5; then
        echo "failed reading scu_bl_cfg 1 into file /tmp/tmp_scu_bl_cfg_1.bin!"
        return 1
    fi

    # Read scu_bl_cfg 2 from flash at offset 0x6000
    if ! read_scu_bl_cfg_file_from_device ${tmp_scu_bl_cfg_2_filename} 6; then
        echo "failed reading scu_bl_cfg 2 into file /tmp/tmp_scu_bl_cfg_2.bin!"
        return_value=1
    fi

    # Verify CRC of scu_bl_cfg_1, success = 0, failure = non-zero
    /etc/verify_file_crc ${tmp_scu_bl_cfg_1_filename}
    verification_result=$?

    # if scu_bl_cfg_1 is valid - compare it with scu_bl_cfg_2
    if [ $verification_result -eq 0 ]; then
        if ! cmp -s -n 4096 ${tmp_scu_bl_cfg_1_filename} ${tmp_scu_bl_cfg_2_filename}; then
            # Write scu_bl_cfg_1 to scu_bl_cfg_2, indicate no truncate needed
            if ! write_and_confirm_scu_bl_cfg_file_to_device ${tmp_scu_bl_cfg_1_filename} 6 0; then
                echo "verify_existing_scu_bl_cfg_validity: failed to write and confirm ${tmp_scu_bl_cfg_1_filename}"
            fi
        fi
    else
        echo "verify_existing_scu_bl_cfg_validity: failed to verify CRC of ${tmp_scu_bl_cfg_1_filename}"

        # Verify CRC of scu_bl_cfg_2, success = 0, failure = non-zero
        /etc/verify_file_crc ${tmp_scu_bl_cfg_2_filename}
        verification_result=$?

        # if scu_bl_cfg_2 is valid - copy it to scu_bl_cfg_1
        if [ $verification_result -eq 0 ]; then
            # Write scu_bl_cfg_2 to scu_bl_cfg_1, indicate no truncate needed
            if ! write_and_confirm_scu_bl_cfg_file_to_device ${tmp_scu_bl_cfg_2_filename} 5 0; then
                echo "verify_existing_scu_bl_cfg_validity: failed to write and confirm ${tmp_scu_bl_cfg_2_filename}"
                return_value=1
            fi
        else
            echo "both scu_bl_cfg_1 and scu_bl_cfg_2 are NOT valid, aborting!"
            return_value=1
        fi
    fi

    # Remove the temporary files
    rm ${tmp_scu_bl_cfg_1_filename}
    rm ${tmp_scu_bl_cfg_2_filename}

    return $return_value
}

function write_scu_bl_cfg_bin()
{
    local scu_bl_cfg=$1

    # Before writing, verify validity of existing config files 1 and 2
    if ! verify_existing_scu_bl_cfg_validity; then
        echo "error verifying the existing scu_bl_cfg, not writing new config"
        exit 1
    else
        # Write scu_bl_cfg at offset 0x5000, indicate truncate needed 
        if ! write_and_confirm_scu_bl_cfg_file_to_device ${scu_bl_cfg} 5 1; then 
            echo "failed writing ${scu_bl_cfg} to scu_bl_cfg_1 location , aborting!"
            exit 1
        fi

        # Write the same scu_bl_cfg also at offset 0x6000, indicate truncate needed
        if !  write_and_confirm_scu_bl_cfg_file_to_device ${scu_bl_cfg} 6 1; then
            echo "failed writing ${scu_bl_cfg} to scu_bl_cfg_2 location , aborting!"
            exit 1
        fi
    fi

    return 0
}

function usage()
{
    echo "Set SW image used for next boot in SCU bootloader configuration in QSPI flash."
    echo "Usage: set_sw_image.sh [a/b/remote_update]"
    echo ""

    return 0
}

if [ $# -ne 1 ]; then
    usage
    exit 1
fi

next_boot_copy=$1

if [[ ${next_boot_copy} != "a" && ${next_boot_copy} != "b" && ${next_boot_copy} != "remote_update" ]]; then
    usage
    exit 1
fi

if [ ${next_boot_copy} = "a" ]; then
    write_scu_bl_cfg_bin "/etc/scu_bl_cfg/scu_bl_cfg_a.bin"
fi

if [ ${next_boot_copy} = "b" ]; then
    write_scu_bl_cfg_bin "/etc/scu_bl_cfg/scu_bl_cfg_b.bin"
fi

if [ ${next_boot_copy} = "remote_update" ]; then
    write_scu_bl_cfg_bin "/etc/scu_bl_cfg/scu_bl_cfg_a_remote_update.bin"
fi
