SUMMARY = "Verisilicon vivante SW package user space code build"

LICENSE = "Proprietary-VSI"
LIC_FILES_CHKSUM = "file://${S}/software/LICENSE;md5=2c81b04b9390e9d08d51eea41d622b4e"

INSANE_SKIP:${PN} += "already-stripped"

SO_TARGET_NAME = "libhantro_vc8000e"
SW_INC_PATH = "software/inc"
SW_SRC_COMMON_PATH = "software/source/common"
SW_LINUX_TEST_HEVC = "software/linux_reference/test/hevc"
SW_LINUX_REFS = "software/linux_reference"
APP_FILE_NAME = "hevc_testenc"

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/1.4.2/video-encoder.tar.gz"
SRC_URI[sha256sum] = "b17c8d0c12a1db3ee4174dd566910830a4ed9c786acd1f0ee236c71f2deabf19"

S = "${WORKDIR}/video-encoder"

SOURCE_DEPLOY_LIB = "${S}/${SW_LINUX_REFS}"
SOURCE_DEPLOY_INC = "${S}/${SW_INC_PATH}"
SOURCE_COMMON_DEPLOY_INC = "${S}/${SW_SRC_COMMON_PATH}"
SOURCE_DEPLOY_BIN = "${S}/${SW_LINUX_TEST_HEVC}"

RDEPENDS:video-encoder += "bash"

do_install () {
    # create the directories 
    install -d ${D}${libdir}
    install -d ${D}${includedir}
    install -d ${D}${includedir}/video_encoder
    install -d ${D}${bindir}

    # deploy artifacts 
    install -m 0755 -D ${SOURCE_DEPLOY_LIB}/${SO_TARGET_NAME}.so ${D}${libdir}
    install -m 0755 -D ${SOURCE_DEPLOY_INC}/*.h ${D}${includedir}/video_encoder
    install -m 0755 -D ${SOURCE_COMMON_DEPLOY_INC}/*.h ${D}${includedir}/video_encoder
    install -m 0755 -D ${SOURCE_DEPLOY_BIN}/${APP_FILE_NAME}  ${D}${bindir}
}

FILES:${PN}      = "${libdir}/*"
FILES:${PN}     += "${includedir}/*"
FILES:${PN}     += "${includedir}/video_encoder/*"
FILES:${PN}     += "${bindir}/*"

# For non versioned libraries - https://docs.yoctoproject.org/dev-manual/common-tasks.html#non-versioned-libraries
SOLIBS = ".so"
FILES_SOLIBSDEV = ""
