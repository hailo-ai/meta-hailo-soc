DESCRIPTION = "Run the swupdate process"
SECTION = "apps"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://../COPYING.MIT;md5=aa7321c8e0df442b97243c2e1d64c9ee"
RDEPENDS:${PN} += "bash"
targetdir = "/etc"

SRC_URI = "file://run_swupdate.sh \
           file://get_sw_image.sh \
           file://set_sw_image.sh \
           file://COPYING.MIT"

do_install() {
    install -m 0755 -d ${D}${targetdir}
    install -m 0500 ${WORKDIR}/run_swupdate.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/get_sw_image.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/set_sw_image.sh ${D}${targetdir}
}

FILES:${PN} += "${targetdir}/run_swupdate.sh"
FILES:${PN} += "${targetdir}/get_sw_image.sh"
FILES:${PN} += "${targetdir}/set_sw_image.sh"
