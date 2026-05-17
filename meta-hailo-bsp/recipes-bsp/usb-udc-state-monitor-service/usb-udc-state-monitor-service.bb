SUMMARY = "USB UDC state change monitor service"
DESCRIPTION = "Monitors and logs USB device controller state changes"
LICENSE = "CLOSED"

SRC_URI = "file://usb-udc-state-monitor.c \
           file://usb-udc-state-monitor-init.sh \
           ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'file://usb-udc-state-monitor.service', '', d)}"

S = "${WORKDIR}"

inherit update-rc.d
inherit ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}

INITSCRIPT_NAME = "usb-udc-state-monitor-init.sh"
INITSCRIPT_PARAMS = "defaults 21"

SYSTEMD_SERVICE:${PN} = "usb-udc-state-monitor.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} -o usb-udc-state-monitor ${WORKDIR}/usb-udc-state-monitor.c
}

do_install() {
    install -d ${D}${sysconfdir}/init.d
    install -d ${D}${bindir}
    install -m 0755 ${B}/usb-udc-state-monitor ${D}${bindir}
    install -m 0755 ${S}/usb-udc-state-monitor-init.sh ${D}${sysconfdir}/init.d/

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/usb-udc-state-monitor.service ${D}${systemd_unitdir}/system/
    fi
}

FILES:${PN} += "${bindir}/usb-udc-state-monitor"
FILES:${PN} += "${sysconfdir}/init.d/usb-udc-state-monitor-init.sh"
FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/usb-udc-state-monitor.service', '', d)}"
