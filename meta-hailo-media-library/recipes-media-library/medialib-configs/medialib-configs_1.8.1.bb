DESCRIPTION = "Media Library Configuration files recipe \
               fetches the configuration files for the media library and sets hierarchy in /usr/lib"

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${RESOURCES_DIR}/sensors/LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.8.1"
SRC_URI += "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/1.8.1/hefs.tar.gz;name=hefs_${HAILO_SOC_NAME}"
SRCREV = "53e9105aa7c55c08cc92abe6120130fa69195f5d"
SRC_URI[hefs_hailo15.sha256sum] = "169620998db41d46b5f424c0601f148a08696fb0641a6fdee7634f42b9333934"
SRC_URI[hefs_hailo15l.sha256sum] = "4a5f3030680a217e00e97546f347e97190ebbb727826fd575593909012d8077a"

S = "${WORKDIR}/git"
RESOURCES_DIR = "${S}/resources"
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
    cp -R --no-dereference --preserve=mode,links -v ${RESOURCES_DIR}/* ${ROOTFS_CONFIGS_DIR}
    cp -R --no-dereference --preserve=mode,links -v ${HEFS_DIR}/* ${ROOTFS_CONFIGS_DIR}

    # copy media library resources
    install -d ${D}/${ROOTFS_APPS_DIR}/resources
    install -m 0755 ${RESOURCES_DIR}/sensors/${KIT_4K_CONFIG_PATH}/dewarp/cam_intrinsics.txt \
        ${D}/${ROOTFS_APPS_DIR}/resources/cam_intrinsics_678.txt
    install -m 0755 ${RESOURCES_DIR}/sensors/imx678/kit_sc65a/fhd/dewarp/cam_intrinsics.txt \
        ${D}/${ROOTFS_APPS_DIR}/resources/cam_intrinsics_imx678_fhd.txt
    install -m 0755 ${RESOURCES_DIR}/sensors/imx678/theia_sl410m/4k/dewarp/cam_intrinsics.txt \
        ${D}/${ROOTFS_APPS_DIR}/resources/cam_intrinsics_theia_678.txt
    install -m 0755 ${RESOURCES_DIR}/sensors/imx334/kit_sc65a/4k/dewarp/cam_intrinsics.txt \
        ${D}/${ROOTFS_APPS_DIR}/resources/cam_intrinsics_334.txt
    install -m 0755 ${RESOURCES_DIR}/sensors/${KIT_4K_CONFIG_PATH}/dewarp/cam_intrinsics.txt \
        ${D}/${ROOTFS_APPS_DIR}/resources/cam_intrinsics.txt
    install -m 0755 ${RESOURCES_DIR}/sensors/imx675/theia_sl410m/5mp/dewarp/cam_intrinsics.txt \
        ${D}/${ROOTFS_APPS_DIR}/resources/cam_intrinsics_675.txt

    ln -s -r ${ROOTFS_CONFIGS_DIR}/sensors/${THEIA_4K_CONFIG_PATH} \
        ${ROOTFS_CONFIGS_DIR}/${VISION_GUI_DEFAULT_CONFIG}

}

FILES:${PN} += " /usr/lib/medialib/* ${ROOTFS_APPS_DIR}/resources/*"