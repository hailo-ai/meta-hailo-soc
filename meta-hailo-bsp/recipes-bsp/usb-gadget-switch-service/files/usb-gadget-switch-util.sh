!/bin/bash

declare -r SCRIPT=$(basename "$0")
declare -i PID=$$
declare -r LOCK_FILE="/tmp/$SCRIPT.lock"
declare -r HAILORT_SRV_FFS_PID_FILE="/tmp/hailort_server_functionfs.pid"

declare F_SWITCH_TO_GADGET="none"

trap 'trap_func' TERM INT
trap_func()
{
    echo "$SCRIPT interrupted. Exiting."
    # Clean up daemon if running
    if [ -f "$HAILORT_SRV_FFS_PID_FILE" ]; then
        local daemon_pid=$(cat "$HAILORT_SRV_FFS_PID_FILE")
        if kill -0 "$daemon_pid" 2>/dev/null; then
            kill "$daemon_pid"
        fi
        rm -f "$HAILORT_SRV_FFS_PID_FILE"
    fi
    rm -rf "$LOCK_FILE"
}

# @brief script usage.
function usage()
{
  echo "Switch USB gadgets between ether & hailo"
  echo "Usage: $SCRIPT [OPTIONS]"
  echo "       -h|--help: show this help."
  echo "       -s|--switch: hailo-legacy-enable"
  echo "                    hailo-legacy-disable"
  echo "                    hailo-ffs-enable"
  echo "                    hailo-ffs-disable"
  return 0
}

function is_hailort_server_ffs_running()
{
    [ -f "$HAILORT_SRV_FFS_PID_FILE" ] && kill -0 $(cat "$HAILORT_SRV_FFS_PID_FILE") 2>/dev/null  && return 0
    return 1
}

# @brief USB controller recovery when UDC gets stuck
function udc_recovery_reset()
{
    logger "Attempting USB controller recovery..."
    
    # Step 1: Force soft disconnect if UDC exists
    if [ -e /sys/class/udc/280000.cdns-usb3/soft_connect ]; then
        echo "disconnect" > /sys/class/udc/280000.cdns-usb3/soft_connect 2>/dev/null
        sleep 0.1
    fi
    
    # Step 2: Unbind USB controller driver to reset its state  
    if [ -e /sys/bus/platform/drivers/cdns-usb3/280000.cdns-usb3 ]; then
        echo "280000.cdns-usb3" > /sys/bus/platform/drivers/cdns-usb3/unbind 2>/dev/null
        sleep 0.1
    fi
    
    # Step 3: Rebind USB controller driver
    if [ -e /sys/bus/platform/drivers/cdns-usb3/bind ]; then
        echo "280000.cdns-usb3" > /sys/bus/platform/drivers/cdns-usb3/bind 2>/dev/null
        sleep 0.1
    fi
    
    # Step 4: Re-enable soft connect
    if [ -e /sys/class/udc/280000.cdns-usb3/soft_connect ]; then
        echo "connect" > /sys/class/udc/280000.cdns-usb3/soft_connect 2>/dev/null
    fi
    
    logger "USB controller recovery completed"
}


# @brief Hailo gadget control.
function hailo_gadget_ctrl_legacy()
{
    local state="$1"
    local ret

    if [ ! -e /sys/kernel/hailo_gadget/hailo_gadget ]; then
        logger -s "Hailo gadget sysfs entry not found"
        return 1
    fi

    echo "${state}" > /sys/kernel/hailo_gadget/hailo_gadget
    ret=$?
    if [ $ret -ne 0 ]; then
        logger -s "Failed to set Hailo gadget state to ${state}"
        return $ret
    fi

    return 0
}

function hailo_gadget_ctrl_ffs()
{
    local state="$1"
    local curr_state
    local daemon_pid

    case "$state" in
    "enable")
        if [ ! -f /tmp/hailo_gadget_ffs_setup_ready.flag ]; then
            /usr/bin/hailort_usb_setup.sh setup || return 1
            touch /tmp/hailo_gadget_ffs_setup_ready.flag
            logger -s "Hailo FFS gadget setup done."
        fi
        # Start daemon and capture PID
        /usr/bin/hailort_server_functionfs &
        echo $! > "$HAILORT_SRV_FFS_PID_FILE"
        logger -s "Started hailort_server_functionfs daemon with PID $(cat $HAILORT_SRV_FFS_PID_FILE)"
        /usr/bin/hailort_usb_setup.sh enable || return 1
        ;;
    "disable")
        # Stop daemon if running
        if is_hailort_server_ffs_running; then
            daemon_pid=$(cat "$HAILORT_SRV_FFS_PID_FILE")
            kill "$daemon_pid"
            logger -s "Stopped hailort_server_functionfs daemon (PID: $daemon_pid)"
        fi
        rm -f "$HAILORT_SRV_FFS_PID_FILE"
        ;;
    esac

    return 0
}


# @brief main.
main()
{
    case "$F_SWITCH_TO_GADGET" in
    "hailo-legacy-disable")
        # Disable Hailo legacy gadget (frees UDC)
        hailo_gadget_ctrl_legacy "disable" || return 1
        ;;
    "hailo-legacy-enable")
        # Enable Hailo legacy gadget
        hailo_gadget_ctrl_legacy "enable" || return 1
        ;;
    "hailo-ffs-disable")
        # Disable Hailo ffs gadget (frees UDC)
        hailo_gadget_ctrl_ffs "disable" || return 1
        ;;
    "hailo-ffs-enable")
        # Enable Hailo ffs gadget
        hailo_gadget_ctrl_ffs "enable" || return 1
        ;;
    *) logger -s "Invalid switch gadget value" && usage && return 1
    esac

    return 0
}

declare -i main_ret=0
#------------------------------------------------------------------------------
#                               MAIN
#------------------------------------------------------------------------------
(
    flock -xn 200 || { echo "$SCRIPT is already running."; exit 1; }

    OPTS_SHORT="hs:"   # Legal short options
    OPTS_LONG="help,switch:" # Legal long options
    # $PARSED_OPTIONS will contain the legal arguments out of "$@".
    PARSED_OPTIONS=$(getopt -n "$0" -o $OPTS_SHORT -l $OPTS_LONG -- "$@") || {
        rm -rf "$LOCK_FILE"
        exit 1
    }

    eval set -- "$PARSED_OPTIONS"     # Set the positional parameters ($1, $2, etc)

    while true; do
        case "$1" in
        --help|-h) usage && exit 0 ;;
        --switch|-s)  F_SWITCH_TO_GADGET="$2"; shift 2 ;;
        --) shift; break ;;
        *) echo "Argument [$1] not handled."; shift; break ;;
        esac
    done

    main "$@"
    main_ret=$?
    rm -rf "$LOCK_FILE"
    exit $main_ret
) 200>"$LOCK_FILE"
