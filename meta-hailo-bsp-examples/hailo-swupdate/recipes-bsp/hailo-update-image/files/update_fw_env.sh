#!/bin/sh

set -e
exec 2>&1

export FS_DEVICE=$1
export FW_ENV_DEVICE=$2
export FW_ENV_OFFSET=$3
echo "update_fw_env.sh"
echo "FS_DEVICE=${FS_DEVICE}, FW_ENV_DEVICE=${FW_ENV_DEVICE}, FW_ENV_OFFSET=${FW_ENV_OFFSET}"

FS_MOUNTPOINT="/tmp-fs-mount"
mkdir ${FS_MOUNTPOINT}
mount ${FS_DEVICE} ${FS_MOUNTPOINT}
sed -e "s|\${FW_ENV_DEVICE}|${FW_ENV_DEVICE}|g" -e "s|\${FW_ENV_OFFSET}|${FW_ENV_OFFSET}|g" ${FS_MOUNTPOINT}/etc/fw_env.config.template > ${FS_MOUNTPOINT}/etc/fw_env.config

# Check if FS_DEVICE contains mmcblk0 or mmcblk1 and set UBOOT_MMC_DEVICE accordingly
if echo "${FS_DEVICE}" | grep -q "mmcblk0"; then
    UBOOT_MMC_DEVICE="mmc1"
    echo "Setting UBOOT_MMC_DEVICE to ${UBOOT_MMC_DEVICE} for mmcblk0"
elif echo "${FS_DEVICE}" | grep -q "mmcblk1"; then
    UBOOT_MMC_DEVICE="mmc2"
    echo "Setting UBOOT_MMC_DEVICE to ${UBOOT_MMC_DEVICE} for mmcblk1"
else
    echo "Unknown device ${FS_DEVICE}, UBOOT_MMC_DEVICE not set"
    umount "${FS_MOUNTPOINT}"
    exit
fi

# Update the uboot env variable 'spl_boot_source' and 'default_spl_boot_source'
# These control where to look for the uboot-tfa image
echo "updating fw_env variables"
fw_setenv --config ${FS_MOUNTPOINT}/etc/fw_env.config spl_boot_source ${UBOOT_MMC_DEVICE}
fw_setenv --config ${FS_MOUNTPOINT}/etc/fw_env.config default_spl_boot_source ${UBOOT_MMC_DEVICE}

umount ${FS_MOUNTPOINT}
rmdir ${FS_MOUNTPOINT}
