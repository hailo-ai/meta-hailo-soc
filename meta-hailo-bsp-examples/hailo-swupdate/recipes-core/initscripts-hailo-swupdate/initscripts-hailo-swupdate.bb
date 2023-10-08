SUMMARY = "Hailo SWUpdate startup script"
SECTION = "base"
PR = "r0"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
RDEPENDS:${PN} += "bash"

SRC_URI = "file://rcS.swupdate \
	"

RPROVIDES:${PN} += "virtual/initscripts-hailo-swupdate"

S = "${WORKDIR}"

HAILO_UPDATE_TFTP_SERVER_IP = "10.0.0.2"
HAILO_UPDATE_FILENAME = "hailo-update-image-${MACHINE}.swu"

inherit allarch update-alternatives

do_install () {
	install -d ${D}/${sysconfdir}
	echo -n "${HAILO_UPDATE_TFTP_SERVER_IP}" > ${WORKDIR}/server_ip
	install -m 0755 ${WORKDIR}/server_ip ${D}${sysconfdir}/server_ip
	echo -n "${HAILO_UPDATE_FILENAME}" > ${WORKDIR}/update_filename
	install -m 0755 ${WORKDIR}/update_filename ${D}${sysconfdir}/update_filename
	install -d ${D}/${sysconfdir}/init.d
	install -d ${D}${base_sbindir}
	install -m 755 ${S}/rcS.swupdate ${D}${base_sbindir}/init
}

ALTERNATIVE_PRIORITY = "300"
ALTERNATIVE:${PN} = "init"
ALTERNATIVE_LINK_NAME[init] = "${base_sbindir}/init"
ALTERNATIVE_PRIORITY[init] = "60"

PACKAGES = "${PN}"
FILES:${PN} = "/"

CONFFILES:${PN} = ""
