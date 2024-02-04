require recipes-bsp/trusted-firmware-a/trusted-firmware-a.inc

BRANCH = "1.2.1"
SRCREV = "6374c56d77e922045224aca9f853957bb03a8bbc"
SRC_URI := "git://git@github.com/hailo-ai/arm-trusted-firmware.git;protocol=https;branch=${BRANCH}"

LIC_FILES_CHKSUM += "file://docs/license.rst;md5=b2c740efedc159745b9b31f88ff03dde"
LICENSE = "BSD-3-Clause"

COMPATIBLE_MACHINE:hailo15 = ".*"
TFA_PLATFORM:hailo15 = "hailo15"
TFA_BUILD_TARGET = "bl31"
