DESCRIPTION = "Hailo SCU. \
               This recipe will download the SCU firmware binary from AWS and add it to deploy"

inherit deploy

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

BASE_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/1.3.0-dev/scu-fw"
FW = "hailo15_scu_fw.bin"
LICENSE_FILE = "LICENSE"
SRC_URI = "${BASE_URI}/${FW};name=fw \
           ${BASE_URI}/${LICENSE_FILE};name=lic"

SRC_URI[fw.sha256sum] = "58c15e04b1bd343e7c6c686f799b42eed7882add8304a001a9a9110ee4f12336"
SRC_URI[lic.sha256sum] = "ca96445e6e33ae0a82170ea847b0925c864492f0cbb6342d42c54fd647133608"

do_deploy() {
  install -m 644 -D ${WORKDIR}/${FW} ${DEPLOYDIR}/${FW}
}

addtask deploy after do_compile
