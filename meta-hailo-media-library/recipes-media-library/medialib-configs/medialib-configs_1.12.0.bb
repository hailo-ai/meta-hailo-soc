DESCRIPTION = "Media Library Configuration files recipe \
               fetches the configuration files for the media library and sets hierarchy in /usr/lib"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=4f9220a5c4c232aa3971ad6ef826474a"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.12.0-dv-1"
SRC_URI += "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/1.12.0-dv-1/hefs.tar.gz;name=hefs_${HAILO_SOC_NAME}"
SRCREV = "238e1bdcd708f84b2f82b1e0b1ec4c1568e534f8"
SRC_URI[hefs_hailo15.sha256sum] = "2229054de36be3e19c12ae15733b28cda65bb90cde0b5ff26fe97e88697dd4ac"
SRC_URI[hefs_hailo15l.sha256sum] = "8d81a09782df4268564346277df056b2743db1c807bf4796b476721de3d1246d"

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