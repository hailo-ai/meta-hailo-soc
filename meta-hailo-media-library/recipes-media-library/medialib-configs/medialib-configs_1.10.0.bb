DESCRIPTION = "Media Library Configuration files recipe \
               fetches the configuration files for the media library and sets hierarchy in /usr/lib"

LICENSE = "Proprietary"
MD5SUM = "4f9220a5c4c232aa3971ad6ef826474a"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=${MD5SUM}"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.10.0"
SRC_URI += "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/1.10.0/hefs.tar.gz;name=hefs_${HAILO_SOC_NAME}"
SRCREV = "9eb73ef8ec88eb3406d646585636725c972cb9ef"
SRC_URI[hefs_hailo15.sha256sum] = "c4ede0dacd60cfaa9ad251370cd6ea45a8b583729bdb996cf708495ed36a0a1f"
SRC_URI[hefs_hailo15l.sha256sum] = "7119586afc43396e1bb8c8952f43c535e8b3ee4581bda88a944b7c4e3ba94a82"

S = "${WORKDIR}/git"
HEFS_DIR = "${WORKDIR}/hefs"

ROOTFS_APPS_DIR = "/home/root/apps"
ROOTFS_HOME_DIR = "/home/root"
ROOTFS_CONFIGS_DIR = "${D}/usr/lib/medialib"
VISION_GUI_DEFAULT_CONFIG ="default_config"
THEIA_4K_CONFIG_PATH = "imx678/theia_sl410m/4k"
KIT_4K_CONFIG_PATH = "imx678/kit_sc65a/4k"

do_install() {
    install -d ${ROOTFS_CONFIGS_DIR}

    # Copy the extracted files into the config path
    cp -R --no-dereference --preserve=mode,links -v ${HEFS_DIR}/* ${ROOTFS_CONFIGS_DIR}

}

FILES:${PN} += " /usr/lib/medialib/* ${ROOTFS_APPS_DIR}/resources/*"