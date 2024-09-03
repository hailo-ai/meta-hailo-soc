DESCRIPTION = "Hailo Recovery FW. \
               This recipe will download the Recovery firmware binary from AWS and add it to deploy"

inherit deploy

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

BASE_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/1.4.2/recovery-fw"
FW = "hailo15_uart_recovery_fw.bin"
LICENSE_FILE = "LICENSE"
SRC_URI = "${BASE_URI}/${FW};name=fw \
           ${BASE_URI}/${LICENSE_FILE};name=lic"

SRC_URI[fw.sha256sum] = "f0a69fdf2a9b8da4dd1641de62828a94288613349925282298df740fbc35b142"
SRC_URI[lic.sha256sum] = "ca96445e6e33ae0a82170ea847b0925c864492f0cbb6342d42c54fd647133608"

do_deploy() {
  install -m 644 -D ${WORKDIR}/${FW} ${DEPLOYDIR}/${FW}
}

# Allows a creation of a package without files. If files are added, this attribute should be removed.
ALLOW_EMPTY:${PN} = "1"

addtask deploy after do_compile
