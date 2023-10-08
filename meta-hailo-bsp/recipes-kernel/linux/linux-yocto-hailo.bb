DESCRIPTION = "Linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

LINUX_VERSION = "5.15.32"
PV = "${LINUX_VERSION}"

LINUX_YOCTO_HAILO_URI ??= "git@github.com/hailo-ai/linux-yocto-hailo.git"
LINUX_YOCTO_HAILO_BRANCH ??= "1.1.0"
LINUX_YOCTO_HAILO_SRCREV ??= "46b2521b4eb097b489d5f6056f8b02adbede2fa0"
LINUX_YOCTO_HAILO_BOARD_VENDOR ?= "hailo"

KBRANCH = "${LINUX_YOCTO_HAILO_BRANCH}"
SRCREV = "${LINUX_YOCTO_HAILO_SRCREV}"

SRC_URI = "git://${LINUX_YOCTO_HAILO_URI};protocol=https;branch=${KBRANCH} \
           file://defconfig \
           file://cfg/;destsuffix=cfg;type=kmeta"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'thermal_debug_en', ' file://cfg/thermal-configuration.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'kernel_debug_en', ' file://cfg/debug-configuration.cfg', '', d)}"

SDIO0_POSTFIX = "${@bb.utils.contains('MACHINE_FEATURES', 'sdio0', '-sdio0', '', d)}"
KERNEL_DEVICETREE = "${LINUX_YOCTO_HAILO_BOARD_VENDOR}/${MACHINE}${SDIO0_POSTFIX}.dtb"

KCONFIG_MODE="--alldefconfig"

# customer certificate is deployed by the hailo-secureboot-assets
# and used for signing the fitimage
do_assemble_fitimage[depends] += "hailo-secureboot-assets:do_deploy"

require recipes-kernel/linux/linux-yocto.inc
