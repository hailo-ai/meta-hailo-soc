LICENSE = "MIT & Proprietary-VSI"
LIC_FILES_CHKSUM = "file://${B}/LICENSE;md5=805d1be5d56ae9500316a754de03ab5f \
					file://${S}/LICENSE;md5=8349eaff29531f0a3c4f4c8b31185958"

INHERITS = "externalsrc ccache"
RDEPENDS_IMAGING_SUB_SYSTEM = ""
DEPENDS_IMAGING_SUB_SYSTEM = "libdrm"

inherit ${INHERITS}

RDEPENDS:${PN} += "${RDEPENDS_IMAGING_SUB_SYSTEM}"
DEPENDS += "${DEPENDS_IMAGING_SUB_SYSTEM}"

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/1.4.3/imaging-sub-system.tar.gz"
SRC_URI[sha256sum] = "530e0dd69c87031855e9cd726c3f6993db1b1632ec0fd90dcb3f3acc85f2bd92"

B = "${WORKDIR}/imaging-sub-system/build"
S = "${WORKDIR}/imaging-sub-system/scripts"
