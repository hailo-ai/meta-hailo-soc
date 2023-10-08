# This class is used for signing binary files for authentication using cryptocell-312
# by Hailo-15 SCU.
#
# Recipes using this class should declare the following variables:
# - HAILO_CC312_SIGNED_BINARY: path to the output signed binary
# - HAILO_CC312_UNSIGNED_BINARY: path to the binary to sign
#
# a file ${HAILO_CC312_UNSIGNED_BINARY}.padded will be created
#

DEPENDS += "cryptocell-312-runtime-native"
DEPENDS += "hailo-secureboot-assets"

do_hailo_cc312_sign[depends] += " hailo-secureboot-assets:do_deploy"

CERT_KEYPAIR ?= "${DEPLOY_DIR_IMAGE}/customer.key"
HAILO_CC312_PADDED_UNSIGNED_BINARY = "${HAILO_CC312_UNSIGNED_BINARY}.padded"

do_hailo_cc312_sign() {
    # pad so the signed content is aligned to 4 byte
    dd if=${HAILO_CC312_UNSIGNED_BINARY} of=${HAILO_CC312_PADDED_UNSIGNED_BINARY} ibs=4 conv=sync
    unsigned_binary_size=$(printf "0x%x" `stat -c "%s" "${HAILO_CC312_PADDED_UNSIGNED_BINARY}"`)

    # <image file name> <32b mem load addr> <32b flash store addr> <32b image max size> <encryption flag: 0  not encrypted, 1 encrypted>
    cat <<EOF > ${B}/images_table
${HAILO_CC312_PADDED_UNSIGNED_BINARY} 0xffffffff 0 ${unsigned_binary_size} 0
EOF

    cat <<EOF > ${B}/certificate_config
[CNT-CFG]
load-verify-scheme=1
crypto-type=0
aes-ce-id=0
nvcounter-val=0
cert-keypair=${CERT_KEYPAIR}
images-table=${B}/images_table
cert-pkg=${B}/certificate.bin
EOF
    cert_sb_content_util.py ${B}/certificate_config -cfg_file ${STAGING_ETCDIR_NATIVE}/cc_proj.cfg
    cat ${B}/certificate.bin ${HAILO_CC312_PADDED_UNSIGNED_BINARY} > ${HAILO_CC312_SIGNED_BINARY}
}
