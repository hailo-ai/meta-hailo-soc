DESCRIPTION = "TAPPAS Hailo15 reference camera api recipe, \
               the recipe compiles and copies headers/pkgconfig"

inherit tappas-base

PV_PARSED = "${@ '${PV}'.replace('.0', '')}"
SRC_URI = "git://git@github.com/hailo-ai/hailo-camera-apps.git;protocol=https;branch=1.9.0"

SRCREV = "9eae18baeb6bad04782c1839dc0acd9f61156537"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM += "file://../../../LICENSE;md5=4fbd65380cdd255951079008b364516c"

DEPENDS += " gstreamer1.0 gstreamer1.0-plugins-base cxxopts rapidjson libgsthailotools libmedialib-api"
RDEPENDS:${PN} += " bash libgsthailotools"

S = "${WORKDIR}/git/apps/h15/native"

DEPENDS += " opencv"
# meson configuration
EXTRA_OEMESON += " \
        -Dtarget='api' \
        "

FILES:${PN} += " ${incdir}/hailo/tappas/* ${libdir}/libhailo_reference_camera*"
FILES:${PN}-lib += ""
RDEPENDS:${PN}-staticdev = ""
RDEPENDS:${PN}-dev = ""
RDEPENDS:${PN}-dbg = ""