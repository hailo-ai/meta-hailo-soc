DESCRIPTION = "Hailo Postprocess Tools package recipe \
               compiles hailo postprocessing tools library and copies shared objects to usr/lib/ "

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.12.0-dv-4"
SRCREV = "c2f7f9359c36a96da7ee0c4ee1c9db90d41a0ba8"

inherit media-library-base

S = "${WORKDIR}/git"

MESON_SOURCEPATH = "${S}/hailo-postprocess"

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
    libmedialib \
    spdlog \
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

