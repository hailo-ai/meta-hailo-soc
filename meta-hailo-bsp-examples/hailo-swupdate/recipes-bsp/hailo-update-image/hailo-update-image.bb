DESCRIPTION = "Recipe generating SWU image for Hailo SoC"
SECTION = ""

LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-or-later;md5=fed54355545ffd980b814dab4a3b312c"

RDEPENDS:${PN} += "scu-bl"

SRC_URI = " \
    file://sw-description \
    file://resize_rootfs.sh \
    file://fw_env.b.config \
    "

SWUPDATE_MMC_INDEX = "0"
SWUPDATE_MMC_INDEX:hailo15-sbc  = "1"

IMAGE_DEPENDS = "${HAILO_TARGET} scu-bl scu-fw u-boot-tfa-image"

# images and files that will be included in the .swu image
SWUPDATE_IMAGES += "${HAILO_TARGET}"
SWUPDATE_IMAGES += "swupdate-image"
SWUPDATE_IMAGES += "fitImage"
SWUPDATE_IMAGES += "u-boot-tfa.itb"
SWUPDATE_IMAGES += "u-boot-spl.bin"
SWUPDATE_IMAGES += "u-boot-initial-env.bin"
SWUPDATE_IMAGES += "u-boot.dtb.signed"
SWUPDATE_IMAGES += "${SCU_FW_BINARY_NAME}"
SWUPDATE_IMAGES += "${SCU_BL_BINARY_NAME}"
SWUPDATE_IMAGES += "scu_bl_cfg_a.bin"
SWUPDATE_IMAGES += "customer_certificate.bin"

SWUPDATE_IMAGES_FSTYPES[swupdate-image] = ".ext4.gz"
SWUPDATE_IMAGES_FSTYPES[fitImage] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[fitImage] = "1"
SWUPDATE_IMAGES_FSTYPES[u-boot-tfa.itb] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[u-boot-tfa.itb] = "1"
SWUPDATE_IMAGES_FSTYPES[u-boot-spl.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[u-boot-spl.bin] = "1"
SWUPDATE_IMAGES_FSTYPES[u-boot-initial-env.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[u-boot-initial-env.bin] = "1"
SWUPDATE_IMAGES_FSTYPES[u-boot.dtb.signed] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[u-boot.dtb.signed] = "1"
SWUPDATE_IMAGES_FSTYPES[customer_certificate.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[customer_certificate.bin] = "1"
python () {
    d.setVarFlags("SWUPDATE_IMAGES_FSTYPES",  {d.getVar("HAILO_TARGET") : ".ext4.gz"})
    d.setVarFlags("SWUPDATE_IMAGES_FSTYPES",  {d.getVar("SCU_FW_BINARY_NAME") : ""})
    d.setVarFlags("SWUPDATE_IMAGES_NOAPPEND_MACHINE",  {d.getVar("SCU_FW_BINARY_NAME") : "1"})
    d.setVarFlags("SWUPDATE_IMAGES_FSTYPES",  {d.getVar("SCU_BL_BINARY_NAME") : ""})
    d.setVarFlags("SWUPDATE_IMAGES_NOAPPEND_MACHINE",  {d.getVar("SCU_BL_BINARY_NAME") : "1"})
}
SWUPDATE_IMAGES_FSTYPES[scu_bl_cfg_a.bin] = ""
SWUPDATE_IMAGES_NOAPPEND_MACHINE[scu_bl_cfg_a.bin] = "1"
inherit swupdate
