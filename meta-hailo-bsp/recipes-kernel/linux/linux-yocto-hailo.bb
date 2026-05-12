DESCRIPTION = "Linux kernel"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit deploy hailo-common-utils

LINUX_VERSION = "5.15.32"
PV = "${LINUX_VERSION}"

LINUX_YOCTO_HAILO_URI ??= "git@github.com/hailo-ai/linux-yocto-hailo.git"
LINUX_YOCTO_HAILO_BRANCH ??= "0.0.0.LGL-dv-LGL_22"
LINUX_YOCTO_HAILO_SRCREV ??= "5077e04f3f2219663080a1e1195df8c149818083"
LINUX_YOCTO_HAILO_BOARD_VENDOR ?= "hailo"
ADD_ITS_TO_FITIMAGE ?= "yes"

KBRANCH = "${LINUX_YOCTO_HAILO_BRANCH}"
SRCREV = "${LINUX_YOCTO_HAILO_SRCREV}"

# Special defconfigs
FILESEXTRAPATHS:prepend:hailo10-m2 := "${THISDIR}/${PN}/tiny_defconfig/:"
FILESEXTRAPATHS:prepend:hailo10-sbc-rev3 := "${THISDIR}/${PN}/accelerator_usb_defconfig/:"
FILESEXTRAPATHS:prepend:hailo10-usb-dongle := "${THISDIR}/${PN}/accelerator_usb_defconfig/:"

# Base SRC_URI without defconfig - we'll add it conditionally below
SRC_URI = "git://${LINUX_YOCTO_HAILO_URI};protocol=https;branch=${KBRANCH} \
           file://defconfig \
           file://cfg/;destsuffix=cfg;type=kmeta"

SRC_URI:append = "${@bb.utils.contains('DISTRO_FEATURES', 'securefs', ' file://cfg/dm-verity.cfg', '', d)}"

# configurations for debug
# KASAN/UBSAN/checkers would degrade performance, so they should be used only for test build for bugchecking
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'kernel_debug_en', ' file://cfg/debug-configuration.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'linux_kasan', ' file://cfg/kasan.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'linux_ubsan', ' file://cfg/ubsan.cfg', '', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'linux_kernel_checkers', ' file://cfg/kernel-checkers.cfg', '', d)}"

SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'reusable_cma', '', ' file://cfg/cma-non-reusable.cfg', d)}"
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'dma_zone_disable', ' file://cfg/dma-zone-disable.cfg', '', d)}"
SRC_URI:append:hailo10-m2 = " file://cfg/dma-zone-disable.cfg"
SRC_URI:append:veloce = " file://cfg/veloce.cfg"
SRC_URI:append:hailo15l = " file://cfg/hailo-i2s-warrper.cfg"
SRC_URI:append:hailo15l-sbc = " file://cfg/hailo15l-sbc.cfg"
SRC_URI:append:hailo15l-sbc-nand = " file://cfg/hailo15l-sbc.cfg"

SDIO0_POSTFIX = "${@bb.utils.contains('MACHINE_FEATURES', 'sdio0', '-sdio0', '', d)}"
KERNEL_DEVICETREE ?= "${LINUX_YOCTO_HAILO_BOARD_VENDOR}/${MACHINE}${SDIO0_POSTFIX}.dtb"
# Hailo10 DTS is not set by board/machine/sdio.
# It has DTS per board SKU-ID and all are specified in linux-yocto-hailo.bbappend recipe.
# Note: Hailo10 fitImage includes multiple DTBs and configurations (config per Board SKU-ID).
KERNEL_DEVICETREE:hailo10-m2 = ""

KCONFIG_MODE="--alldefconfig"

do_assemble_fitimage_verified[depends] += "hailo-secureboot-assets:do_deploy"

kernel_do_deploy:append() {
    install -m 0644 ${B}/.config ${DEPLOYDIR}/kernel.config
}

require recipes-kernel/linux/linux-yocto.inc

RRECOMMENDS:${KERNEL_PACKAGE_NAME}-base = ""

