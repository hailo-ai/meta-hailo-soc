#!/bin/bash

# sysexits.h equivalents
declare -r EX_OK=0              # successful termination
declare -r EX_USAGE=64          # command line usage error
declare -r EX_DATAERR=65        # data format error
declare -r EX_NOINPUT=66        # cannot open input
declare -r EX_NOUSER=67         # addressee unknown
declare -r EX_NOHOST=68         # host name unknown
declare -r EX_UNAVAILABLE=69    # service unavailable
declare -r EX_SOFTWARE=70       # internal software error
declare -r EX_OSERR=71          # system error (e.g., can't fork)
declare -r EX_OSFILE=72         # critical OS file missing
declare -r EX_CANTCREAT=73      # can't create (user) output file
declare -r EX_IOERR=74          # input/output error
declare -r EX_TEMPFAIL=75       # temp failure; user is invited to retry
declare -r EX_PROTOCOL=76       # remote error in protocol
declare -r EX_NOPERM=77         # permission denied
declare -r EX_CONFIG=78         # configuration error

declare -r SCRIPT=$(basename "$0")
declare -i PID=$$
declare -r LOCK_FILE="/tmp/$SCRIPT.lock"

declare F_ACTION="none"

trap 'trap_func' TERM INT
trap_func()
{
    echo "$SCRIPT interrupted. Exiting."
    rm -rf "$LOCK_FILE"
}

# @brief script usage.
function usage()
{
  echo "Setup USB gadget for SWU mode via configfs"
  echo "Usage: $SCRIPT [OPTIONS]"
  echo "       -h|--help: show this help."
  echo "       -a|--action: start/stop."
  return 0
}

start()
{
    # 1. Boot → built-in g_hailo starts (dual-config: RFS + SWU)
    # 2. Host uploads RFS via config 1
    # 3. Device boots into uploaded rootfs, then:

    # 4. Disable built-in gadget (frees UDC 280000.cdns-usb3)
    echo disable > /sys/kernel/hailo_gadget/hailo_gadget || {
        local rc=$?
        logger -t "$SCRIPT" "Failed to disable built-in gadget (rc=$rc)"
        return $rc
    }

    # 5. Create SWU-only gadget via configfs
    GADGET_DIR=/sys/kernel/config/usb_gadget/g_swu
    mkdir -p $GADGET_DIR
    cd $GADGET_DIR || {
        local rc=$?
        logger -t "$SCRIPT" "Failed to create gadget directory: $GADGET_DIR (rc=$rc)"
        return $rc
    }

    # Set USB IDs (same as built-in for host compatibility)
    echo 0x0B05 > idVendor
    echo 0x1D6F > idProduct
    echo 0x0100 > bcdDevice
    # Device class (match hailo.c: USB_CLASS_VENDOR_SPEC)
    echo 0xff > bDeviceClass
    echo 0x00 > bDeviceSubClass
    echo 0x00 > bDeviceProtocol

    # Device strings
    mkdir -p strings/0x409
    echo "Hailo Technologies Ltd." > strings/0x409/manufacturer
    echo "UGen300 USB Loader"      > strings/0x409/product
    echo "H10-DEV-001"             > strings/0x409/serialnumber

    # Create SWU function instance
    mkdir -p functions/hailo_swu_load.0

    # Create config 1 with SWU function
    mkdir -p configs/c.1/strings/0x409
    echo "Hailo Software Update Mode" > configs/c.1/strings/0x409/configuration
    echo 896 > configs/c.1/MaxPower

    # Link function to config
    ln -s functions/hailo_swu_load.0 configs/c.1/

    # 6. Bind to UDC → USB re-enumerates with SWU-only
    echo "280000.cdns-usb3" > UDC || {
        local rc=$?
        logger -t "$SCRIPT" "Failed to bind gadget to UDC (rc=$rc)"
        return $rc
    }

    # Host sees: same VID/PID, SWU as config 1
    logger -t "$SCRIPT" "SWU gadget started successfully"
    return 0
}

function stop()
{
    # Unbind from UDC
    echo "" > /sys/kernel/config/usb_gadget/g_swu/UDC || {
        local rc=$?
        logger -t "$SCRIPT" "Failed to unbind gadget from UDC (rc=$rc)"
        return $rc
    }

    # Remove symlinks and directories (reverse order)
    rm /sys/kernel/config/usb_gadget/g_swu/configs/c.1/hailo_swu_load.0
    rmdir /sys/kernel/config/usb_gadget/g_swu/configs/c.1/strings/0x409
    rmdir /sys/kernel/config/usb_gadget/g_swu/configs/c.1
    rmdir /sys/kernel/config/usb_gadget/g_swu/functions/hailo_swu_load.0
    rmdir /sys/kernel/config/usb_gadget/g_swu/strings/0x409
    rmdir /sys/kernel/config/usb_gadget/g_swu || {
        local rc=$?
        logger -t "$SCRIPT" "Failed to clean up gadget configfs directories (rc=$rc)"
        return $rc
    }

    logger -t "$SCRIPT" "SWU gadget stopped successfully"
    return 0
}

# @brief main.
function main()
{
    case $F_ACTION in
    start) start ;;
    stop) stop ;;
    *) echo "Invalid action: $F_ACTION"; usage; return $EX_USAGE ;;
    esac
    return $EX_OK
}

declare -i main_ret=0
#------------------------------------------------------------------------------
#                               MAIN
#------------------------------------------------------------------------------
(
    flock -xn 200 || { echo "$SCRIPT is already running."; exit $EX_TEMPFAIL; }

    OPTS_SHORT="ha:"   # Legal short options
    OPTS_LONG="help,action:" # Legal long options
    # $PARSED_OPTIONS will contain the legal arguments out of "$@".
    PARSED_OPTIONS=$(getopt -n "$0" -o $OPTS_SHORT -l $OPTS_LONG -- "$@") || {
        rm -rf "$LOCK_FILE"
        exit $EX_USAGE
    }

    eval set -- "$PARSED_OPTIONS"     # Set the positional parameters ($1, $2, etc)

    while true; do
        case "$1" in
        --help|-h) usage && exit 0 ;;
        --action|-a)  F_ACTION="$2"; shift 2 ;;
        --) shift; break ;;
        *) echo "Argument [$1] not handled."; shift; break ;;
        esac
    done

    main "$@"
    main_ret=$?
    rm -rf "$LOCK_FILE"
    exit $main_ret
) 200>"$LOCK_FILE"
