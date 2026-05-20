#!/bin/sh
### BEGIN INIT INFO
# Provides:          usb-gadget-swu-setup
# Required-Start:    $local_fs $network
# Required-Stop:     $local_fs $network
# Default-Start:     5
# Default-Stop:      0 1 6
# Short-Description: USB Gadget Switch Service
# Description:       Starts the USB gadget switch service at boot
### END INIT INFO

UTIL_NAME=/usr/bin/usb-gadget-swu-setup.sh
PIDFILE=/var/run/usb-gadget-swu-setup.pid
set -e

# source function library
. /etc/init.d/functions

if [ ! -x $UTIL_NAME ]; then
    echo "Error: $UTIL_NAME not installed"
    exit 1
fi

case "$1" in
    start)
        $UTIL_NAME -a start
        ;;
    stop)
        $UTIL_NAME -a stop
        ;;
    status)
        status $UTIL_NAME
        ;;
    *)
        echo "Usage: $0 {start|stop|status}"
        exit 1
        ;;
esac
exit 0
