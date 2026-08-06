DESCRIPTION = "Tunning tool for the vision pipeline."

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/CrossProducts/1.12.1-dv-7/tuning-tool.tar.gz"
SRC_URI[sha256sum] = "a0c42e789ad4c642fbe50fa39a1ef6a2a075c869dc595d0d5ff3d169757687e6"

ROOTFS_CONFIGS_DIR = "${D}/usr/bin/hailo"

RDEPENDS:${PN} += "bash zlib"
# No need to configure/compile
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # install config path on the rootfs
    install -d ${ROOTFS_CONFIGS_DIR}
    # copy the required files into the config path
    install -m 0755 -D  ${WORKDIR}/tuning-tool ${WORKDIR}/startup.sh ${ROOTFS_CONFIGS_DIR}
    # Create a symlink from /usr/bin/hailo/tuning-tool to /usr/bin/start-tuning
    ln -sf /usr/bin/hailo/startup.sh ${D}/usr/bin/hailo-tuning-tool
}

FILES:${PN} += " /usr/bin/hailo/* /usr/bin/tuning-tool"
INSANE_SKIP:${PN} += "already-stripped"
