#!/bin/sh
# Postinstall for copy-a / copy-b: restore /etc/network/interfaces on the
# freshly written rootfs from the backup saved on the data partition (p5).
#
# The data partition backup is written by either:
#   - run_swupdate.sh (normal dual-mode or single-mode update), or
#   - copy_network_to_data.sh (board-init path, after diskpart recreates p5)
#
# $1 = rootfs partition device (e.g. /dev/mmcblk1p2 or /dev/mmcblk1p4)
#      The data partition (p5) is derived by replacing the trailing partition
#      number: /dev/mmcblk1p2 -> /dev/mmcblk1p5.

set -e
exec 2>&1

ROOTFS_DEVICE=$1
ROOTFS_MOUNTPOINT="/tmp-net-mount"
# Derive the data partition (p5) from the rootfs device.
# Works for both single mode (p2->p5) and dual mode (p2 or p4->p5).
DATA_PART=$(echo "${ROOTFS_DEVICE}" | sed 's/p[0-9]*$/p5/')
DATA_MOUNTPOINT="/tmp-net-data"

mkdir -p "${ROOTFS_MOUNTPOINT}"
mount "${ROOTFS_DEVICE}" "${ROOTFS_MOUNTPOINT}"

if [ -b "${DATA_PART}" ]; then
    mkdir -p "${DATA_MOUNTPOINT}"
    if mount "${DATA_PART}" "${DATA_MOUNTPOINT}" 2>/dev/null; then
        if [ -f "${DATA_MOUNTPOINT}/network/interfaces.saved" ]; then
            cp "${DATA_MOUNTPOINT}/network/interfaces.saved" "${ROOTFS_MOUNTPOINT}/etc/network/interfaces"
            echo "Restored network config from ${DATA_PART}/network/interfaces.saved"
        fi
        umount "${DATA_MOUNTPOINT}"
    fi
    rmdir "${DATA_MOUNTPOINT}" 2>/dev/null || true
fi

umount "${ROOTFS_MOUNTPOINT}"
rmdir "${ROOTFS_MOUNTPOINT}"
