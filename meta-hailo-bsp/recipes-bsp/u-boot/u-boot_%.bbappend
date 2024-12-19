FILESEXTRAPATHS:prepend := "${THISDIR}/:"

DEPENDS += "u-boot-mkenvimage-native"

require u-boot-hailo.inc

inherit hailo-cc312-sign

SRC_URI:append = " file://fw_env.config"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'ddr_ecc_en', ' file://cfg/hailo15_ddr_ecc_enable.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'emmc_8bit', ' file://cfg/hailo15_sdio1_8bit.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'coresight', ' file://cfg/coresight.cfg', '', d)}"
UBOOT_ENV_SIZE = "0x4000"

do_compile[depends] += " hailo-secureboot-assets:do_deploy"

do_compile:append() {
    uboot-mkenvimage -s ${UBOOT_ENV_SIZE} -o u-boot-initial-env.bin u-boot-initial-env
    # sign u-boot-spl-nodtb.bin, generate u-boot-spl.bin
    hailo15_boot_image_sign ${B}/${SPL_DIR}/${SPL_NODTB_BINARY} image ${B}/${SPL_DIR}/u-boot-spl.bin.signed
}

do_configure:append() {
    sed -i "s/.*CONFIG_CORE_IMAGE_NAME.*/CONFIG_CORE_IMAGE_NAME=\"${HAILO_TARGET}\"/" ${B}/.config
}

do_install:append() {
    install -Dm 0644 ${SPL_DIR}/${SPL_NODTB_BINARY} ${D}${datadir}/${SPL_NODTB_BINARY}
}

do_deploy:append() {
    install -m 0644 ${B}/u-boot-initial-env.bin ${DEPLOYDIR}/u-boot-initial-env.bin

    # do not deploy default u-boot-spl files, only our signed dtb
    rm -f ${DEPLOYDIR}/u-boot-spl*
    install -m 0644 ${B}/${SPL_DIR}/u-boot-spl.bin.signed ${DEPLOYDIR}/u-boot-spl.bin
    install -m 0644 ${B}/${SPL_DIR}/u-boot-spl ${DEPLOYDIR}/u-boot-spl.elf
}
