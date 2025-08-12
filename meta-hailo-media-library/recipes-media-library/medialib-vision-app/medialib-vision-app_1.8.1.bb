DESCRIPTION = "Media Library vision control application \
               fetches the client application that allows control media library image properties"

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/CrossProducts/1.8.1/vision_control.tar.gz"
SRC_URI[sha256sum] = "6f66399c270157ca8ffd0e2764bac6950b241baab2e0580a62b420ae1cbf2e9f"

ROOTFS_CONFIGS_DIR = "${D}/usr/share/hailo/webpage"
S = "${WORKDIR}/vision_control"

do_install() {
	# install config path on the rootfs
    install -d ${ROOTFS_CONFIGS_DIR}
	    # copy the required files into the config path
    cp -R --no-dereference --preserve=mode,links -v ${S}/* ${ROOTFS_CONFIGS_DIR}
}

FILES:${PN} += " /usr/share/hailo/webpage/*"
