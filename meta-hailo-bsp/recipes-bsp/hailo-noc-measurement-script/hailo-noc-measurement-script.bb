DESCRIPTION = "Configure and run NoC measurement"
SECTION = "apps"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://../COPYING.MIT;md5=aa7321c8e0df442b97243c2e1d64c9ee"
RDEPENDS:${PN} += "bash perf"
targetdir = "/etc"

SRC_URI = "file://hailo_noc_perf.sh \
           file://COPYING.MIT"

do_install() {
    install -m 0755 -d ${D}${targetdir}
    install -m 0500 ${WORKDIR}/hailo_noc_perf.sh ${D}${targetdir}
}

FILES:${PN} += "${targetdir}/hailo_noc_perf.sh"
