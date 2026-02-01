SUMMARY = "Hailo DSP requirements"
DESCRIPTION = "The set of packages required to enable DSP functionality"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

PACKAGEGROUP_DISABLE_COMPLEMENTARY = "1"
PACKAGES = "packagegroup-hailo-dsp \
            packagegroup-hailo-dsp-dev-pkg"
DSP_DEV_PACKAGES = ""

RDEPENDS:${PN} = "\
    dsp-fw \
    libhailodsp-dev \
    "

RDEPENDS:${PN}-dev-pkg = "\
    ${PN} \
    ${DSP_DEV_PACKAGES}"
