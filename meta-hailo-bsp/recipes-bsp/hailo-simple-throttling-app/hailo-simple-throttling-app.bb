SUMMARY = "Simple Hailo Throttling Application"
DESCRIPTION = "A simple application that uses libhailo-throttling Library."
LICENSE = "CLOSED"

DEPENDS = "libhailo-throttling"
RDEPENDS_${PN}-dev += "libhailo-throttling"

PV = "1.0"
SRC_URI = "file://main.cpp \
           file://CMakeLists.txt"

S = "${WORKDIR}"

inherit cmake
EXTRA_OECMAKE += "-DCMAKE_BUILD_TYPE=Release"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/hailo-simple-throttling-app ${D}${bindir}/
}

FILES_${PN} += "${bindir}/hailo-simple-throttling-app"
