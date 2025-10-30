SUMMARY = "Hailo SoC Profiler tracing service"
DESCRIPTION = "Service script to start Hailo SoC Profiler tracing components"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

RDEPENDS:${PN} = "perfetto"

SRC_URI = "file://hailo-soc-profiler-service"
SRC_URI += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', \
    'file://hailo-soc-profiler.service \
     file://hailo-soc-profiler-traced.service \
     file://hailo-soc-profiler-traced-probes.service \
     file://hailo-soc-profiler-traced-perf.service', '', d)}"

S = "${WORKDIR}"

inherit ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', 'update-rc.d', d)}

INITSCRIPT_NAME = "hailo-soc-profiler-service"
INITSCRIPT_PARAMS = "defaults 90"

SYSTEMD_SERVICE:${PN} = "hailo-soc-profiler.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        # Install systemd files when systemd is enabled
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/hailo-soc-profiler.service ${D}${systemd_unitdir}/system/
        install -m 0644 ${WORKDIR}/hailo-soc-profiler-traced.service ${D}${systemd_unitdir}/system/
        install -m 0644 ${WORKDIR}/hailo-soc-profiler-traced-probes.service ${D}${systemd_unitdir}/system/
        install -m 0644 ${WORKDIR}/hailo-soc-profiler-traced-perf.service ${D}${systemd_unitdir}/system/
    else
        # Install sysvinit script when systemd is not enabled
        install -d ${D}${sysconfdir}/init.d
        install -m 0755 ${WORKDIR}/hailo-soc-profiler-service ${D}${sysconfdir}/init.d/hailo-soc-profiler-service
    fi
}

PACKAGES = "${PN}"
FILES:${PN} = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/*', '${sysconfdir}/init.d/*', d)}"