# Required for fdtput and fdtget commands (only for securefs)
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'securefs', 'dtc-native', '', d)}"

# This is an intentional work-around.
# When enabled in the context of 'kernel_do_install', this variable tells yocto that fitImage does not exist yet.
# Since we create it in a custom task later, we need to inform yocto to not look for it.
INITRAMFS_IMAGE_BUNDLE:task-install = "1"

do_assemble_fitimage_verified() {
    if [ "${@bb.utils.contains('DISTRO_FEATURES', 'securefs', 'securefs', '', d)}" = "securefs" ]; then
        # securefs is enabled - we need to modify the device tree and enable dm-verity feature

        # Source the verity env file
        # This file is created when the target filesystem is created
        # It contains variables such as ROOT_HASH, SALT, DATA_SIZE, etc.
        # which are needed to configure dm-verity in the device tree

        # Verify DM_VERITY_IMAGE_TYPE variable is set
        if [ -z "${DM_VERITY_IMAGE_TYPE}" ]; then
            bbfatal "DM_VERITY_IMAGE_TYPE variable is not set. Please define it in your configuration."
        fi

        bbdebug 1 "HAILO_TARGET=${HAILO_TARGET}"
        bbdebug 1 "DM_VERITY_IMAGE_TYPE=${DM_VERITY_IMAGE_TYPE}"
        bbdebug 1 "DEPLOY_DIR_IMAGE=${DEPLOY_DIR_IMAGE}"

        # Construct the specific verity env file path
        verity_env_file="${DEPLOY_DIR_IMAGE}/${HAILO_TARGET}.${DM_VERITY_IMAGE_TYPE}.verity.env"
        bbdebug 1 "Looking for verity env file: $verity_env_file"

        # Check if the specific verity env file exists
        if [ ! -f "$verity_env_file" ]; then
            bbdebug 1 "DEPLOY_DIR_IMAGE contents:"
            ls -la ${DEPLOY_DIR_IMAGE}/ || bbdebug 1 "DEPLOY_DIR_IMAGE does not exist or is empty"
            bbfatal "Required verity env file not found: $verity_env_file. Available files in ${DEPLOY_DIR_IMAGE}: $(ls -la ${DEPLOY_DIR_IMAGE}/ 2>/dev/null || echo 'directory does not exist')"
        fi

        # Source the verity env file
        bbdebug 1 "Found and sourcing verity env file: $verity_env_file"
        . "$verity_env_file"

        bbdebug 1 "Verity env file sourced successfully. ROOT_HASH=${ROOT_HASH:-UNSET} SALT=${SALT:-UNSET}"

        # Validate that all required verity variables are set
        required_vars="DATA_SIZE DATA_BLOCKS DATA_BLOCK_SIZE HASH_BLOCK_SIZE HASH_ALGORITHM ROOT_HASH SALT"
        for var in $required_vars; do
            eval "var_value=\$$var"
            if [ -z "$var_value" ]; then
                bbfatal "Required verity variable $var is not set after sourcing verity env file"
            else
                bbdebug 1 "Verity variable $var=$var_value"
            fi
        done

        for DTB in ${KERNEL_DEVICETREE}; do
            bbdebug 1 "Processing DTB: $DTB"

            # Normalize DTB name
            if echo $DTB | grep -q '/dts/'; then
                bbwarn "$DTB contains the full path to the the dts file, but only the dtb name should be used."
                DTB=`basename $DTB | sed 's,\.dts$,.dtb,g'`
            fi

            # Get DTB path
            DTB_PATH="${B}/arch/${ARCH}/boot/dts/$DTB"
            if [ ! -e "$DTB_PATH" ]; then
                DTB_PATH="${B}/arch/${ARCH}/boot/$DTB"
            fi

            # Check that DTB file exists
            if [ ! -e "$DTB_PATH" ]; then
                bbfatal "DTB file $DTB_PATH does not exist"
            fi

            bbdebug 1 "Found DTB at path: $DTB_PATH"

            # Get current bootargs
            set +e
            boot_args=`fdtget -t s $DTB_PATH /chosen bootargs`
            if [ -z "$boot_args" ]; then
                # no bootargs in DTB, skip
                set -e
                continue
            fi
            set -e

            # Remove the following if exist in bootargs:
            # 1. root arg
            # 2. ramdisk args
            # 3. phram args (in case they exist, they will be re-added with correct values)
            # 4. dm-verity args (in case they exist, they will be re-added with correct values)
            boot_args=`echo $boot_args | sed -e 's,root=[^ ]*,,g' -e 's,ramdisk[^ =]*=[^ ]*,,g' -e 's,phram.phram=[^ ]*,,g' -e 's,dm-mod.create="[^"]*",,g'`

            # TODO: MSW-12986 - separate this feature to be dependent on a different distro feature
            # Disable UART console when dm-verity is enabled
            boot_args=`echo $boot_args | sed -e 's,console=[^ ]*,,g'`

            # Constant size in kernel
            SECTOR_SIZE=512

            # Calculate number of sectors
            DATA_SECTORS=`echo \$(( ${DATA_SIZE} / ${SECTOR_SIZE} ))`

            FIRST_METADATA_BLOCK=`echo \$(( ${DATA_BLOCKS} + 1 ))`
            # Add dm-verity args
            boot_args="$boot_args phram.phram=ramdisk0,0x90000000,0x10000000,1 root=/dev/dm-0 dm-mod.create=\"dm-verity,,,ro,0 ${DATA_SECTORS} verity 1 /dev/mtdblock0 /dev/mtdblock0 ${DATA_BLOCK_SIZE} ${HASH_BLOCK_SIZE} ${DATA_BLOCKS} ${FIRST_METADATA_BLOCK} ${HASH_ALGORITHM} ${ROOT_HASH} ${SALT} 3 ignore_zero_blocks check_at_most_once panic_on_corruption\""

            # Write back modified bootargs
            fdtput -t s $DTB_PATH /chosen bootargs "$boot_args"

            # Reserve phram memory region
            set +e
            fdtput -c $DTB_PATH /reserved-memory/phram
            set -e
            fdtput -t x $DTB_PATH /reserved-memory/phram reg 0x00000000 0x90000000 0x00000000 0x10000000
            fdtput -t s $DTB_PATH /reserved-memory/phram no-map

            # Delete initrd-start and initrd-end if exists
            set +e
            fdtput -d -t s $DTB_PATH /chosen linux,initrd-start linux,initrd-end
            set -e

            # TODO: MSW-12986 - separate this feature to be dependent on a different distro feature
            # Get current /chosen/stdout-path
            set +e
            stdout_path=`fdtget -t s $DTB_PATH /chosen stdout-path`
            set -e
            # If stdout-path contains the word "uart", remove the stdout-path property
            # In addtion, disable the UART node pointed by the stdout-path
            if echo $stdout_path | grep -q "uart"; then
                # Remove stdout-path property
                fdtput -d -t s $DTB_PATH /chosen stdout-path
                # Disable the UART node
                fdtput -t s $DTB_PATH $stdout_path status "disabled"
            fi
        done
    fi

    # Finally, create fitImage
    do_assemble_fitimage
}

# Ensure that the target filesystem is created and verity env files are deployed before we create the fitImage
do_assemble_fitimage_verified[depends] += "${@bb.utils.contains('DISTRO_FEATURES', 'securefs', ' %s:do_deploy' % (d.getVar('HAILO_TARGET')), '', d)}"

# Add our custom task to the task graph
addtask assemble_fitimage_verified before do_deploy after do_compile

do_assemble_fitimage_verified[depends] += "${@bb.utils.contains('MACHINE_FEATURES', 'falcon_mode', ' trusted-firmware-a-hailo:do_deploy', '', d)}"

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

# Remove the original do_assemble_fitimage task, we are calling it from our custom task
python () {
        bb.build.deltask('do_assemble_fitimage', d)
}
