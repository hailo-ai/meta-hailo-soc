SUMMARY = "Hailo USB Current Supply Service"
DESCRIPTION = "Init service to run the Hailo USB current supply script"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://hailo-usb-current-supply.sh \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'file://hailo-usb-current-supply.service', '', d)} \
"

S = "${WORKDIR}"

inherit update-rc.d
inherit ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}

INITSCRIPT_NAME = "hailo-usb-current-supply.sh"
INITSCRIPT_PARAMS = "start 90 S ."

SYSTEMD_SERVICE:${PN} = "hailo-usb-current-supply.service"
SYSTEMD_AUTO_ENABLE = "enable"

RDEPENDS:${PN} = "i2c-tools"

do_install() {
    # Install the script to init.d (works as both init script and executable)
    install -d ${D}${sysconfdir}/init.d
    install -m 0555 ${WORKDIR}/hailo-usb-current-supply.sh ${D}${sysconfdir}/init.d/

    # Also install to bindir for manual execution
    install -d ${D}${bindir}
    install -m 0555 ${WORKDIR}/hailo-usb-current-supply.sh ${D}${bindir}/

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/hailo-usb-current-supply.service ${D}${systemd_unitdir}/system/
    fi
}

FILES:${PN} = " \
    ${bindir}/hailo-usb-current-supply.sh \
    ${sysconfdir}/init.d/hailo-usb-current-supply.sh \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/hailo-usb-current-supply.service', '', d)} \
"