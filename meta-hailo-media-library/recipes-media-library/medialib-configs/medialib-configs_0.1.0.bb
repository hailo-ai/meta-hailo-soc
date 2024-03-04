DESCRIPTION = "Media Library Configuration files recipe \
               fetches the configuration files for the media library and sets hierarchy in /usr/lib"

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${WORKDIR}/media-library/LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/1.2.2/media-library.tar.gz"
SRC_URI[sha256sum] = "48dd09fb201f6a3a2ac135857f9c2b419fe56e9e451affddaf0109cdfa51479f"

S = "${WORKDIR}/media-library/medialib"

ROOTFS_CONFIGS_DIR = "${D}/usr/lib/medialib/"

do_install() {
	# install config path on the rootfs
    install -d ${ROOTFS_CONFIGS_DIR}

    # copy the required files into the config path
    cp -R --no-dereference --preserve=mode,links -v ${S}/* ${ROOTFS_CONFIGS_DIR}
}

FILES:${PN} += " /usr/lib/medialib/*"
