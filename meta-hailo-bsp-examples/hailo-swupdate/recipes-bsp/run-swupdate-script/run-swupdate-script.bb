DESCRIPTION = "Run the swupdate process"
SECTION = "apps"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://../COPYING.MIT;md5=aa7321c8e0df442b97243c2e1d64c9ee"

RDEPENDS:${PN} += "bash"
RDEPENDS:${PN} += "scu-bl"
targetdir = "/etc"

CRC_VERIFY_FILE_NAME = "verify_file_crc"

SRC_URI = "file://run_swupdate.sh \
           file://get_sw_image.sh \
           file://set_sw_image.sh \
           file://get_boot_dev.sh \
           file://boot_definitions.sh \
           file://${CRC_VERIFY_FILE_NAME}.c \
           file://COPYING.MIT"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

do_compile () {
   ${CC} ${CFLAGS} ${LDFLAGS}  ${WORKDIR}/${CRC_VERIFY_FILE_NAME}.c -o ${WORKDIR}/${CRC_VERIFY_FILE_NAME} 
}

do_install() {
    install -m 0755 -d ${D}${targetdir}
    install -m 0500 ${WORKDIR}/run_swupdate.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/get_boot_dev.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/get_sw_image.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/set_sw_image.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/boot_definitions.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/${CRC_VERIFY_FILE_NAME} ${D}${targetdir}/${CRC_VERIFY_FILE_NAME}
}

FILES:${PN} += "${targetdir}/run_swupdate.sh"
FILES:${PN} += "${targetdir}/get_boot_dev.sh"
FILES:${PN} += "${targetdir}/get_sw_image.sh"
FILES:${PN} += "${targetdir}/set_sw_image.sh"
FILES:${PN} += "${targetdir}/boot_definitions.sh"
FILES:${PN} += "${targetdir}/${CRC_VERIFY_FILE_NAME}"
