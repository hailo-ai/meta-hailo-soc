#!/bin/sh
# Postinstall for init-partitions-single / init-partitions-dual: write the
# network config saved by save_network_before_init.sh (/tmp/interfaces.saved)
# to the freshly created data partition (p5), where preserve_network.sh will
# read it when the subsequent copy-a / copy-b group runs.
#
# $1 = data partition device (e.g. /dev/mmcblk1p5)
exec 2>&1

DATA_PART=$1
MOUNTPOINT="/tmp-copy-net-data"

if [ ! -f /tmp/interfaces.saved ]; then
    echo "No network config in /tmp, nothing to copy to data partition"
    exit 0
fi

if [ ! -b "${DATA_PART}" ]; then
    echo "Warning: data partition ${DATA_PART} not found"
    exit 0
fi

mkdir -p "${MOUNTPOINT}"
if ! mount "${DATA_PART}" "${MOUNTPOINT}" 2>/dev/null; then
    rmdir "${MOUNTPOINT}"
    echo "Warning: could not mount ${DATA_PART}"
    exit 0
fi

mkdir -p "${MOUNTPOINT}/network"
cp /tmp/interfaces.saved "${MOUNTPOINT}/network/interfaces.saved"
echo "Copied network config to ${DATA_PART}/network/interfaces.saved"

umount "${MOUNTPOINT}"
rmdir "${MOUNTPOINT}"
