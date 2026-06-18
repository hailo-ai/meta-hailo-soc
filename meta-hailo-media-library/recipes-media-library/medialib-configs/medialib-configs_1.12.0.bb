DESCRIPTION = "Media Library Configuration files recipe \
               fetches the configuration files for the media library and sets hierarchy in /usr/lib"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=4f9220a5c4c232aa3971ad6ef826474a"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.12.0-dv-10"
SRC_URI += "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/1.12.0-dv-10/hefs.tar.gz;name=hefs_${HAILO_SOC_NAME}"
SRCREV = "3a952ae6a8a4b1c9e412959412e1bf24ee36410b"
SRC_URI[hefs_hailo15.sha256sum] = "41fed4b72747a24c4b089acfe6b257ef71c5db7d03d7148f3c9060bc977207cd"
SRC_URI[hefs_hailo15l.sha256sum] = "0351624c2fb00d83a7b585366d13718d96e4d49046cc6b9ebfaeb344c55e2f99"

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