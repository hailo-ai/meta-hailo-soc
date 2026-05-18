#!/bin/sh
### BEGIN INIT INFO
# Provides:          usb-gadget-switch
# Required-Start:    $local_fs $network
# Required-Stop:     $local_fs $network
# Default-Start:     5
# Default-Stop:      0 1 6
# Short-Description: USB Gadget Switch Service
# Description:       Starts the USB gadget switch service at boot
### END INIT INFO

UTIL_SCRIPT=/usr/bin/usb-gadget-switch-util.sh
set -e

# source function library
. /etc/init.d/functions

case "$1" in
    start)
        $UTIL_SCRIPT -s hailo-legacy-disable
        ret=$?
        printf "Disabling Hailo legacy USB gadget..."
        if [ $ret -eq 0 ]; then
            echo $(success)
        else
            echo $(failure)
        fi
        ;;
    status)
        status $UTIL_SCRIPT
        ;;
    *)
        echo "Usage: $0 {start|status}"
        exit 1
        ;;
esac
exit 0
