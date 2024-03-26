DESCRIPTION = "Linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit deploy hailo-cc312-sign

LINUX_VERSION = "5.15.32"
PV = "${LINUX_VERSION}"

LINUX_YOCTO_HAILO_URI ??= "git@github.com/hailo-ai/linux-yocto-hailo.git"
LINUX_YOCTO_HAILO_BRANCH ??= "1.3.0-dev"
LINUX_YOCTO_HAILO_SRCREV ??= "c740fbf4441b51799fe2098b051b15ffe9631331"
LINUX_YOCTO_HAILO_BOARD_VENDOR ?= "hailo"

KBRANCH = "${LINUX_YOCTO_HAILO_BRANCH}"
SRCREV = "${LINUX_YOCTO_HAILO_SRCREV}"

HAILO_CC312_SIGNED_BINARY = "${B}/${UBOOT_DTB_BINARY}.signed"
HAILO_CC312_UNSIGNED_BINARY = "${B}/${UBOOT_DTB_BINARY}"

SRC_URI = "git://${LINUX_YOCTO_HAILO_URI};protocol=https;branch=${KBRANCH} \
           file://defconfig \
           file://cfg/;destsuffix=cfg;type=kmeta"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'kernel_debug_en', ' file://cfg/debug-configuration.cfg', '', d)}"

SDIO0_POSTFIX = "${@bb.utils.contains('MACHINE_FEATURES', 'sdio0', '-sdio0', '', d)}"
KERNEL_DEVICETREE = "${LINUX_YOCTO_HAILO_BOARD_VENDOR}/${MACHINE}${SDIO0_POSTFIX}.dtb"

KCONFIG_MODE="--alldefconfig"

# customer certificate is deployed by the hailo-secureboot-assets
# and used for signing the fitimage
do_assemble_fitimage[depends] += "hailo-secureboot-assets:do_deploy"

do_assemble_fitimage:append() {
    # sign u-boot.dtb, generate u-boot.dtb.signed
    do_hailo_cc312_sign
}

kernel_do_deploy:append() {
    install -m 0644 ${HAILO_CC312_SIGNED_BINARY} ${DEPLOYDIR}/
}

require recipes-kernel/linux/linux-yocto.inc

RRECOMMENDS:${KERNEL_PACKAGE_NAME}-base = ""
