#!/bin/sh
# Preinstall for init-partitions-single / init-partitions-dual: save
# /etc/network/interfaces from the current rootfs to /tmp BEFORE diskpart
# wipes all partitions (including p5).
# copy_network_to_data.sh (postinstall) then writes it to the new p5 so that
# preserve_network.sh can restore it into the freshly written rootfs.
#
# $1 = block device (e.g. /dev/mmcblk1) — the whole MMC device, not a partition
exec 2>&1

DEVICE=$1
MOUNTPOINT="/tmp-save-net-preint"

mkdir -p "${MOUNTPOINT}"

# Try copy-A rootfs (p2) first, then copy-B (p4)
for part_num in 2 4; do
    part="${DEVICE}p${part_num}"
    [ -b "${part}" ] || continue

    if mount -t ext4 -o ro,norecovery "${part}" "${MOUNTPOINT}" 2>/dev/null; then
        if [ -f "${MOUNTPOINT}/etc/network/interfaces" ]; then
            cp "${MOUNTPOINT}/etc/network/interfaces" /tmp/interfaces.saved
            umount "${MOUNTPOINT}"
            rmdir "${MOUNTPOINT}"
            echo "Saved network config from ${part} to /tmp/interfaces.saved"
            exit 0
        fi
        umount "${MOUNTPOINT}"
    fi
done

rmdir "${MOUNTPOINT}" 2>/dev/null || true
echo "No existing rootfs found to save network config from (first init or blank eMMC)"
