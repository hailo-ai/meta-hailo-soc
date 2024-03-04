# Configuration for Hailo15

#@TYPE: Machine
#@NAME: Hailo15 machine
#@DESCRIPTION: Machine configuration for Hailo15

require conf/machine/include/arm/arch-armv8a.inc

MACHINEOVERRIDES =. "hailo15:"

PREFERRED_PROVIDER_virtual/kernel_forcevariable = "linux-yocto-hailo"
PREFERRED_PROVIDER_virtual/kernel = "linux-yocto-hailo"

TARGET_ARCH = "aarch64"
TUNE_FEATURES = "aarch64"

IMAGE_FSTYPES = "wic ext4"
WKS_FILE = "sd.wks"

IMAGE_BOOT_FILES += "fitImage u-boot-tfa.itb"

KERNEL_IMAGETYPE = "fitImage"
KERNEL_CLASSES += "kernel-fitimage"
KERNEL_ALT_IMAGETYPE = "vmlinux"

WKS_FILE_DEPENDS += " linux-yocto-hailo u-boot-tfa-image"
EXTRA_IMAGEDEPENDS += " u-boot-tfa-image"
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

SERIAL_CONSOLES = "115200;ttyS1"

hostname:pn-base-files = "hailo15"

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
