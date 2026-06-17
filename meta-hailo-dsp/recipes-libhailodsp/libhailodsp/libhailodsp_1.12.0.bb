DESCRIPTION = "libhailodsp - Hailo's API for DSP"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2740b88bd0ffad7eda222e6f5cd097f4"

BRANCH = "1.12.0-dv-9"
SRCREV = "0c1bdf4e4cf2855bcb4186d8fcbac8b69b4c868a"

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
