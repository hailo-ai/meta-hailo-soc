#!/bin/sh

exec 2>&1

DEVICE=$1

e2fsck -f -p "${DEVICE}"
e2fsck_exit=$?
# e2fsck return codes 0 - 3 are considered successful
if [ "${e2fsck_exit}" -ge 4 ]; then
    echo "e2fsck failed with exit code ${e2fsck_exit}"
    exit 1
fi

resize2fs "${DEVICE}"
resize2fs_exit=$?
if [ "${resize2fs_exit}" -ne 0 ]; then
    echo "resize2fs failed with exit code ${resize2fs_exit}"
    exit 2
fi
