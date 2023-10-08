DESCRIPTION = "Run the swupdate process"
SECTION = "apps"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://../COPYING.MIT;md5=aa7321c8e0df442b97243c2e1d64c9ee"
RDEPENDS:${PN} += "bash"
targetdir = "/etc"

run_swupdate_script_file = "run_swupdate.sh"

SRC_URI = "file://${run_swupdate_script_file} \
           file://COPYING.MIT"

do_install() {
    install -m 0755 -d ${D}${targetdir}
    install -m 0500 ${WORKDIR}/${run_swupdate_script_file} ${D}${targetdir}
}
FILES:${PN} += "${targetdir}/${run_swupdate_script_file}"
