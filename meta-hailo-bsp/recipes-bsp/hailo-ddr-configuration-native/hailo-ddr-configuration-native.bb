DESCRIPTION = "Generate DDR regconfig for Hailo SoC"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8349eaff29531f0a3c4f4c8b31185958"

DEPENDS = "openssl-native"

inherit deploy native

# set this variable to your board's regconfig file (generated by hailo tools)
# this file is added to the SRC_URI, so it has to be located in the FILESPATH or FILESEXTRAPATHS
HAILO_DDR_REGCONFIG_FILE ?= "hailo15_evb_MT53E1G32D2FW-046.h"

# ECC mode.
# Possible values:
#   "disabled"
#   "enabled": ECC enabled, detection disabled, correction disabled
#   "detection": ECC enabled, detection enabled, correction disabled
#   "correction": ECC enabled, detection enabled, correction enabled
HAILO_DDR_ECC_MODE ?= "${@bb.utils.contains('MACHINE_FEATURES', 'ddr_ecc_en', 'correction', 'disabled', d)}"

# BIST mode.
# controls enabling or disable the BIST procedure that run at every boot
# Possible values:
#   "enabled"
#   "disabled"
HAILO_DDR_BIST_ENABLE ?= "enabled"

# Operational frequency.
# controls the operational frequency of the DDR after training.
# Possible values:
#   "f0" (50Mhz, xtal mode)
#   "f1"
#   "f2"
HAILO_DDR_OPERATIONAL_FREQUENCY_INDEX ?= "f2"

# F1, F2 frequency Hz.
# controls the frequency of the DDR in F1 and F2.
# Note: this value is half the transfer rate (MT/s) value because it is double data-rate.
# Possible values:
# 50000000, 100000000, 1598000000, 200000000,
# 400000000, 800000000, 1200000000, 1600000000,
# 2000000000, 2130000000, 2132000000, 2133000000
HAILO_DDR_F1_FREQUENCY_HZ ?= "1600000000"
HAILO_DDR_F2_FREQUENCY_HZ ?= "2000000000"


HAILO_DDR_CONFIGURATION_FILENAME = "hailo_ddr_configuration.bin"

SRC_URI = "file://build_ddr_configuration.c \
           file://${HAILO_DDR_REGCONFIG_FILE} \
	   file://LICENSE"

S = "${WORKDIR}"

CFLAGS:append = " -Werror"
LDFLAGS:append = " -lssl -lcrypto"

do_compile () {
    ${CC} ${CFLAGS} build_ddr_configuration.c -DREGCONFIG_FILENAME=\"${HAILO_DDR_REGCONFIG_FILE}\" -o build_ddr_configuration ${LDFLAGS}
    ./build_ddr_configuration \
        ${HAILO_DDR_ECC_MODE} \
        ${HAILO_DDR_BIST_ENABLE} \
        ${HAILO_DDR_OPERATIONAL_FREQUENCY_INDEX} \
        ${HAILO_DDR_F1_FREQUENCY_HZ} \
        ${HAILO_DDR_F2_FREQUENCY_HZ} \
        ${HAILO_DDR_CONFIGURATION_FILENAME}
}

do_deploy () {
    install -m 644 -D ${B}/${HAILO_DDR_CONFIGURATION_FILENAME} ${DEPLOYDIR}/${HAILO_DDR_CONFIGURATION_FILENAME}
}

addtask deploy after do_compile
