DESCRIPTION = "Crypto assets for Hailo secure boot"
LICENSE = "Proprietary"
DEPENDS = "openssl-native"
DEPENDS += "cryptocell-312-runtime-native"
DEPENDS += "hailo-secureboot-scripts-native"
DEPENDS += "gettext-native"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

inherit deploy

CUSTOMER_CERT ?= "development_customer_cert_chain.bin"
CUSTOMER_KEY ?= "development_customer_keypair.pem"

CUSTOMER_ROOT_KEY ?= "development_customer_root_keypair.pem"

CUSTOMER_SIGNED_CERT ?= "customer_signed_certificate.bin"

KEY_CERTIFICATE ?= "key_certificate.bin"

BASE_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/SecureBoot"
SRC_URI = "${BASE_URI}/${CUSTOMER_CERT};name=cert \
           ${BASE_URI}/${CUSTOMER_KEY};name=key \
           ${BASE_URI}/${CUSTOMER_ROOT_KEY};name=root_key \
           ${BASE_URI}/LICENSE;name=lic"

SRC_URI[cert.sha256sum] = "4e878fb261dbdb46e9f31a9c195b1171916fcaa79b09ec394fee302135d2ac70"
SRC_URI[key.sha256sum] = "1a3c0142934da9164ec599a5677104cd1a1df2ece1a3f4bb2448a9ccf08cd351"
SRC_URI[root_key.sha256sum] = "f4faf9d8c88bc50ad443a016f68705a42e6c94055257f8cc8dd894e60dc0cec0"
SRC_URI[lic.sha256sum] = "ca96445e6e33ae0a82170ea847b0925c864492f0cbb6342d42c54fd647133608"

CERT_KEYPAIR ?= "${WORKDIR}/${CUSTOMER_KEY}"
CC312_DIR ?= "${STAGING_ETCDIR_NATIVE}/cc312"

do_sign() {
  if [ -n "${HAS_CUSTOMER_ROOT_KEY}" ]; then
    openssl rsa -in ${CERT_KEYPAIR} -pubout -out ${WORKDIR}/customer.pubkey
    hailo15_customer_key_sign.sh ${CC312_DIR} ${WORKDIR}/${CUSTOMER_ROOT_KEY} ${WORKDIR}/customer.pubkey ${WORKDIR}/${KEY_CERTIFICATE}
    hailo15_customer_certificate_generate.sh ${CC312_DIR} ${WORKDIR}/${KEY_CERTIFICATE} ${CERT_KEYPAIR} ${WORKDIR}/${CUSTOMER_SIGNED_CERT}
  fi
}

addtask sign after do_compile

do_deploy() {
  install -m 644 -D ${WORKDIR}/${CUSTOMER_KEY} ${DEPLOYDIR}/customer.key
  if [ -n "${HAS_CUSTOMER_ROOT_KEY}" ]; then
    install -m 644 -D ${WORKDIR}/${CUSTOMER_ROOT_KEY} ${DEPLOYDIR}/customer_root.key
    install -m 644 -D ${WORKDIR}/${CUSTOMER_SIGNED_CERT} ${DEPLOYDIR}/${CUSTOMER_SIGNED_CERT}
    ln -s -r ${DEPLOYDIR}/${CUSTOMER_SIGNED_CERT} ${DEPLOYDIR}/customer_certificate.bin
    install -m 644 -D ${WORKDIR}/${KEY_CERTIFICATE} ${DEPLOYDIR}/${KEY_CERTIFICATE}
  else
    install -m 644 -D ${WORKDIR}/${CUSTOMER_CERT} ${DEPLOYDIR}/customer_certificate.bin
  fi

  if [ "${SECURE_BOOT_MODE}" != "accelerator" ]; then
    openssl req -batch -new -x509 -key ${DEPLOYDIR}/customer.key -out ${DEPLOYDIR}/customer.crt
  fi
}

addtask deploy after do_sign
