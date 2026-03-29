SUMMARY = "expected : Single header implementation of std::expected with functional-style extensions."

LICENSE = "CC0 1.0 Universal"
LIC_FILES_CHKSUM = "file://COPYING;md5=65d3616852dbf7b1a6d4b53b00626032"

SRC_URI = "git://github.com/TartanLlama/expected.git;protocol=https;branch=master"

PV = "1.0+git${SRCPV}"
SRCREV = "292eff8bd8ee230a7df1d6a1c00c4ea0eb2f0362"

S = "${WORKDIR}/git"

# expected is a header-only C++ library, so the main package will be empty.
ALLOW_EMPTY:${PN} = "1"
ALLOW_EMPTY:${PN}-dev = "1"

do_install(){
    install -d ${D}${includedir}
    install -d ${D}${includedir}/tl

    install -m 0644 ${S}/include/tl/*.hpp ${D}${includedir}/tl/
}

FILES:${PN} += "${includedir}/* ${includedir}/TL ${includedir}/tl/*.hpp"
FILES:${PN}-dev += "${includedir}/* ${includedir}/tl ${includedir}/tl/*"
