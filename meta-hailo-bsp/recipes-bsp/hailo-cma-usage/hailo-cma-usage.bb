DESCRIPTION = "Hailo CMA usage utility"
LICENSE = "CLOSED"
TARGETDIR = "/usr/bin"
RDEPENDS:${PN} += "bash"

S = "${WORKDIR}"
FILESEXTRAPATHS:prepend := "${THISDIR}/files/:"
SRC_URI = "file://hailo-cma-usage.sh"

do_install () {
    install -d ${D}${TARGETDIR}
    install -m 0555 -D ${WORKDIR}/hailo-cma-usage.sh ${D}${TARGETDIR}/hailo-cma-usage.sh
}

# Specify the location where the script will be installed
FILES:${PN} += "${TARGETDIR}/hailo-cma-usage.sh"
