SUMMARY = "Hailo systemd network configuration"
DESCRIPTION = "Network configuration files for systemd-networkd on Hailo devices"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

FILESEXTRAPATHS:prepend := "${THISDIR}/systemd-network:"

SRC_URI = "\
    file://10-eth0.network \
    file://20-eth1.network \
"

SYSTEMD_DIR = "${sysconfdir}/systemd"

inherit systemd

RDEPENDS:${PN} = "systemd"

do_install() {
    # Network units
    install -d ${D}${SYSTEMD_DIR}/network
    install -m 0644 ${WORKDIR}/*.network ${D}${SYSTEMD_DIR}/network/
}

FILES:${PN} += "${SYSTEMD_DIR}/network/*.network"
