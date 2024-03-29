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

inherit allarch update-alternatives

do_install () {
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
