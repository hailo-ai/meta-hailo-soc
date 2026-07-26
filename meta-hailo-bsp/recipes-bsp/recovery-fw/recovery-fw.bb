DESCRIPTION = "Hailo Recovery FW. \
               This recipe will download the Recovery firmware binary from AWS and add it to deploy"

inherit deploy
inherit hailo-cc312-sign

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

BASE_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/1.12.1-dv-6/recovery-fw"
FW = "${RECOVERY_FW_BINARY_NAME}"
FW_UNSIGNED = "${RECOVERY_FW_UNSIGNED_BINARY_NAME}"
FW_CUSTOMER_SIGNED = "${RECOVERY_FW_CUSTOMER_SIGNED_BINARY_NAME}"
LICENSE_FILE = "LICENSE"
SRC_URI = "${BASE_URI}/${FW};name=fw_${HAILO_SOC_NAME} \
           ${BASE_URI}/${FW_UNSIGNED};name=fw_unsigned_${HAILO_SOC_NAME} \
           ${BASE_URI}/${LICENSE_FILE};name=lic"

SRC_URI[fw_hailo15.sha256sum] = "549716ca991f4b4768ed70cb7cb56256325754a54b3703d65f08e435c7676ed2"
SRC_URI[fw_hailo15l.sha256sum] = "fa5cb01f8ec353d151499ee5f560df0f6b27ecbf55480c70a140b3d91e9567cf"
SRC_URI[fw_unsigned_hailo15.sha256sum] = "547e92abe50872e65cf7021a02c4ea88d4ff6adadcaf273ff07ae084e7b612f3"
SRC_URI[fw_unsigned_hailo15l.sha256sum] = "4cdf26b25488e6efd95e9226f5878d02cd4617dc9d170cac58810fcda25969b6"
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
