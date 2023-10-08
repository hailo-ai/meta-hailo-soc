DESCRIPTION = "Recipe generating SWU image for Hailo SoC"
SECTION = ""

LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

SRC_URI = " \
    file://sw-description \
    file://resize_rootfs.sh \
    "

SWUPDATE_MMC_INDEX = "0"
SWUPDATE_MMC_INDEX:hailo15-sbc  = "1"

IMAGE_DEPENDS = "core-image-minimal hailo-ddr-configuration-native scu-fw u-boot-tfa-image"

# images and files that will be included in the .swu image
SWUPDATE_IMAGES += "core-image-minimal"
SWUPDATE_IMAGES += "swupdate-image"
SWUPDATE_IMAGES += "fitImage"
SWUPDATE_IMAGES += "u-boot-tfa.itb"
SWUPDATE_IMAGES += "u-boot-spl.bin"
SWUPDATE_IMAGES += "u-boot-initial-env.bin"
SWUPDATE_IMAGES += "hailo_ddr_configuration.bin"
SWUPDATE_IMAGES += "hailo15_scu_fw.bin"

SWUPDATE_IMAGES_FSTYPES[core-image-minimal] = ".ext4"
SWUPDATE_IMAGES_FSTYPES[swupdate-image] = ".ext4.gz"
SWUPDATE_IMAGES_FSTYPES[fitImage] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[fitImage] = "1"
SWUPDATE_IMAGES_FSTYPES[u-boot-tfa.itb] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[u-boot-tfa.itb] = "1"
SWUPDATE_IMAGES_FSTYPES[u-boot-spl.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[u-boot-spl.bin] = "1"
SWUPDATE_IMAGES_FSTYPES[u-boot-initial-env.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[u-boot-initial-env.bin] = "1"
SWUPDATE_IMAGES_FSTYPES[hailo_ddr_configuration.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[hailo_ddr_configuration.bin] = "1"
SWUPDATE_IMAGES_FSTYPES[hailo15_scu_fw.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[hailo15_scu_fw.bin] = "1"

inherit swupdate