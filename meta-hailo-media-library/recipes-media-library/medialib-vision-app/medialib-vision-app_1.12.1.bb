DESCRIPTION = "Media Library vision control application \
               fetches the client application that allows control media library image properties"

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/CrossProducts/1.12.1-dv-7/vision_control.tar.gz"
SRC_URI[sha256sum] = "f1b7803efea1d19a4cd6f72c7dfae02ccee72eb03ed519a2501671a4b294e317"

ROOTFS_CONFIGS_DIR = "${D}/usr/share/hailo/webpage"
S = "${WORKDIR}/vision_control"

do_install() {
	# install config path on the rootfs
    install -d ${ROOTFS_CONFIGS_DIR}
	    # copy the required files into the config path
    cp -R --no-dereference --preserve=mode,links -v ${S}/* ${ROOTFS_CONFIGS_DIR}
}

FILES:${PN} += " /usr/share/hailo/webpage/*"
