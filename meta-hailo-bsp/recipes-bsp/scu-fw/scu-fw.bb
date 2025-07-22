DESCRIPTION = "Hailo SCU. \
               This recipe will download the SCU firmware binary from AWS and add it to deploy"

inherit deploy
inherit hailo-cc312-sign

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

BASE_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/1.8.0/scu-fw"
FW = "${SCU_FW_BINARY_NAME}"
FW_UNSIGNED = "${SCU_FW_UNSIGNED_BINARY_NAME}"
FW_CUSTOMER_SIGNED = "${SCU_FW_CUSTOMER_SIGNED_BINARY_NAME}"
FW_LINK = "${SCU_FW_BASE_BINARY_NAME}"
LICENSE_FILE = "LICENSE"
SRC_URI = "${BASE_URI}/${FW};name=fw_${HAILO_SOC_NAME} \
           ${BASE_URI}/${FW_UNSIGNED};name=fw_unsigned_${HAILO_SOC_NAME} \
           ${BASE_URI}/${LICENSE_FILE};name=lic"

SRC_URI[fw_hailo15.sha256sum] = "1e70162c2436d825619c237e0ffd49b8afef210417df6a9ed37518244283db4b"
SRC_URI[fw_hailo15l.sha256sum] = "8804d2f6983b96bfc78e033e17884fe9f3f55745a9b8b6ebeead61da144c9d5a"
SRC_URI[fw_unsigned_hailo15.sha256sum] = "a7b64adb65d1352b998da7fd86a66b75d67cc0d6145f7ff0ac93224eda278060"
SRC_URI[fw_unsigned_hailo15l.sha256sum] = "8c15a876e866c3e97b80d7f12e46198daf003724f280bdd4817b4363bec16cfe"
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

