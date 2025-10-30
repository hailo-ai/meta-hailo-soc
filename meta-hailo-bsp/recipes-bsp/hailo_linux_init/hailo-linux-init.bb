DESCRIPTION = "Install the SW user example for running the hailo-linux-init script"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING.MIT;md5=aa7321c8e0df442b97243c2e1d64c9ee"
TARGETDIR = "/etc"
SCRIPT_FILE_NAME = "hailo_linux_init.sh"
RDEPENDS:${PN} += "bash"

SRC_URI = "file://${SCRIPT_FILE_NAME} \
           ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'file://hailo-linux-init.service', '', d)} \
           file://COPYING.MIT"

S = "${WORKDIR}"

INITSCRIPT_NAME = "${SCRIPT_FILE_NAME}"
INITSCRIPT_PARAMS = "start 50 5 ."
inherit update-rc.d

inherit ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}
SYSTEMD_SERVICE:${PN} = "hailo-linux-init.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install() {
    install -d ${D}${TARGETDIR}/init.d
    install -m 0755 ${WORKDIR}/${SCRIPT_FILE_NAME} ${D}${TARGETDIR}/init.d/${SCRIPT_FILE_NAME}

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/hailo-linux-init.service ${D}${systemd_unitdir}/system/
    fi
}

FILES_${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/hailo-linux-init.service', '', d)}"
