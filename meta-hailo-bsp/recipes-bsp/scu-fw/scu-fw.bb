DESCRIPTION = "Hailo SCU. \
               This recipe will download the SCU firmware binary from AWS and add it to deploy"

inherit deploy
inherit hailo-cc312-sign

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

BASE_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/1.11.0-dv-LGL_34/scu-fw"
FW = "${SCU_FW_BINARY_NAME}"
FW_UNSIGNED = "${SCU_FW_UNSIGNED_BINARY_NAME}"
FW_CUSTOMER_SIGNED = "${SCU_FW_CUSTOMER_SIGNED_BINARY_NAME}"
FW_LINK = "${SCU_FW_BASE_BINARY_NAME}"
LICENSE_FILE = "LICENSE"
SRC_URI = "${BASE_URI}/${FW};name=fw_${HAILO_SOC_NAME} \
           ${BASE_URI}/${FW_UNSIGNED};name=fw_unsigned_${HAILO_SOC_NAME} \
           ${BASE_URI}/${LICENSE_FILE};name=lic"

SRC_URI[fw_hailo15.sha256sum] = "8041a86fa7169af7d3b9284af199e4815f0f62a71f7c2a80855cd50973cbc829"
SRC_URI[fw_hailo15l.sha256sum] = "aba235c2a19a73649834fa1e5b2aa29889e67cd46338861462dd5b7476ec22b6"
SRC_URI[fw_unsigned_hailo15.sha256sum] = "8fe3f44b9c8ccd98857df17c8958c3ee31178464056752771369a61ca5d8d5cc"
SRC_URI[fw_unsigned_hailo15l.sha256sum] = "a6510a0cd8fdfebddeac62f324b04392d311b8c487b5f75c1f9a41d8c0e45286"
SRC_URI[lic.sha256sum] = "ca96445e6e33ae0a82170ea847b0925c864492f0cbb6342d42c54fd647133608"

do_sign() {
  if [ -n "${HAS_CUSTOMER_ROOT_KEY}" ]; then
    hailo15_scu_firmware_sign ${WORKDIR}/${FW_UNSIGNED} ${WORKDIR}/${FW_CUSTOMER_SIGNED}
  fi
}

addtask sign after do_compile

do_deploy() {
  if [ -z "${HAS_CUSTOMER_ROOT_KEY}" ]; then
    install -m 644 -D ${WORKDIR}/${FW} ${DEPLOYDIR}/${FW}
  else
    install -m 644 -D ${WORKDIR}/${FW_CUSTOMER_SIGNED} ${DEPLOYDIR}/${FW_CUSTOMER_SIGNED}
	  ln -s -r ${DEPLOYDIR}/${FW_CUSTOMER_SIGNED} ${DEPLOYDIR}/${FW}
  fi
	ln -s -r ${DEPLOYDIR}/${FW} ${DEPLOYDIR}/${FW_LINK}

  install -m 644 -D ${WORKDIR}/${FW_UNSIGNED} ${DEPLOYDIR}/${FW_UNSIGNED}
}

addtask deploy after do_sign

# Allows a creation of a package without files. If files are added, this attribute should be removed.
ALLOW_EMPTY:${PN} = "1"

