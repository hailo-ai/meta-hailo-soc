SUMMARY = "CMA tracking snapshot capture utility"
DESCRIPTION = "A utility script to capture CMA tracking snapshot"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://capture-cma-tracking-snapshot"

S = "${WORKDIR}"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/capture-cma-tracking-snapshot ${D}${bindir}/capture-cma-tracking-snapshot
}

FILES:${PN} = "${bindir}/capture-cma-tracking-snapshot"

