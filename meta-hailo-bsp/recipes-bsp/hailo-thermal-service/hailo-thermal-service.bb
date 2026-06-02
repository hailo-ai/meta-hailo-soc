SUMMARY = "Hailo Thermal Engine"
DESCRIPTION = "A thermal management service for Hailo"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://hailo-thermal-service.sh \
           ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'file://hailo-thermal-service.service', '', d)}"

S = "${WORKDIR}"

inherit update-rc.d
inherit ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}

INITSCRIPT_NAME = "hailo-thermal-service.sh"
INITSCRIPT_PARAMS = "defaults 99"

SYSTEMD_SERVICE:${PN} = "hailo-thermal-service.service"
SYSTEMD_AUTO_ENABLE = "enable"

DEPENDS += "hailo-thermal-engine"
RDEPENDS:${PN} = "hailo-thermal-engine"
do_install() {
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${S}/hailo-thermal-service.sh ${D}${sysconfdir}/init.d/

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/hailo-thermal-service.service ${D}${systemd_unitdir}/system/
    fi
}

FILES_${PN} += "${sysconfdir}/init.d/hailo-thermal-engine"
FILES_${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/hailo-thermal-service.service', '', d)}"
