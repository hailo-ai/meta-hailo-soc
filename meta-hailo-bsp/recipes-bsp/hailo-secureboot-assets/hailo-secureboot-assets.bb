DESCRIPTION = "Crypto assets for Hailo secure boot"
LICENSE = "Proprietary"
DEPENDS = "openssl-native"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"
inherit deploy

CUSTOMER_CERT ?= "development_customer_cert_chain.bin"
CUSTOMER_KEY ?= "development_customer_keypair.pem"

BASE_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/SecureBoot"
SRC_URI = "${BASE_URI}/${CUSTOMER_CERT};name=cert \
           ${BASE_URI}/${CUSTOMER_KEY};name=key \ 
           ${BASE_URI}/LICENSE;name=lic"

SRC_URI[cert.sha256sum] = "4e878fb261dbdb46e9f31a9c195b1171916fcaa79b09ec394fee302135d2ac70"
SRC_URI[key.sha256sum] = "1a3c0142934da9164ec599a5677104cd1a1df2ece1a3f4bb2448a9ccf08cd351"
SRC_URI[lic.sha256sum] = "ca96445e6e33ae0a82170ea847b0925c864492f0cbb6342d42c54fd647133608"

do_deploy() {
  install -m 644 -D ${WORKDIR}/${CUSTOMER_CERT} ${DEPLOYDIR}/customer_certificate.bin
  install -m 644 -D ${WORKDIR}/${CUSTOMER_KEY} ${DEPLOYDIR}/customer.key
  openssl req -batch -new -x509 -key ${DEPLOYDIR}/customer.key -out ${DEPLOYDIR}/customer.crt
}

addtask deploy after do_compile
