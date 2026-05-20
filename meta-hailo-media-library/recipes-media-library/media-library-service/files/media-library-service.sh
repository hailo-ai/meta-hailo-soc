#!/bin/sh
### BEGIN INIT INFO
# Provides:          media-library-service
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     5
# Default-Stop:      0 1 6
# Short-Description: Hailo Media Library gRPC Service
# Description:       Starts the media_library_service gRPC server at boot
### END INIT INFO

DAEMON=/usr/bin/media_library_service
PIDFILE=/var/run/media-library-service.pid
RESPAWN_DELAY=2
set -e

. /etc/init.d/functions

# Source configuration from /etc/default/ if present (port, log level, config path).
# Use set -a / +a so all variables are exported to the daemon environment.
ENV_FILE="/etc/default/media-library-service.config"
if [ -f "$ENV_FILE" ]; then
    set -a
    . "$ENV_FILE"
    set +a
fi

if [ ! -x $DAEMON ]; then
    echo "Error: $DAEMON not installed"
    exit 1
fi

case "$1" in
    start)
        echo "Starting Media Library Service..."
        start-stop-daemon --start --quiet --background --pidfile $PIDFILE \
            --make-pidfile --startas /bin/sh -- -c \
            "while true; do $DAEMON; echo 'media_library_service exited, restarting in ${RESPAWN_DELAY}s...' | logger -t media-library-service; sleep $RESPAWN_DELAY; done"
        ;;
    stop)
        echo "Stopping Media Library Service..."
        start-stop-daemon --stop --quiet --pidfile $PIDFILE
        # Also kill any lingering server process (the respawn wrapper may have spawned a new one)
        pkill -f "$DAEMON" 2>/dev/null || true
        rm -f $PIDFILE
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
