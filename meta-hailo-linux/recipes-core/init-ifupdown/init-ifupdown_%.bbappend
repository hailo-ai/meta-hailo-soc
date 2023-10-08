
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://interfaces"

S = "${WORKDIR}"

do_install:append() {
        install -m 0644 ${WORKDIR}/interfaces ${D}${sysconfdir}/network/interfaces
}

FILES:${PN} += " /etc /etc/network /etc/network/interfaces "
