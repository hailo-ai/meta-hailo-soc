SUMMARY = "Linux Kernel Thermal engine"
DESCRIPTION = "thermal-engine is a thermal monitoring and analysis tool/service provided by the Linux kernel's tools directory. It allows monitoring and analysis of thermal data exposed by the Linux kernel's thermal subsystem."
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"
PROVIDES = "virtual/hailo-thermal-engine hailo-thermal-engine-libs"
RPROVIDES:${PN} = "hailo-thermal-engine-libs"

inherit kernel-arch kernelsrc

do_populate_lic[depends] += "virtual/kernel:do_patch"

TARGET_LINK_HASH_STYLE = ""

DEPENDS += "ncurses"
DEPENDS += "libconfig"
DEPENDS += "libnl"
#DEPENDS += "hailo-thermal-engine-libs"
RDEPENDS:${PN} += "ncurses"
RDEPENDS:${PN} += "libconfig"
RDEPENDS:${PN} += "libnl"
#RDEPENDS:${PN} += "hailo-thermal-engine_libs"

VERBOSE_MAKE = "0"
COMMON_CFLAGS:${PN} = "O=${B} CROSS=${TARGET_PREFIX} CC="${CC}" LD="${LD}" AR=${AR} ARCH=${ARCH} V=${VERBOSE_MAKE}"

do_configure[depends] += "virtual/kernel:do_shared_workdir"

do_compile() {
    oe_runmake -C ${S}/tools/lib/thermal/ ${COMMON_CFLAGS:${PN}}
    oe_runmake -C ${S}/tools/thermal/lib/ ${COMMON_CFLAGS:${PN}}
    oe_runmake -C ${S}/tools/thermal/thermal-engine prefix="${B}/" ${COMMON_CFLAGS:${PN}} hailo-thermal-engine
}

do_install() {
    install -d ${D}${libdir}
    cp -d ${B}/libthermal.so* ${D}${libdir}
    cp -d ${B}/libthermal_tools.so* ${D}${libdir}
    install -d ${D}${includedir}
    install -m 0644 ${S}/tools/lib/thermal/include/thermal.h ${D}${includedir}
    install -m 0644 ${S}/tools/thermal/thermal-engine/hailo-thermal-engine.h ${D}${includedir}

    install -d ${D}${bindir}
    install -m 0755 ${B}/hailo-thermal-engine ${D}${bindir}
}

python do_package:prepend() {
    d.setVar('PKGV', d.getVar("KERNEL_VERSION", True).split("-")[0])
}

B = "${WORKDIR}/${BPN}-${PV}"

FILES_${PN} += "${bindir}/hailo-thermal-engine"
FILES_${PN} += "${libdir}/libthermal.so* ${libdir}/libthermal_tools.so*"
FILES_${PN}-dev = "${includedir}/hailo-thermal-engine.h ${libdir}/libthermal.so ${libdir}/libthermal_tools.so"
