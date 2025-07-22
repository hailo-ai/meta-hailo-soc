SUMMARY = "Thermal Monitoring Tool (tmon) from Linux kernel"
DESCRIPTION = "tmon is a thermal monitoring and analysis tool provided by the Linux kernel's tools directory. It allows monitoring and analysis of thermal data exposed by the Linux kernel's thermal subsystem."
LICENSE = "GPL-2.0-only"
# LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"
PROVIDES = "virtual/tmon"

inherit bash-completion kernelsrc kernel-arch externalsrc

do_populate_lic[depends] += "virtual/kernel:do_patch"

EXTRA_OEMAKE = "-C ${S}/tools/thermal/tmon O=${B} CROSS=${TARGET_PREFIX} CC="${CC}" LD="${LD}" AR=${AR} ARCH=${ARCH}"

DEPENDS += "ncurses"
RDEPENDS:${PN} += "ncurses"

do_configure[depends] += "virtual/kernel:do_shared_workdir"

do_compile() {
    oe_runmake
}

do_install() {
    oe_runmake DESTDIR=${D} install
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

python do_package:prepend() {
    d.setVar('PKGV', d.getVar("KERNEL_VERSION", True).split("-")[0])
}

B = "${WORKDIR}/${BPN}-${PV}"
