LICENSE = "MIT & Proprietary-VSI"
LIC_FILES_CHKSUM = "file://${B}/LICENSE;md5=805d1be5d56ae9500316a754de03ab5f \
					file://${S}/LICENSE;md5=8349eaff29531f0a3c4f4c8b31185958"

INHERITS = "externalsrc ccache"
RDEPENDS_IMAGING_SUB_SYSTEM = "v4l-utils"
DEPENDS_IMAGING_SUB_SYSTEM = "libdrm v4l-utils"

inherit ${INHERITS}

RDEPENDS:${PN} += "${RDEPENDS_IMAGING_SUB_SYSTEM}"
DEPENDS += "${DEPENDS_IMAGING_SUB_SYSTEM}"

IMAGING_COMPILATION_MODE ?= 'release'
BUILD_TYPE = "${@'release' if '${IMAGING_COMPILATION_MODE}' == 'release' else 'debug'}"

# Note: external_releaser.py sets the following variables for us when releasing version
# IMAGING_SRC_PATH, IMAGING_BINS_PATH, SRC_URI[bins.sha256sum]
# Variable controlling which branch/binary to fetch, being modified by external_releaser.py
IMAGING_SRC_PATH = "git://git@github.com/hailo-ai/hailo-imaging.git;protocol=https;branch=1.10.0"
IMAGING_BINS_PATH = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/CrossProducts/1.10.0/imaging-sub-system.tar.gz"

# We have 2 URIs: "bins" for our binaries (S3), "source" for source code (github)
SRC_URI = "${IMAGING_BINS_PATH};name=bins \
		${IMAGING_SRC_PATH};name=source"

# Hash of binaries and hash of fetched commits
SRC_URI[bins.sha256sum] = "bfe90dcc82244b109ef3e14aa2d3e50dd9821ab0cff37fe234d6450e3b36c86e"

# Specify the commit hash of imaging repo - filled and uncommented by external_releaser
SRCREV:pn-imaging-sub-system = "1a0392e3df331388fb359f01ba439898fbe2d0cd"
SRCREV:pn-imaging-sub-system-ext = "1a0392e3df331388fb359f01ba439898fbe2d0cd"

B = "${WORKDIR}/imaging-sub-system/build"
S = "${WORKDIR}/imaging-sub-system/scripts"

# Path to github clone of source files
SRC_GIT="${WORKDIR}/git"

PACKAGES = "${PN} ${PN}-dev"
INSANE_SKIP:${PN}-dev =  "file-rpaths dev-so debug-files rpaths staticdev installed-vs-shipped"
INSANE_SKIP:${PN} =  "file-rpaths dev-so debug-files rpaths staticdev installed-vs-shipped"

# FIXME: why? arch? 
do_package_qa[noexec] = "1"
EXCLUDE_FROM_SHLIBS = "1"

FILES:${PN}     += "/lib/* /lib/*/*"
FILES:${PN}     += "${bindir}/*"
FILES:${PN}     += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/isp_media_server.service', '', d)}"

