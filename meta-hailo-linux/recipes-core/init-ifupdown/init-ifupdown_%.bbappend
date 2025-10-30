FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "${@bb.utils.contains('MACHINE_FEATURES', 'no_eth', 'file://interfaces-no-eth', 'file://interfaces', d)}"

S = "${WORKDIR}"

do_install:append() {
        install -d ${D}${sysconfdir}/network
        if [ "${@bb.utils.contains("MACHINE_FEATURES", "no_eth", "1", "0", d)}" = "1" ]; then
                install -m 0644 ${WORKDIR}/interfaces-no-eth ${D}${sysconfdir}/network/interfaces
        else
                install -m 0644 ${WORKDIR}/interfaces ${D}${sysconfdir}/network/interfaces
        fi
}

FILES:${PN} += " /etc /etc/network /etc/network/interfaces "