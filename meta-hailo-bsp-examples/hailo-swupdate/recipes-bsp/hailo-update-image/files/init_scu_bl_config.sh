#!/bin/sh

set -e
exec 2>&1

dd if=/dev/zero of=/dev/mtdblock0 bs=4096 count=1 seek=6