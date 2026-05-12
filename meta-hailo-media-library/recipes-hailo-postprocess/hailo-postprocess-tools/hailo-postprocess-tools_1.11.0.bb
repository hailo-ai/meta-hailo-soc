DESCRIPTION = "Hailo Postprocess Tools package recipe \
               compiles hailo postprocessing tools library and copies shared objects to usr/lib/ "

LICENSE = "MIT"
MD5SUM = "4f9220a5c4c232aa3971ad6ef826474a"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=${MD5SUM}"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=0.0.0.LGL-dv-LGL_21"
SRCREV = "2306fc2ab30ef7c6adb58712be83e75f5d0eb736"

inherit media-library-base

S = "${WORKDIR}/git/hailo-postprocess"

ROOTFS_POST_PROCESSES_DIR = "${libdir}/hailo-post-processes"

DEPENDS:append = " \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    rapidjson \
    cppzmq \
    json-schema-validator \
    opencv \
    xtensor \
    xtl \
    libhailort \
    libgsthailo \
    "
# meson configuration
EXTRA_OEMESON += " \
        -Dpost_processes_install_dir='${ROOTFS_POST_PROCESSES_DIR}' \
        "

FILES:${PN} += "${libdir}/hailo-post-processes/* ${libdir}/libhailo_postprocess_tools.so*"

do_install:append() {
    export DESTDIR="${D}"
    ninja -C ${B} install
}

