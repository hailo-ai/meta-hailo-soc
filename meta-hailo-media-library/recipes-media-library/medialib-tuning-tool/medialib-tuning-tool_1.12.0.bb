DESCRIPTION = "Tunning tool for the vision pipeline."

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Proprietary;md5=0557f9d92cf58f2ccdd50f62f8ac0b28"

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/CrossProducts/1.12.0-dv-2/tuning-tool.tar.gz"
SRC_URI[sha256sum] = "8af65c66108cf168a14ad6e63d7ccfc5742f713a3b072a32d91712bfa9f0794f"

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
