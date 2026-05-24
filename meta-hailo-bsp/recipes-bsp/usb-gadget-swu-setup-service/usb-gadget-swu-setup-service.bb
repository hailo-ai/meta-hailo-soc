SUMMARY = "USB gadget SWU setup service"
DESCRIPTION = "A USB gadget SWU setup management service"
LICENSE = "CLOSED"

SRC_URI = "file://usb-gadget-swu-setup.sh \
           file://usb-gadget-swu-setup-init.sh \
           ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'file://usb-gadget-swu-setup.service', '', d)}"

S = "${WORKDIR}"

inherit update-rc.d
inherit ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}

INITSCRIPT_NAME = "usb-gadget-swu-setup-init.sh"
INITSCRIPT_PARAMS = "defaults 22"

SYSTEMD_SERVICE:${PN} = "usb-gadget-swu-setup.service"
SYSTEMD_AUTO_ENABLE = "enable"

RDEPENDS:${PN} = "bash"

do_install() {
    install -d ${D}${sysconfdir}/init.d
    install -d ${D}${bindir}
    install -m 0755 ${S}/usb-gadget-swu-setup.sh ${D}${bindir}
    install -m 0755 ${S}/usb-gadget-swu-setup-init.sh ${D}${sysconfdir}/init.d/

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/usb-gadget-swu-setup.service ${D}${systemd_unitdir}/system/
    fi
}

FILES:${PN} += "${bindir}/usb-gadget-swu-setup.sh"
FILES:${PN} += "${sysconfdir}/init.d/usb-gadget-swu-setup-init.sh"
FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/usb-gadget-swu-setup.service', '', d)}"
