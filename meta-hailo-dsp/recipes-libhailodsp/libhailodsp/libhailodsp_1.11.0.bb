DESCRIPTION = "libhailodsp - Hailo's API for DSP"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2740b88bd0ffad7eda222e6f5cd097f4"

BRANCH = "0.0.0.LGL-dv-LGL_20"
SRCREV = "eae2addbe233c65b0b2f6ffce3f4294ab6df4b51"

SRC_URI = "git://git@github.com/hailo-ai/hailodsp.git;protocol=https;branch=${BRANCH}"
S = "${WORKDIR}/git"
OECMAKE_SOURCEPATH = "${S}/libhailodsp"

DSP_COMPILATION_MODE ??= "release"
DEPENDS:prepend := "catch2 spdlog cli11 "
BUILD_TYPE = "${@bb.utils.contains('DSP_COMPILATION_MODE', 'release', 'Release', 'Debug', d)}"
EXTRA_OECMAKE:append = "-DCMAKE_BUILD_TYPE=${BUILD_TYPE}"
inherit cmake

# libhailodsp builds without soname versioning — only an unversioned .so is produced.
# By default Yocto puts unversioned .so in -dev. Override to keep it in the base package.
FILES:${PN} += "${libdir}/libhailodsp.so"
