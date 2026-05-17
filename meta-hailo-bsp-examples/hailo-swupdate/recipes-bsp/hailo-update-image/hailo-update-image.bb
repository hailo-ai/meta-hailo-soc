DESCRIPTION = "Recipe generating SWU image for Hailo SoC"
SECTION = ""

LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

RDEPENDS:${PN} += "scu-bl"
RDEPENDS:${PN}:remove:hailo12l-evb = "scu-bl"
RDEPENDS:${PN}:remove:hailo12l-maple = "scu-bl"
RDEPENDS:${PN}:remove:hailo12l-mint = "scu-bl"
RDEPENDS:${PN}:remove:hailo12l-veloce = "scu-bl"

FILESEXTRAPATHS:prepend:accelerator := "${THISDIR}/files/h10-usb/:"
FILESEXTRAPATHS:prepend:hailo15l-sbc-nand := "${THISDIR}/files/h15l-sbc-nand/:"

SRC_URI = " \
    file://sw-description \
    file://resize_fs.sh \
    file://update_fw_env.sh \
    file://preserve_network.sh \
    file://save_network_before_init.sh \
    file://copy_network_to_data.sh \
    "
SRC_URI:remove:accelerator = "file://resize_fs.sh"

# Custom signing only for accelerator builds
SWUPDATE_SIGNING:accelerator = "CUSTOM"

# Arguments (passed by SWUpdate):
#   $1 - BINDIR: Directory containing hailo_mkimage_wrapper_atftp.py
#   $2 - KEYDIR: Directory containing signing keys
#   $3 - MKARGS: Additional arguments for mkimage wrapper
#   $4 - SWDESC: Path to sw-description file to be signed
SWUPDATE_SIGN_TOOL:accelerator = "${THISDIR}/files/sign_swu.sh \
    ${STAGING_BINDIR_NATIVE} \
    ${SPL_SIGN_KEYDIR} \
    '${UBOOT_MKIMAGE_SIGN_ARGS}' \
    ${WORKDIR}/hailo-update-image"

# Add dependency on hailo-secureboot-scripts-native for hailo_mkimage_wrapper_atftp.py (accelerator only)
DEPENDS:append:accelerator = " hailo-secureboot-scripts-native"
do_swuimage[depends] += "${@'hailo-secureboot-scripts-native:do_populate_sysroot' if 'accelerator' in d.getVar('OVERRIDES').split(':') else ''}"

SWUPDATE_DEFAULT_FILESYSTEM_DEVICE = "mmcblk0"
SWUPDATE_DEFAULT_FILESYSTEM_DEVICE:hailo15-sbc  = "mmcblk1"
SWUPDATE_DEFAULT_FILESYSTEM_DEVICE:hailo15l-sbc  = "mmcblk1"
SWUPDATE_DEFAULT_FILESYSTEM_DEVICE:hailo15l-sbc-nand  = ""
SWUPDATE_DEFAULT_FIRMWARE_DEVICE = "mtdblock0"
SWUPDATE_DEFAULT_FIRMWARE_DEVICE:hailo15l = "mmcblk1boot0"
SWUPDATE_DEFAULT_FIRMWARE_DEVICE:hailo15l-sbc-nand = "mtdblock0"
SWUPDATE_DEFAULT_FW_ENV_DEVICE = "mtd0"
SWUPDATE_DEFAULT_FW_ENV_DEVICE:hailo15l = "mmcblk1boot0"
SWUPDATE_DEFAULT_FW_ENV_DEVICE:hailo15l-sbc-nand = "mtd0"
SWUPDATE_DEFAULT_BOOTLOADER_DEVICE:hailo15l-sbc-nand = "mtd1"
SWUPDATE_DEFAULT_FITIMAGE_DEVICE:hailo15l-sbc-nand = "mtd2"
SWUPDATE_DEFAULT_ROOTFS_DEVICE:hailo15l-sbc-nand = "mtd3"

IMAGE_DEPENDS = "scu-bl scu-fw virtual/kernel ${HAILO_TARGET} swupdate-image u-boot-tfa-image"
IMAGE_DEPENDS:remove:accelerator = "${HAILO_TARGET} swupdate-image u-boot-tfa-image"

# Base images included in all SWU packages
SWUPDATE_IMAGES += "fitImage"
SWUPDATE_IMAGES += "u-boot-spl.bin"
SWUPDATE_IMAGES += "u-boot-initial-env.bin"
SWUPDATE_IMAGES += "u-boot.dtb.signed"
SWUPDATE_IMAGES += "${SCU_FW_BINARY_NAME}"
SWUPDATE_IMAGES += "${SCU_BL_BINARY_NAME}"
SWUPDATE_IMAGES += "scu_bl_cfg_a.bin"
SWUPDATE_IMAGES += "customer_certificate.bin"

# Additional images for non-accelerator machines (removed for accelerator)
SWUPDATE_IMAGES += "${HAILO_TARGET} swupdate-image u-boot-tfa.itb"
SWUPDATE_IMAGES:remove:accelerator = "${HAILO_TARGET} swupdate-image u-boot-tfa.itb"

# Base filesystem type configurations
SWUPDATE_IMAGES_FSTYPES[fitImage] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[fitImage] = "1"
SWUPDATE_IMAGES_FSTYPES[u-boot-spl.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[u-boot-spl.bin] = "1"
SWUPDATE_IMAGES_FSTYPES[u-boot-initial-env.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[u-boot-initial-env.bin] = "1"
SWUPDATE_IMAGES_FSTYPES[u-boot.dtb.signed] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[u-boot.dtb.signed] = "1"
SWUPDATE_IMAGES_FSTYPES[customer_certificate.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[customer_certificate.bin] = "1"
python () {
    # Check if accelerator override is active
    overrides = d.getVar("OVERRIDES") or ""
    is_accelerator = "accelerator" in overrides.split(":")

    # Configure HAILO_TARGET and related images only for non-accelerator
    if not is_accelerator:
        rootfs_extension = ".ubifs" if d.getVar("MACHINE") == "hailo15l-sbc-nand" else ".ext4.gz"
        d.setVarFlags("SWUPDATE_IMAGES_FSTYPES", {d.getVar("HAILO_TARGET"): rootfs_extension})
        d.setVarFlag("SWUPDATE_IMAGES_FSTYPES", "swupdate-image", ".ext4.gz")
        d.setVarFlag("SWUPDATE_IMAGES_FSTYPES", "u-boot-tfa.itb", "")
        d.setVarFlag("SWUPDATE_IMAGES_NOAPPEND_MACHINE", "u-boot-tfa.itb", "1")

    d.setVarFlags("SWUPDATE_IMAGES_FSTYPES",  {d.getVar("SCU_FW_BINARY_NAME") : ""})
    d.setVarFlags("SWUPDATE_IMAGES_NOAPPEND_MACHINE",  {d.getVar("SCU_FW_BINARY_NAME") : "1"})
    d.setVarFlags("SWUPDATE_IMAGES_FSTYPES",  {d.getVar("SCU_BL_BINARY_NAME") : ""})
    d.setVarFlags("SWUPDATE_IMAGES_NOAPPEND_MACHINE",  {d.getVar("SCU_BL_BINARY_NAME") : "1"})
}
SWUPDATE_IMAGES_FSTYPES[scu_bl_cfg_a.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[scu_bl_cfg_a.bin] = "1"
inherit swupdate
