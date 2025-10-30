SUMMARY = "Linux Kernel vm-tools tool"
DESCRIPTION = "This utility reads kernel memory page flags from files like /proc/kpageflags and/or /proc/kpagecount"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"
PROVIDES = "virtual/vm-tools"
RPROVIDES:${PN} = "vm-tools"

inherit kernel-arch kernelsrc

do_populate_lic[depends] += "virtual/kernel:do_patch"

TARGET_LINK_HASH_STYLE = ""

VERBOSE_MAKE = "1"
COMMON_CFLAGS:${PN} = "O=${B} CROSS=${TARGET_PREFIX} CC="${CC}" LD="${LD}" AR=${AR} ARCH=${ARCH} V=${VERBOSE_MAKE}"

do_configure[depends] += "virtual/kernel:do_shared_workdir"

do_compile() {
    oe_runmake -C ${S}/tools/lib/api/ ${COMMON_CFLAGS:${PN}}  srctree=${S}/ prefix=${B}/
    oe_runmake -C ${S}/tools/vm/ ${COMMON_CFLAGS:${PN}} srctree=${S}/ prefix=${B}/
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/page-types ${D}${bindir}
    install -m 0755 ${B}/slabinfo ${D}${bindir}
}

python do_package:prepend() {
    d.setVar('PKGV', d.getVar("KERNEL_VERSION", True).split("-")[0])
}

B = "${WORKDIR}/${BPN}-${PV}"

FILES_${PN} += "${bindir}/page-types"
FILES_${PN} += "${bindir}/slabinfo"
