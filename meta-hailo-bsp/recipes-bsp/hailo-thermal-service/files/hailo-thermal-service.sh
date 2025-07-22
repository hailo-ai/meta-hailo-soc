#!/bin/sh
### BEGIN INIT INFO
# Provides:          hailo-thermal-engine
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     5
# Default-Stop:      0 1 6
# Short-Description: Hailo Thermal Engine
# Description:       Starts the hailo-thermal-engine at boot
### END INIT INFO

DAEMON=/usr/bin/hailo-thermal-engine
PIDFILE=/var/run/hailo-thermal-engine.pid
set -e

# source function library
. /etc/init.d/functions


if [ ! -x $DAEMON ]; then
    echo "Error: $DAEMON not installed"
    exit 1
fi

# Usage: hailo-thermal-engine [options]
#         -l <level>, --loglevel <level>  log level: DEBUG, INFO, NOTICE, WARN, ERROR
#         -s, --syslog            output to syslog
export DAEMON_OPTS="--loglevel INFO --syslog"

case "$1" in
    start)
        echo "Starting Hailo Thermal Engine..."
        start-stop-daemon --start --quiet --background --pidfile $PIDFILE --make-pidfile --exec $DAEMON -- $DAEMON_OPTS
        ;;
    stop)
        echo "Stopping Hailo Thermal Engine..."
        start-stop-daemon --stop --quiet --pidfile $PIDFILE
        ;;
    restart)
        $0 stop
        sleep 2
        $0 start
        ;;
    status)
        status $DAEMON
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
exit 0
