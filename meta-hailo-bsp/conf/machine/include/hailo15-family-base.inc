# Configuration for Hailo15

#@TYPE: Machine
#@NAME: Hailo15 machine
#@DESCRIPTION: Machine configuration for Hailo15

MACHINEOVERRIDES =. "hailo15:"

require conf/machine/include/arm/arch-armv8a.inc

PREFERRED_PROVIDER_virtual/kernel_forcevariable = "linux-yocto-hailo"
PREFERRED_PROVIDER_virtual/kernel = "linux-yocto-hailo"

TARGET_ARCH = "aarch64"
TUNE_FEATURES = "aarch64"

IMAGE_FSTYPES = "wic ext4 ext4.gz"
WKS_FILE = "sd.wks.in"
# Default SD fixed size is 32M for production, for development we use 64M
SD_FIXED_SIZE = "32"

IMAGE_BOOT_FILES += "fitImage"
IMAGE_BOOT_FILES += "${@bb.utils.contains("MACHINE_FEATURES", "falcon_mode", "", "u-boot-tfa.itb", d)}"

KERNEL_IMAGETYPE = "fitImage"
KERNEL_CLASSES += "kernel-fitimage"
KERNEL_ALT_IMAGETYPE = "vmlinux"

WKS_FILE_DEPENDS += "linux-yocto-hailo"
WKS_FILE_DEPENDS += "${@bb.utils.contains("MACHINE_FEATURES", "falcon_mode", "", "u-boot-tfa-image", d)}"
EXTRA_IMAGEDEPENDS += "${@bb.utils.contains("MACHINE_FEATURES", "falcon_mode", "", "u-boot-tfa-image", d)}"
UBOOT_ELF = "u-boot"

# This is the kernel load address and entry point - which are the same address in our case.
# u-boot uncompresses and copies the kernel from the fitImage (that it loads to DRAM)
# into this address. Since u-boot SPL is copied by the SCU to 0x80000000 (DRAM start),
# and the secondary cores (cores 1-3) are running from u-boot SPL's load address,
# we can't use the beginning of the DRAM as the kernel load address.
# We load it to offset 2MiB in the DRAM since the u-boot (SPL) image is located in QSPI flash
# and is smaller than that.
UBOOT_LOADADDRESS="0x80200000"
UBOOT_ENTRYPOINT="0x80200000"
UBOOT_DTB_LOADADDRESS="0x84800000"

SERIAL_CONSOLES = "115200;ttyS1"

MACHINE_FEATURES_BACKFILL_CONSIDERED += "rtc "

TOOLCHAIN_HOST_TASK:append = " nativesdk-u-boot-tools"

UBOOT_SIGN_KEYDIR = "${DEPLOY_DIR_IMAGE}"
UBOOT_SIGN_KEYNAME = "customer"
UBOOT_MKIMAGE_DTCOPTS = "-I dts -O dtb -p 2000"
UBOOT_SIGN_ENABLE = "1"
FIT_SIGN_ALG = "rsa3072"
UBOOT_FIT_SIGN_ALG = "rsa3072"
FIT_SIGN_NUMBITS = "3072"
UBOOT_FIT_SIGN_NUMBITS = "3072"
SPL_SIGN_KEYDIR = "${DEPLOY_DIR_IMAGE}"
SPL_SIGN_KEYNAME = "customer"

SCU_FW_BINARY_EXTENSION = "bin"
SCU_FW_UNSIGNED_BINARY_EXTENSION = "unsigned.bin"
SCU_FW_CUSTOMER_SIGNED_BINARY_EXTENSION = "customer_signed.bin"
SCU_FW_SUFFIX = "scu_fw"
SCU_FW_BASE_BINARY_NAME = "${SCU_FW_SUFFIX}.${SCU_FW_BINARY_EXTENSION}"
SCU_FW_BINARY_NAME = "${SCU_FW_MACHINE}_${SCU_FW_BASE_BINARY_NAME}"
SCU_FW_UNSIGNED_BINARY_NAME = "${SCU_FW_MACHINE}_${SCU_FW_SUFFIX}.${SCU_FW_UNSIGNED_BINARY_EXTENSION}"
SCU_FW_CUSTOMER_SIGNED_BINARY_NAME = "${SCU_FW_MACHINE}_${SCU_FW_SUFFIX}.${SCU_FW_CUSTOMER_SIGNED_BINARY_EXTENSION}"

SCU_BL_BINARY_EXTENSION = "bin"
SCU_BL_UNSIGNED_BINARY_EXTENSION = "unsigned.bin"
SCU_BL_CUSTOMER_SIGNED_BINARY_EXTENSION = "customer_signed.bin"
SCU_BL_SUFFIX = "scu_bl"
SCU_BL_BINARY_NAME = "${SCU_BL_MACHINE}_${SCU_BL_SUFFIX}.${SCU_BL_BINARY_EXTENSION}"
SCU_BL_UNSIGNED_BINARY_NAME = "${SCU_BL_MACHINE}_${SCU_BL_SUFFIX}.${SCU_BL_UNSIGNED_BINARY_EXTENSION}"
SCU_BL_CUSTOMER_SIGNED_BINARY_NAME = "${SCU_BL_MACHINE}_${SCU_BL_SUFFIX}.${SCU_BL_CUSTOMER_SIGNED_BINARY_EXTENSION}"

RECOVERY_FW_BINARY_EXTENSION = "bin"
RECOVERY_FW_UNSIGNED_BINARY_EXTENSION = "unsigned.bin"
RECOVERY_FW_CUSTOMER_SIGNED_BINARY_EXTENSION = "customer_signed.bin"
RECOVERY_FW_SUFFIX = "uart_recovery_fw"
RECOVERY_FW_BINARY_NAME = "${RECOVERY_FW_MACHINE}_${RECOVERY_FW_SUFFIX}.${RECOVERY_FW_BINARY_EXTENSION}"
RECOVERY_FW_UNSIGNED_BINARY_NAME = "${RECOVERY_FW_MACHINE}_${RECOVERY_FW_SUFFIX}.${RECOVERY_FW_UNSIGNED_BINARY_EXTENSION}"
RECOVERY_FW_CUSTOMER_SIGNED_BINARY_NAME = "${RECOVERY_FW_MACHINE}_${RECOVERY_FW_SUFFIX}.${RECOVERY_FW_CUSTOMER_SIGNED_BINARY_EXTENSION}"

HAILO_TARGET ?= "core-image-minimal"

# make sure fitImage and u-boot-tfa.itb devicetree blobs are aligned to 64 bytes
# this is done only in the call to mkimage that does the signing, not in the call to mkimage that creates the image
# otherwise the image verification will fail
UBOOT_MKIMAGE_SIGN_ARGS:append = " -E -B 0x40"
