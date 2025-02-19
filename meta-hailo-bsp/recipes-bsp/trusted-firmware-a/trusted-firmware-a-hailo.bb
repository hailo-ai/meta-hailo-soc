require recipes-bsp/trusted-firmware-a/trusted-firmware-a.inc

BRANCH = "1.6.1"
SRCREV = "9c7fe6eed4ad08460da2de33f0bbcea1095babeb"
SRC_URI := "git://git@github.com/hailo-ai/arm-trusted-firmware.git;protocol=https;branch=${BRANCH}"

LIC_FILES_CHKSUM += "file://docs/license.rst;md5=b2c740efedc159745b9b31f88ff03dde"
LICENSE = "BSD-3-Clause"

COMPATIBLE_MACHINE:hailo15 = ".*"
COMPATIBLE_MACHINE:hailo15l = ".*"
COMPATIBLE_MACHINE:hailo10h2 = ".*"
TFA_PLATFORM:hailo15 = "hailo15"
TFA_PLATFORM:hailo15l = "hailo15l"
TFA_PLATFORM:hailo10h2 = "hailo10h2"
TFA_BUILD_TARGET = "bl31"
EXTRA_OEMAKE:append:hailo15l-oregano = " HAILO_FPGA=1"
EXTRA_OEMAKE:append:hailo10h2-mint = " HAILO_FPGA=1"