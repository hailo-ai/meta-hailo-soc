DESCRIPTION = "U-Boot & TrustedFirmware-A image"

PACKAGE_ARCH = "${MACHINE_ARCH}"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://../COPYING.MIT;md5=aa7321c8e0df442b97243c2e1d64c9ee"

inherit deploy hailo-common-utils uboot-config

SRC_URI = "file://u-boot-tfa.its \
	   file://COPYING.MIT"

DEPENDS += " dtc-native u-boot-tools-native u-boot hailo-secureboot-scripts-native"
do_compile[depends] += " u-boot:do_deploy trusted-firmware-a-hailo:do_deploy hailo-secureboot-assets:do_deploy"

do_compile[network] = "1"

do_compile() {
    rm -f ${WORKDIR}/u-boot-nodtb.bin
    cp ${DEPLOY_DIR_IMAGE}/u-boot-nodtb.bin ${WORKDIR}/u-boot-nodtb.bin
    align_file ${WORKDIR}/u-boot-nodtb.bin 64
    rm -f ${WORKDIR}/bl31.bin
    cp ${DEPLOY_DIR_IMAGE}/bl31.bin ${WORKDIR}/bl31.bin
    align_file ${WORKDIR}/bl31.bin 64
    ${UBOOT_MKIMAGE} -f ${WORKDIR}/u-boot-tfa.its ${B}/u-boot-tfa.itb
    # sign u-boot-tfa with customer key
    ${UBOOT_MKIMAGE_SIGN} -F -k ${SPL_SIGN_KEYDIR} -r ${B}/u-boot-tfa.itb ${UBOOT_MKIMAGE_SIGN_ARGS}
}

do_deploy() {
    install -m 0644 ${B}/u-boot-tfa.itb ${DEPLOYDIR}/
    install -m 0644 ${WORKDIR}/u-boot-tfa.its ${DEPLOYDIR}/
}

addtask deploy after do_compile
