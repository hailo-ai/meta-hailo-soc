LICENSE = "MIT & Proprietary-VSI"
LIC_FILES_CHKSUM = "file://${B}/LICENSE;md5=805d1be5d56ae9500316a754de03ab5f \
					file://${S}/LICENSE;md5=8349eaff29531f0a3c4f4c8b31185958"

INHERITS = "externalsrc ccache"
RDEPENDS_IMAGING_SUB_SYSTEM = "v4l-utils"
DEPENDS_IMAGING_SUB_SYSTEM = "libdrm v4l-utils"

inherit ${INHERITS}

RDEPENDS:${PN} += "${RDEPENDS_IMAGING_SUB_SYSTEM}"
DEPENDS += "${DEPENDS_IMAGING_SUB_SYSTEM}"

# Note: external_releaser.py sets the following variables for us when releasing version
# IMAGING_SRC_PATH, IMAGING_BINS_PATH, SRC_URI[bins.sha256sum]
# Variable controlling which branch/binary to fetch, being modified by external_releaser.py
IMAGING_SRC_PATH = "git://git@github.com/hailo-ai/hailo-imaging.git;protocol=https;branch=1.8.1"
IMAGING_BINS_PATH = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/CrossProducts/1.8.1/imaging-sub-system.tar.gz"

# We have 2 URIs: "bins" for our binaries (S3), "source" for source code (github)
SRC_URI = "${IMAGING_BINS_PATH};name=bins \
		${IMAGING_SRC_PATH};name=source"

# Hash of binaries and hash of fetched commits
SRC_URI[bins.sha256sum] = "900e5596f7a36f744ce188137022371c1822dd1e63f7276177249295ef073f69"

# Specify the commit hash of imaging repo - filled and uncommented by external_releaser
SRCREV:pn-imaging-sub-system = "cf76c68e7e147890dcce07de2054c14f16667618"
SRCREV:pn-imaging-sub-system-ext = "cf76c68e7e147890dcce07de2054c14f16667618"

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
