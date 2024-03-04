#!/bin/sh

set -e
exec 2>&1

MMC_DEV_NUM=$1
MMC_PART_NUM=$2
resize2fs /dev/mmcblk${MMC_DEV_NUM}p${MMC_PART_NUM}
