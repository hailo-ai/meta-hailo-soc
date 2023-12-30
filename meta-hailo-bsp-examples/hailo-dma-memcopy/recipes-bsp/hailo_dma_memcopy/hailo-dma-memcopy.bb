DESCRIPTION = "Compile and install the SW user example for using hailo gp dma"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING.MIT;md5=aa7321c8e0df442b97243c2e1d64c9ee"
TARGETDIR = "/etc"
TEST_FILE_NAME = "test_hailo_memcpy"

SRC_URI = "file://${TEST_FILE_NAME}.c \
            file://COPYING.MIT"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

S = "${WORKDIR}"

do_compile () {
   ${CC} ${CFLAGS} ${LDFLAGS}  ${TEST_FILE_NAME}.c -o ${TEST_FILE_NAME} 
}

do_install() {
    install -d ${D}/${TARGETDIR}
    install -m 0755 ${TEST_FILE_NAME} ${D}/${TARGETDIR}/${TEST_FILE_NAME}
}

FILES:${PN} += "${TARGETDIR}/${TEST_FILE_NAME}"
