SUMMARY = "Hailo SoC Profiler tracing service"
DESCRIPTION = "Service script to start Hailo SoC Profiler tracing components"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

RDEPENDS:${PN} = "perfetto"

SRC_URI = "file://hailo-soc-profiler-service"

S = "${WORKDIR}"

inherit update-rc.d

INITSCRIPT_NAME = "hailo-soc-profiler-service"
INITSCRIPT_PARAMS = "defaults 90"

do_install() {
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/hailo-soc-profiler-service ${D}${sysconfdir}/init.d/hailo-soc-profiler-service
}

PACKAGES = "${PN}"
FILES:${PN} = "${sysconfdir}/init.d/hailo-soc-profiler-service"