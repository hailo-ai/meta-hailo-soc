FILESEXTRAPATHS:prepend := "${THISDIR}/:"

DEPENDS += "u-boot-mkenvimage-native"

require u-boot-hailo.inc

SPL_BINARY = "spl/u-boot-spl.bin"

SRC_URI:append = " file://fw_env.config"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'ddr_ecc_en', ' file://cfg/hailo15_ddr_ecc_enable.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'emmc_8bit', ' file://cfg/hailo15_sdio1_8bit.cfg', '', d)}"
UBOOT_ENV_SIZE = "0x4000"

do_compile:append() {
    uboot-mkenvimage -s ${UBOOT_ENV_SIZE} -o u-boot-initial-env.bin u-boot-initial-env
}

do_install:append() {
    install -Dm 0644 ${SPL_DIR}/${SPL_DTB_BINARY} ${D}${datadir}/${SPL_DTB_BINARY}
    install -Dm 0644 ${SPL_DIR}/${SPL_NODTB_BINARY} ${D}${datadir}/${SPL_NODTB_BINARY}
}

do_deploy:append() {
    install -m 0644 ${B}/u-boot-initial-env.bin ${DEPLOYDIR}/u-boot-initial-env.bin
    install -m 0644 ${B}/spl/u-boot-spl ${DEPLOYDIR}/u-boot-spl.elf

    # do not deploy SPL related binaries here, we do it in the u-boot-tfa-image recipe
    rm -f ${DEPLOYDIR}/u-boot-spl*
}
