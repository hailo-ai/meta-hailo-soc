DESCRIPTION = "Hailo Analytics package recipe \
               compiles hailo analytics library and copies shared objects to usr/lib/ "

LICENSE = "MIT"
MD5SUM = "4f9220a5c4c232aa3971ad6ef826474a"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=${MD5SUM}"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.10.0"
SRCREV = "9eb73ef8ec88eb3406d646585636725c972cb9ef"

inherit media-library-base media-library-downloader

S = "${WORKDIR}/git/hailo-analytics"

REQS_PATH := "${FILE_DIRNAME}/files/"
python set_reqs_file() {
    d.setVar('REQS_FILE', d.getVar('REQS_HAILO15_FILE'))
    d.setVar('ARM_APPS_DIR', d.getVar('HAILO15_DIR'))
}

DEPENDS:append = " \
    libmedialib \
    hailo-postprocess-tools \
    googletest \
    cxxopts \
    libfaiss \
    libdatachannel \
    ffmpeg \
    hailort-server \
    httplib \
    "

RDEPENDS:${PN} += " bash"

EXTRA_OEMESON += " \
        -Dapps_install_dir='/home/root/apps' \
        "

FILES:${PN} += "${libdir}/libhailo_analytics.so* /home/root/apps/* /home/root/tests/*"

do_configure:append() {
            meson ${S} ${B} \
                --prefix=/usr
}

do_install:append() {
    export DESTDIR="${D}"
    ninja -C ${B} install

    install -d ${D}/home/root/apps
    install -m 0755 ${WORKDIR}/git/tools/gst_apps/manage_config_tuning.sh \
        ${D}/home/root/apps/manage_config_tuning.sh
}
