DESCRIPTION = "Hailo SCU. \
               This recipe will download the SCU firmware binary from AWS and add it to deploy"

inherit deploy

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

BASE_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/1.2.1/scu-fw"
FW = "hailo15_scu_fw.bin"
LICENSE_FILE = "LICENSE"
SRC_URI = "${BASE_URI}/${FW};name=fw \
           ${BASE_URI}/${LICENSE_FILE};name=lic"

SRC_URI[fw.sha256sum] = "fadcffc09f8b2692315a24162e72905e931211c984879509668084bc572353a0"
SRC_URI[lic.sha256sum] = "ca96445e6e33ae0a82170ea847b0925c864492f0cbb6342d42c54fd647133608"

do_deploy() {
  install -m 644 -D ${WORKDIR}/${FW} ${DEPLOYDIR}/${FW}
}

addtask deploy after do_compile
