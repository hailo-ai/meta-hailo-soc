DESCRIPTION = "Hailo DSP FW. \
              #  This recipe downloads and installs the DSP firmware"
LICENSE = "LICENSE"
LIC_FILES_CHKSUM = "file://LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"
S3_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/1.4.2/dsp-fw"
FW = "dsp-fw.elf"

SRC_URI = "${S3_URI}/${FW};name=fw \
           ${S3_URI}/LICENSE;name=lic"

SRC_URI[fw.sha256sum] = "9bedcec757e7e2d964e7814a5566edcbea4490128cae9f83f8be3b76760821a6"
SRC_URI[lic.sha256sum] = "ca96445e6e33ae0a82170ea847b0925c864492f0cbb6342d42c54fd647133608"

# elf is compiled for 32bit (DSP), while target (A53) is Aarch64
INSANE_SKIP:${PN} += " arch"

FW_PATH = "${S}/${FW}"
FIRMWARE_INSTALL_DIR = "/lib/firmware"
ROOTFS_FIRMWARE_DIR = "${D}${FIRMWARE_INSTALL_DIR}"

S = "${WORKDIR}"
do_install() {
  install -d ${ROOTFS_FIRMWARE_DIR}
  install -m 0644 ${FW_PATH} ${ROOTFS_FIRMWARE_DIR}
}

FILES:${PN} += "${FIRMWARE_INSTALL_DIR}/${FW}"

