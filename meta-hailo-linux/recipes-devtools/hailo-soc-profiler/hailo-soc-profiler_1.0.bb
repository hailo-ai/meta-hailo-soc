SUMMARY = "Hailo SoC Profiler tracing utility"
DESCRIPTION = "A script that records traces of the Hailo SoC"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

RDEPENDS:${PN} = "perfetto bash"

SOC_PROFILER_CHIP_CONFIGS = "hailo15"
SOC_PROFILER_CHIP_CONFIGS:hailo15l = "hailo15l"

SRC_URI = "file://hailo-soc-profiler \
           file://predefined_configurations/${SOC_PROFILER_CHIP_CONFIGS} \
           file://predefined_configurations/common"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/hailo-soc-profiler ${D}${bindir}/hailo-soc-profiler

    install -d ${D}${datadir}/hailo-soc-profiler/configs

    for config in ${WORKDIR}/predefined_configurations/common/*.pbtx ${WORKDIR}/predefined_configurations/${SOC_PROFILER_CHIP_CONFIGS}/*.pbtx; do
        install -m 0644 ${config} ${D}${datadir}/hailo-soc-profiler/configs/$(basename ${config})
    done
}

FILES:${PN} = "${bindir}/hailo-soc-profiler ${datadir}/hailo-soc-profiler/configs"
