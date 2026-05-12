DESCRIPTION = "Hailo Recovery FW. \
               This recipe will download the Recovery firmware binary from AWS and add it to deploy"

inherit deploy
inherit hailo-cc312-sign

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

BASE_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/0.0.0.LGL-dv-LGL_21/recovery-fw"
FW = "${RECOVERY_FW_BINARY_NAME}"
FW_UNSIGNED = "${RECOVERY_FW_UNSIGNED_BINARY_NAME}"
FW_CUSTOMER_SIGNED = "${RECOVERY_FW_CUSTOMER_SIGNED_BINARY_NAME}"
LICENSE_FILE = "LICENSE"
SRC_URI = "${BASE_URI}/${FW};name=fw_${HAILO_SOC_NAME} \
           ${BASE_URI}/${FW_UNSIGNED};name=fw_unsigned_${HAILO_SOC_NAME} \
           ${BASE_URI}/${LICENSE_FILE};name=lic"

SRC_URI[fw_hailo15.sha256sum] = "2eac12329b6f0d0c112bc1ee137f9f433572ddbf7f5a81d75c805a77d9dcf2f2"
SRC_URI[fw_hailo15l.sha256sum] = "a45be94f9646b48923610877eaec10be2ba8380a0ea25a4cda1edeee3c28a017"
SRC_URI[fw_unsigned_hailo15.sha256sum] = "14be9840efaecdd365423be7d9e7995ad57ad289224901c9dbc7a67e1c0831b8"
SRC_URI[fw_unsigned_hailo15l.sha256sum] = "d1c8572731c8597b9b07fcab0423a78cf42ba28264c9c0a2837f9f11a0cfc896"
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

  install -m 644 -D ${WORKDIR}/${FW_UNSIGNED} ${DEPLOYDIR}/${FW_UNSIGNED}
}

# Allows a creation of a package without files. If files are added, this attribute should be removed.
ALLOW_EMPTY:${PN} = "1"

addtask deploy after do_sign
