DESCRIPTION = "Tunning tool for the vision pipeline."

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/1.6.0/tuning-tool.tar.gz"
SRC_URI[sha256sum] = "a4b165e923542aa6f3b388d1188cb3db72bd015d04b1ff6e3eee4b836d75be55"

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
