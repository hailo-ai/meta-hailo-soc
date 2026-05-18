DESCRIPTION = "Tunning tool for the vision pipeline."

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/CrossProducts/0.0.0.LGL-dv-LGL_27/tuning-tool.tar.gz"
SRC_URI[sha256sum] = "fb9aebee1c55e98d91d81ea0c5ffbdb74b5ffc0159a1a70f214a02a5c25e7d93"

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
