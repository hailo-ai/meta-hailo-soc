# tappas-native-apps_%.bbappend

# 1) Install into the image from ${S}/webserver/daemon
do_install:append() {
    # make sure the dirs exist
    install -d ${D}${bindir} ${D}/etc/init.d ${D}/etc/rc5.d

    # copy the Python listener
    install -m 0755 \
        ${S}/webserver/daemon/webserver_daemon.py \
        ${D}${bindir}/webserver_daemon.py

    # copy the init.d wrapper
    install -m 0755 \
        ${S}/webserver/daemon/webserver_daemon \
        ${D}/etc/init.d/webserver_daemon

    # add the runlevel-5 symlink
    ln -s -r /etc/init.d/webserver_daemon \
          ${D}/etc/rc5.d/S20webserver_daemon
}

# 2) Ensure these files get packaged
FILES:${PN} += " \
    ${bindir}/webserver_daemon.py \
    /etc/init.d/webserver_daemon \
    /etc/rc5.d/S20webserver_daemon \
"