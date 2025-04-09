DESCRIPTION = "Linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit deploy hailo-cc312-sign hailo-common-utils

LINUX_VERSION = "5.15.32"
PV = "${LINUX_VERSION}"

LINUX_YOCTO_HAILO_URI ??= "git@github.com/hailo-ai/linux-yocto-hailo.git"
LINUX_YOCTO_HAILO_BRANCH ??= "1.7.0"
LINUX_YOCTO_HAILO_SRCREV ??= "6d97428ef6e45e609704fcb2f90198c0f8a0a0cb"
LINUX_YOCTO_HAILO_BOARD_VENDOR ?= "hailo"
ADD_ITS_TO_FITIMAGE ?= "yes"

KBRANCH = "${LINUX_YOCTO_HAILO_BRANCH}"
SRCREV = "${LINUX_YOCTO_HAILO_SRCREV}"

SIGNED_UBOOT_DTB = "${B}/${UBOOT_DTB_BINARY}.signed"

SRC_URI = "git://${LINUX_YOCTO_HAILO_URI};protocol=https;branch=${KBRANCH} \
           file://defconfig \
           file://cfg/;destsuffix=cfg;type=kmeta"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'kernel_debug_en', ' file://cfg/debug-configuration.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'dma_zone_disable', ' file://cfg/dma-zone-disable.cfg', '', d)}"
SRC_URI:append:hailo10-m2 = " file://cfg/dma-zone-disable.cfg"
SRC_URI:append:veloce = " file://cfg/veloce.cfg"

SDIO0_POSTFIX = "${@bb.utils.contains('MACHINE_FEATURES', 'sdio0', '-sdio0', '', d)}"
KERNEL_DEVICETREE ?= "${LINUX_YOCTO_HAILO_BOARD_VENDOR}/${MACHINE}${SDIO0_POSTFIX}.dtb"

KCONFIG_MODE="--alldefconfig"

# customer certificate is deployed by the hailo-secureboot-assets
# and used for signing the fitimage
do_assemble_fitimage[depends] += "hailo-secureboot-assets:do_deploy"

do_assemble_fitimage:append() {
    # sign u-boot.dtb, generate u-boot.dtb.signed
    hailo15_boot_image_sign ${B}/${UBOOT_DTB_BINARY} ${HAILO_SOC_NAME} devicetree ${SIGNED_UBOOT_DTB}
}

kernel_do_deploy:append() {
    install -m 0644 ${SIGNED_UBOOT_DTB} ${DEPLOYDIR}/
    install -m 0644 ${B}/.config ${DEPLOYDIR}/kernel.config
}

require recipes-kernel/linux/linux-yocto.inc

RRECOMMENDS:${KERNEL_PACKAGE_NAME}-base = ""

do_assemble_fitimage[depends] += "${@bb.utils.contains('MACHINE_FEATURES', 'falcon_mode', ' trusted-firmware-a-hailo:do_deploy', '', d)}"

fitimage_emit_section_config:append() {
    if [ "${@bb.utils.contains("MACHINE_FEATURES", "falcon_mode", "1", "0", d)}" = "1" ]; then
        # add ATF to the list of images being signed
        sign_line_w_atf=$(echo $sign_line | sed -e "s/;/,\"firmware\";/g")
        # replace sign line with the new one
        sed -e "s/${sign_line}/${sign_line_w_atf}/g" -i $its_file

        # add ATF to to the configuration
        firmware_line='firmware = "atf";'
        loadables_line="loadables = \"fdt-$dtb_image\", \"kernel-$kernel_id\";"
        sed -e "s/${kernel_line}/${kernel_line}\n${firmware_line}\n${loadables_line}/g" -i $its_file
    fi
}

fitimage_emit_section_dtb:prepend() {
    # make sure DTB file is aligned to 64 bytes
    align_file $3 64
}

fitimage_emit_section_kernel:append() {
    if [ "${@bb.utils.contains("MACHINE_FEATURES", "falcon_mode", "1", "0", d)}" = "1" ]; then
        its_file=$1

        rm -f ${B}/bl31.bin
        cp ${DEPLOY_DIR_IMAGE}/bl31.bin ${B}/bl31.bin
        align_file ${B}/bl31.bin 64

        cat << EOF >> $its_file
                atf {
                        description = "ARM TrustedFirmware-A";
                        data = /incbin/("bl31.bin");
                        type = "firmware";
                        os = "arm-trusted-firmware";
                        arch = "${UBOOT_ARCH}";
                        compression = "none";
                        load = <0x80000000>;
                        entry = <0x80000000>;
                        hash-1 {
                                algo = "$kernel_csum";
                        };
                };
EOF
    fi

    if [ "${ADD_ITS_TO_FITIMAGE}" = "yes" ]; then
        its_file=$1

        cat << EOF >> $its_file
                fit-image.its {
                        description = "ITS Source File";
                        data = /incbin/("$its_file");
                        type = "file";
                        compression = "none";
                        hash-1 {
                                algo = "$kernel_csum";
                        };
                };
EOF
    fi
}

uboot_prep_kimage:append() {
    # make sure linux.bin is alligned to 64 bytes
    align_file linux.bin 64
}
