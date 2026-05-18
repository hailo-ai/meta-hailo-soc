SUMMARY = "Verisilicon vivante SW package Linux kernel modules"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${S}/software/linux_reference/kernel_module/LICENSE;md5=2b2755b2924328c8efe5adbf9eca4dd9"

inherit module

VIDEO_ENCODER_BRANCH = "0.0.0.LGL-dv-LGL_26"

SRCREV = "eced4c2b56cdb50796992af9f019d8fd6dc29e38"
SRC_URI = "git://git@github.com/hailo-ai/hailo-imaging.git;protocol=https;branch=${VIDEO_ENCODER_BRANCH}"

# Source code
S = "${WORKDIR}/git/imaging-encoder"
# Build directory
B = "${WORKDIR}/build"

EXTRA_OEMAKE = "KDIR=${STAGING_KERNEL_DIR}"

do_compile() {
    oe_runmake -C ${S}/software/linux_reference/kernel_module
}

do_install() {
    #create the directories
    install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/kernel/drivers/imaging
    #installing
    install -m 555  ${S}/software/linux_reference/kernel_module/*.ko  ${D}${base_libdir}/modules/${KERNEL_VERSION}/kernel/drivers/imaging/
}

