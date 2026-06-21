require recipes-bsp/trusted-firmware-a/trusted-firmware-a.inc

BRANCH = "1.12.0"
SRCREV = "098dc503ebae807e6ae6366a60625a83956ec423"
SRC_URI := "git://git@github.com/hailo-ai/arm-trusted-firmware.git;protocol=https;branch=${BRANCH}"

LIC_FILES_CHKSUM += "file://docs/license.rst;md5=b2c740efedc159745b9b31f88ff03dde"
LICENSE = "BSD-3-Clause"

COMPATIBLE_MACHINE:hailo15 = ".*"
COMPATIBLE_MACHINE:hailo15l = ".*"
COMPATIBLE_MACHINE:hailo12l = ".*"
TFA_PLATFORM:hailo15 = "hailo15"
TFA_PLATFORM:hailo15l = "hailo15l"
TFA_PLATFORM:hailo12l = "hailo12l"
TFA_BUILD_TARGET = "bl31"
# change the log level to ERROR so we don't get any prints during boot
EXTRA_OEMAKE:append:accelerator = " LOG_LEVEL=10"
EXTRA_OEMAKE:append:hailo15l-oregano = " HAILO_FPGA=1"
EXTRA_OEMAKE:append:hailo12l-maple = " HAILO_FPGA=1"
EXTRA_OEMAKE:append:hailo12l-mint = " HAILO_FPGA=1"