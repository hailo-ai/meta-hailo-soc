# This class is used for signing binary files for authentication using cryptocell-312
# by Hailo-15 SCU.
#

DEPENDS += "cryptocell-312-runtime-native"
DEPENDS += "hailo-secureboot-assets"
DEPENDS += "hailo-secureboot-scripts-native"
DEPENDS += "gettext-native"

inherit python3native

CERT_KEYPAIR ?= "${DEPLOY_DIR_IMAGE}/customer.key"
ROOT_KEYPAIR ?= "${DEPLOY_DIR_IMAGE}/customer_root.key"
KEY_CERTIFICATE ?= "${DEPLOY_DIR_IMAGE}/key_certificate.bin"
CC312_DIR ?= "${STAGING_ETCDIR_NATIVE}/cc312"

# Sign a binary file using the Hailo-15 SCU boot image signing tool
# Arguments:
# 1. unsigned_binary: The path to the binary file to sign
# 2. soc: The type of the SoC to sign - either "hailo15", "hailo15l", or "hailo10h2"
# 3. binary_type: The type of the binary file to sign - one of "image", "devicetree"
# 4. signed_binary: The path to the signed binary file
hailo15_boot_image_sign() {
    unsigned_binary=$1
    soc=$2
    binary_type=$3
    signed_binary=$4
    hailo15_boot_image_sign.sh ${CC312_DIR} ${CERT_KEYPAIR} ${unsigned_binary} ${soc} ${binary_type} ${signed_binary}
}

# Sign a firmware file for the SCU processor (SCU-FW, SCU-BL, Uart Recovery FW)
# Arguments:
# 1. unsigned_firmware: The path to the firmware file to sign
# 2. signed_firmware: The path to the signed firmware file
hailo15_scu_firmware_sign() {
    unsigned_firmware=$1
    signed_firmware=$2
    hailo15_scu_image_sign.sh ${CC312_DIR} ${KEY_CERTIFICATE} ${CERT_KEYPAIR} ${unsigned_firmware} ${signed_firmware}
}
