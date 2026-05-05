DESCRIPTION = "Media Library Configuration files recipe \
               fetches the configuration files for the media library and sets hierarchy in /usr/lib"

LICENSE = "Proprietary"
MD5SUM = "4f9220a5c4c232aa3971ad6ef826474a"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=${MD5SUM}"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=0.0.0.LGL-dv-LGL_14"
SRC_URI += "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/0.0.0.LGL-dv-LGL_14/hefs.tar.gz;name=hefs_${HAILO_SOC_NAME}"
SRCREV = "12ddf813e26f7cf7708d477979d6da3cd5ae0c68"
SRC_URI[hefs_hailo15.sha256sum] = "ddce935308de7af86b7051c3ad3abea859284afc151f53d62a2c0046766e6c20"
SRC_URI[hefs_hailo15l.sha256sum] = "aded123a0b527d7615d2656636e0f394d4680d943e972f19799cae76ce3e79cc"

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