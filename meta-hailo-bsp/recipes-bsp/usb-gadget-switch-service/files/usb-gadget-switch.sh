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

DAEMON=/usr/bin/usb-gadget-switch-util.sh
PIDFILE=/var/run/usb-gadget-switch.id
set -e

# source function library
. /etc/init.d/functions


if [ ! -x $DAEMON ]; then
    echo "Error: $DAEMON not installed"
    exit 1
fi

function usb0_special_setup()
{
    ifconfig usb0 mtu 15412 2>/dev/null &> /dev/null
    sysctl -w net.core.rmem_max=8388608 &> /dev/null
    sysctl -w net.core.wmem_max=8388608 &> /dev/null
    sysctl -w net.ipv4.tcp_rmem="4096 87380 8388608" &> /dev/null
    sysctl -w net.ipv4.tcp_wmem="4096 65536 8388608" &> /dev/null

    return 1
}

function wait_for_usb0()
{
    for ((i=0; i<30; i++)); do
        if ip link show usb0 >/dev/null 2>&1; then
             return 0
        fi
        sleep 0.1
    done

    echo "Failed to configure ether gadget. Rebooting..."
    ( echo 1 > /proc/sys/kernel/sysrq; echo b > /proc/sysrq-trigger ) &

    return 1
}

# Usage: usb-gadget-switch-util.sh [OPTIONS]
#        -s|--switch: hailo/ether.
export DAEMON_OPTS="-s ether"

case "$1" in
    start)
        #echo "disable" > /sys/kernel/hailo_gadget/hailo_gadget
        echo "1 1 1 1" > /proc/sys/kernel/printk
        sleep 2
        start-stop-daemon --start --quiet --background --pidfile $PIDFILE --make-pidfile --exec $DAEMON -- $DAEMON_OPTS
        wait_for_usb0 && usb0_special_setup
        ;;
    status)
        status $DAEMON
        ;;
    *)
        echo "Usage: $0 {start|status}"
        exit 1
        ;;
esac
exit 0
