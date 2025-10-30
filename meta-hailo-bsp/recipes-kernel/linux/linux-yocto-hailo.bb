DESCRIPTION = "Linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit deploy hailo-common-utils

LINUX_VERSION = "5.15.32"
PV = "${LINUX_VERSION}"

LINUX_YOCTO_HAILO_URI ??= "git@github.com/hailo-ai/linux-yocto-hailo.git"
LINUX_YOCTO_HAILO_BRANCH ??= "1.9.0"
LINUX_YOCTO_HAILO_SRCREV ??= "b1fc9642b6ce6d7061c3b0cb5827e4c99b67031d"
LINUX_YOCTO_HAILO_BOARD_VENDOR ?= "hailo"
ADD_ITS_TO_FITIMAGE ?= "yes"

KBRANCH = "${LINUX_YOCTO_HAILO_BRANCH}"
SRCREV = "${LINUX_YOCTO_HAILO_SRCREV}"

FILESEXTRAPATHS:prepend:hailo10-m2 := "${THISDIR}/linux-yocto-hailo/tiny_defconfig/:"

SRC_URI = "git://${LINUX_YOCTO_HAILO_URI};protocol=https;branch=${KBRANCH} \
           file://defconfig \
           file://cfg/;destsuffix=cfg;type=kmeta"

# configurations for debug
# KASAN/UBSAN/checkers would degrade performance, so they should be used only for test build for bugchecking
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'kernel_debug_en', ' file://cfg/debug-configuration.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'linux_kasan', ' file://cfg/kasan.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'linux_ubsan', ' file://cfg/ubsan.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'linux_kernel_checkers', ' file://cfg/kernel-checkers.cfg', '', d)}"

SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'dma_zone_disable', ' file://cfg/dma-zone-disable.cfg', '', d)}"
SRC_URI:append:hailo10-m2 = " file://cfg/dma-zone-disable.cfg"
SRC_URI:append:veloce = " file://cfg/veloce.cfg"
SRC_URI:append:hailo15l = " file://cfg/hailo-i2s-warrper.cfg"
SRC_URI:append:hailo15l = " file://cfg/cma-non-reusable.cfg"
SRC_URI:append:hailo15-sbc-rev3 = " file://cfg/cma-non-reusable.cfg"
SRC_URI:append:hailo15-sbc-rev3-1 = " file://cfg/cma-non-reusable.cfg"
SRC_URI:append:hailo15l-sbc = " file://cfg/hailo15l-sbc.cfg"

SDIO0_POSTFIX = "${@bb.utils.contains('MACHINE_FEATURES', 'sdio0', '-sdio0', '', d)}"
KERNEL_DEVICETREE ?= "${LINUX_YOCTO_HAILO_BOARD_VENDOR}/${MACHINE}${SDIO0_POSTFIX}.dtb"
# Hailo10 DTS is not set by board/machine/sdio.
# It has DTS per board SKU-ID and all are specified in linux-yocto-hailo.bbappend recipe.
# Note: Hailo10 fitImage includes multiple DTBs and configurations (config per Board SKU-ID).
KERNEL_DEVICETREE:hailo10-m2 = ""

KCONFIG_MODE="--alldefconfig"

do_assemble_fitimage[depends] += "hailo-secureboot-assets:do_deploy"
do_assemble_fitimage[network] = "1"

kernel_do_deploy:append() {
    install -m 0644 ${B}/.config ${DEPLOYDIR}/kernel.config
}

require recipes-kernel/linux/linux-yocto.inc

RRECOMMENDS:${KERNEL_PACKAGE_NAME}-base = ""

do_assemble_fitimage[depends] += "${@bb.utils.contains('MACHINE_FEATURES', 'falcon_mode', ' trusted-firmware-a-hailo:do_deploy', '', d)}"

#
# Emit the fitImage ITS configuration section
#
# $1 ... .its filename - Path to the Image Tree Source file to modify
# $2 ... Linux kernel ID - Identifier for the kernel image in the FIT
# $3 ... DTB image name - Device Tree Blob image name/identifier
# $4 ... ramdisk ID - Initial ramdisk image identifier
# $5 ... u-boot script ID - U-Boot script identifier
# $6 ... config ID - Configuration section identifier
# $7 ... default flag - Flag indicating if this is the default configuration

##
# Appends ARM Trusted Firmware (ATF) configuration to FIT image when falcon mode is enabled.
#
# This function modifies the Image Tree Source (.its) file to integrate ARM Trusted Firmware
# into the boot configuration when the machine features include "falcon_mode". Falcon mode
# is a U-Boot optimization that enables direct kernel booting without loading the full U-Boot.
#
# The function performs the following modifications to the configuration section:
# 1. Appends DTB image name to the description property
# 2. Adds "firmware" to the sign-images property for cryptographic verification
# 3. Inserts firmware property referencing ARM Trusted Firmware ("atf")
# 4. Adds loadables property listing components to be loaded (DTB and kernel)
#
# @param $its_file: Path to the ITS file (inherited from parent scope)
# @param $dtb_image: DTB image name (inherited from parent scope)
# @param $kernel_id: Kernel identifier (inherited from parent scope)
#
# @modifies: The ITS file specified by $its_file
# @requires: Machine must have "falcon_mode" feature enabled
##
fitimage_emit_section_config:append() {
    if [ "${@bb.utils.contains("MACHINE_FEATURES", "falcon_mode", "1", "0", d)}" = "1" ]; then

        # Find the starting line number of the last configuration node in the ITS file
        # This ensures we modify the correct configuration section
        conf_node_start_line=$(awk '/^\s*conf-[^ ]+ *{/ {start=NR} END {print start}' "$its_file")

        # Append DTB image name to description property
        # This helps identify which device tree is associated with this configuration
        sed -i "$conf_node_start_line,\$ {/description = / s/\";$/ $dtb_image\";/}" $its_file

        # Append "firmware" to sign-images property for secure boot verification
        # This ensures the ARM Trusted Firmware is included in the signing process
        sed -i "$conf_node_start_line,\$ {/sign-images = / s/;$/, \"firmware\";/}" $its_file

        # Add firmware propery [ "firmware = "atf"; ] after kernel property
        # his establishes the firmware component that will be loaded before the kernel
        sed -i "${conf_node_start_line},\$ {/kernel = /a\\
                        firmware = \"atf\";
        }" "$its_file"

        # Add loadables property specifying the load order for DTB and kernel components after firmware propery/
        # This defines which components need to be loaded into memory during boot
        loadables_line="loadables = \"fdt-$dtb_image\", \"kernel-$kernel_id\";"
        sed -i "${conf_node_start_line},\$ {/firmware = /a\\
                        $loadables_line
        }" "$its_file"
    fi
}

#
# Emit the fitImage ITS DTB section
#
# $1 ... .its filename
# $2 ... Image counter
# $3 ... Path to DTB image
fitimage_emit_section_dtb:prepend() {
    # make sure DTB file is aligned to 64 bytes
    align_file $3 64
}

#
# Emit the fitImage ITS kernel section
#
# $1 ... .its filename
# $2 ... Image counter
# $3 ... Path to kernel image
# $4 ... Compression type
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
