SUMMARY = "USB gadget switch service"
DESCRIPTION = "A USB gadget switch management service"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://usb-gadget-switch-util.sh \
           file://usb-gadget-switch.sh \
           ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'file://usb-gadget-switch.service', '', d)}"

S = "${WORKDIR}"

inherit update-rc.d
inherit ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}

INITSCRIPT_NAME = "usb-gadget-switch.sh"
INITSCRIPT_PARAMS = "defaults 59"

SYSTEMD_SERVICE:${PN} = "usb-gadget-switch.service"
SYSTEMD_AUTO_ENABLE = "enable"

RDEPENDS:${PN} = "bash"

do_install() {
    install -d ${D}${sysconfdir}/init.d
    install -d ${D}${bindir}
    install -m 0755 ${S}/usb-gadget-switch-util.sh ${D}${bindir}
    install -m 0755 ${S}/usb-gadget-switch.sh ${D}${sysconfdir}/init.d/

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/usb-gadget-switch.service ${D}${systemd_unitdir}/system/
    fi
}

FILES_${PN} += "${bindir}/usb-gadget-switch-util.sh"
FILES_${PN} += "${sysconfdir}/init.d/usb-gadget-switch.sh"
FILES_${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/usb-gadget-switch.service', '', d)}"
