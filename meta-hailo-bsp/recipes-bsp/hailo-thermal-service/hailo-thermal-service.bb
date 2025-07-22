SUMMARY = "Hailo Thermal Engine"
DESCRIPTION = "A thermal management service for Hailo"
LICENSE = "CLOSED"

SRC_URI = "file://hailo-thermal-service.sh"

S = "${WORKDIR}"

inherit update-rc.d

INITSCRIPT_NAME = "hailo-thermal-service.sh"
INITSCRIPT_PARAMS = "defaults 99"

DEPENDS += "hailo-thermal-engine"
RDEPENDS:${PN} = "hailo-thermal-engine"
do_install() {
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${S}/hailo-thermal-service.sh ${D}${sysconfdir}/init.d/
}

FILES_${PN} += "${sysconfdir}/init.d/hailo-thermal-engine"
