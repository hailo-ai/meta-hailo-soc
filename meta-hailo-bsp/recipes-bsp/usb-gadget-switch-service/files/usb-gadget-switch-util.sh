#!/bin/bash

declare -r SCRIPT=$(basename "$0")
declare -i PID=$$
declare -r LOCK_FILE="/tmp/$SCRIPT.lock"

declare F_SWITCH_TO_GADGET="none"

trap 'trap_func' TERM INT
trap_func()
{
    echo "$SCRIPT interrupted. Exiting."
    rm -rf "$LOCK_FILE"
}

# @brief script usage.
function usage()
{
  echo "Switch USB gadgets between ether & hailo"
  echo "Usage: $SCRIPT [OPTIONS]"
  echo "       -h|--help: show this help."
  echo "       -s|--switch: hailo/ether."
  return 0
}

# @brief Hailo gadget control.
function hailo_gadget_ctrl()
{
    local state="$1"
    local curr_state

    if [ ! -e /sys/kernel/hailo_gadget/hailo_gadget ]; then
        logger -s "Hailo gadget sysfs entry not found"
        return 1
    fi

    curr_state=$(cat /sys/kernel/hailo_gadget/hailo_gadget)
    [ "$curr_state" == "$state" ] && return 0

    echo "${state}" > /sys/kernel/hailo_gadget/hailo_gadget

    return 0
}

function module_remove()
{
    local module_name="$1"

    lsmod | grep -q "${module_name}"
    [ $? -eq 0 ] && modprobe -r "${module_name}"
    [ $? -ne 0 ] && logger -s "Remove module ${module_name} failed" && return 1
    return 0
}

function module_install()
{
    local module_name="$1"

    lsmod | grep -q "${module_name}"
    [ $? -ne 0 ] && modprobe "${module_name}"
    [ $? -ne 0 ] && logger -s "Install module ${module_name} failed" && return 1
    return 0
}
# @brief main.
main()
{
    local ret
    if [ ! -e /sys/kernel/hailo_gadget/hailo_gadget ]; then
        logger -s "Hailo gadget sysfs entry not found"
        return 1
    fi

    case "$F_SWITCH_TO_GADGET" in
    "hailo")
        # Remove g_ether
        module_remove "g_ether" || return 1
        sleep 2
        # Enable Hailo gadget (frees UDC)
        hailo_gadget_ctrl "enable" || return 1
        ;;
    "ether")
        # Disable Hailo gadget (frees UDC)
        hailo_gadget_ctrl "disable" || return 1
        sleep 2
        # Load g_ether (can now bind to UDC)
        module_install "g_ether" || return 1
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
