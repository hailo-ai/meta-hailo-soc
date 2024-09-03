DESCRIPTION = "Media Library Configuration files recipe \
               fetches the configuration files for the media library and sets hierarchy in /usr/lib"

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${WORKDIR}/media-library/LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/1.4.2/media-library.tar.gz"
SRC_URI[sha256sum] = "b98c4086a462cd71005e78f9b8806a1d481eb8f9da26c655363a51c5fc30cc30"

S = "${WORKDIR}/media-library/medialib"

ROOTFS_CONFIGS_DIR = "${D}/usr/lib/medialib/"

do_install() {
	# install config path on the rootfs
    install -d ${ROOTFS_CONFIGS_DIR}

    # copy the required files into the config path
    cp -R --no-dereference --preserve=mode,links -v ${S}/* ${ROOTFS_CONFIGS_DIR}
}

FILES:${PN} += " /usr/lib/medialib/*"
