DESCRIPTION = "Hailo DSP FW. \
              #  This recipe downloads and installs the DSP firmware"
LICENSE = "LICENSE"
LIC_FILES_CHKSUM = "file://LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"
S3_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/1.10.1/dsp-fw"
FW = "dsp-fw.elf"

SRC_URI = "${S3_URI}/${FW};name=fw_${HAILO_SOC_NAME} \
           ${S3_URI}/LICENSE;name=lic"

SRC_URI[fw_hailo15.sha256sum] = "ab8acc248de9e8bdf19739ade3dc0cebd28b92e6ebba74092a31cb0956003142"
SRC_URI[fw_hailo15l.sha256sum] = "32b48f3283149a9be766f01adeec13d73cf866600d97e7cb40adf1041accc905"
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

