#!/bin/sh
### BEGIN INIT INFO
# Provides:          usb-udc-state-monitor
# Required-Start:    $local_fs $network
# Required-Stop:     $local_fs $network
# Default-Start:     5
# Default-Stop:      0 1 6
# Short-Description: USB UDC State Monitor Service
# Description:       Monitors and logs USB device controller state changes
### END INIT INFO

DAEMON=/usr/bin/usb-udc-state-monitor
set -e

# source function library
. /etc/init.d/functions

if [ ! -x $DAEMON ]; then
    echo "Error: $DAEMON not installed"
    exit 1
fi

case "$1" in
    start)
        echo "Starting USB UDC state monitor..."
        start-stop-daemon --start --background --exec $DAEMON
        ;;
    stop)
        echo "Stopping USB UDC state monitor..."
        start-stop-daemon --stop --exec $DAEMON --retry 5
        ;;
    status)
        status $DAEMON
        ;;
    *)
        echo "Usage: $0 {start|stop|status}"
        exit 1
        ;;
esac
exit 0
