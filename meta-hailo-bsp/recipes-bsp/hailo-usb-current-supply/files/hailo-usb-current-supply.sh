#!/bin/sh

# sysexits.h equivalents
EX_OK=0
EX_IOERR=74
EX_DATAERR=65

# HD3SS3220 - Connection Status Register (0x08)- CURRENT_MODE_DETECT  bits 5:4 - Host USB current limit:
readonly I2C_BUS=0
readonly DEVICE_ADDR=0x47
readonly REG_ADDR=0x08
readonly SYSFS_FILE="/sys/devices/soc0/current_limit"

# Read masked value from I2C register
# Args: bus_id i2c_addr reg_offset reg_shift reg_width
i2c_reg_read()
{
    bus_id=$1
    i2c_dev_addr=$2
    reg_offset=$3
    reg_shift=$4
    reg_width=$5
    reg_mask=$(( ((1 << reg_width) - 1) << reg_shift ))

    reg_val=$(i2cget -y "$bus_id" "$i2c_dev_addr" "$reg_offset")
    rc=$?
    [ $rc -ne 0 ] && return $rc

    echo $(( (reg_val & reg_mask) >> reg_shift ))
    return 0
}

sleep 1

# Read Connection status::mode (reg-offset=0x8 mode-offset=4 mode-width=2)
MODE=$(i2c_reg_read $I2C_BUS $DEVICE_ADDR $REG_ADDR 4 2)
if [ $? -ne 0 ]; then
    logger -s "WARNING: Failed to read HD3SS3220, defaulting to Full Performance"
    exit $EX_OK
fi

# Map the Mode to the Current Limit
case $MODE in
    3) LIMIT=3000 ;;
    1) LIMIT=1500 ;;
    0) LIMIT=900  ;;
    *)
        logger -s "ERROR: Unknown USB mode: $MODE"
        exit $EX_DATAERR
        ;;
esac

if [ -f "$SYSFS_FILE" ]; then
    echo "$LIMIT" > "$SYSFS_FILE"
    logger -s "USB Host Current Limit: ${LIMIT}mA"
fi

exit $EX_OK