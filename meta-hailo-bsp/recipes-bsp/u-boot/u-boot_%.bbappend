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

python () {
    dtbs = d.getVar('UBOOT_DTBS', True) or ""
    if dtbs:
        dtb_paths = [f"arch/arm/dts/{dtb}" for dtb in dtbs.split()]
        d.setVar('UBOOT_DTB_PATHS', " ".join(dtb_paths))
    else:
        d.setVar('UBOOT_DTB_PATHS', "u-boot.dtb")
}


do_compile:append() {
    uboot-mkenvimage -s ${UBOOT_ENV_SIZE} -o u-boot-initial-env.bin u-boot-initial-env

    # sign u-boot-spl-nodtb.bin, generate u-boot-spl.bin
    hailo15_boot_image_sign ${B}/${SPL_DIR}/${SPL_NODTB_BINARY} ${HAILO_SOC_NAME} image ${B}/${SPL_DIR}/u-boot-spl.bin.signed

    for UBOOT_DTB_PATH in ${UBOOT_DTB_PATHS}; do
        fdt_add_pubkey -a "${FIT_HASH_ALG},${FIT_SIGN_ALG}" -k "${UBOOT_SIGN_KEYDIR}" -n ${UBOOT_SIGN_KEYNAME} -r conf ${B}/${UBOOT_DTB_PATH}
        # sign u-boot.dtb, generate u-boot.dtb.signed
        hailo15_boot_image_sign ${B}/${UBOOT_DTB_PATH} ${HAILO_SOC_NAME} devicetree ${B}/$(basename ${UBOOT_DTB_PATH}).signed
    done
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

    if [ $(echo ${UBOOT_DTB_PATHS} | wc -w) -gt 1 ]; then
        for UBOOT_DTB_PATH in ${UBOOT_DTB_PATHS}; do
            install -m 0644 ${B}/$(basename ${UBOOT_DTB_PATH}).signed ${DEPLOYDIR}/u-boot-$(basename ${UBOOT_DTB_PATH}).signed
        done
    else
        UBOOT_DTB_PATH=${UBOOT_DTB_PATHS}
        install -m 0644 ${B}/${UBOOT_DTB_PATH}.signed ${DEPLOYDIR}/${UBOOT_DTB_PATH}.signed
    fi

    # since we don't declare UBOOT_DTB_BINARY, we have to manually install these
    install ${B}/${UBOOT_NODTB_BINARY} ${DEPLOYDIR}/${UBOOT_NODTB_IMAGE}
    ln -sf ${UBOOT_NODTB_IMAGE} ${DEPLOYDIR}/${UBOOT_NODTB_SYMLINK}
    ln -sf ${UBOOT_NODTB_IMAGE} ${DEPLOYDIR}/${UBOOT_NODTB_BINARY}
}
