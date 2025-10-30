FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://sshd.service"

SYSTEMD_SERVICE:${PN}-sshd = "sshd.service"
SYSTEMD_AUTO_ENABLE:${PN}-sshd = "enable"

do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/sshd.service ${D}${systemd_unitdir}/system

        # Disable the socket activation approach
        rm -f ${D}${systemd_unitdir}/system/sshd.socket
        rm -f ${D}${systemd_unitdir}/system/sshd@.service
    fi
}