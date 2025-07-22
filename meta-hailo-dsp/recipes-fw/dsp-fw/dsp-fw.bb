DESCRIPTION = "Hailo DSP FW. \
              #  This recipe downloads and installs the DSP firmware"
LICENSE = "LICENSE"
LIC_FILES_CHKSUM = "file://LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"
S3_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/1.8.0/dsp-fw"
FW = "dsp-fw.elf"

SRC_URI = "${S3_URI}/${FW};name=fw_${HAILO_SOC_NAME} \
           ${S3_URI}/LICENSE;name=lic"

SRC_URI[fw_hailo15.sha256sum] = "d4ddb3e9150229d222b60fcbe65d9602f4c8df320ec32fe3b59368748fb00c32"
SRC_URI[fw_hailo15l.sha256sum] = "aace289ce47c166b5ab48ec242bf9e8c5c4412210a73cb0a5086123834f51846"
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

