SUMMARY = "Hailo Throttling Library"
DESCRIPTION = "Library for handling throttling states and thermal events."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"
RPROVIDES_${PN} += "libhailo-throttling"

PV = "1.7.0"

inherit cmake

SRC_URI = "file://libhailo-throttling"

S = "${WORKDIR}/libhailo-throttling"

FILESEXTRAPATHS:prepend := "${THISDIR}/files/:"

DEPENDS = "glibc hailo-thermal-engine"

# Default log level (can be overridden):
#   Possible values:
#   - STDOUT   0
#   - STDERR   1
#   - SYSLOG   2
THROTTLING_LOG_OUTPUT ?= "0"

# Default log level (can be overridden):
#   Possible values:
#   - LOG_EMERG   0
#   - LOG_ALERT   1
#   - LOG_CRIT    2
#   - LOG_ERR     3 
#   - LOG_WARNING 4
#   - LOG_NOTICE  5
#   - LOG_INFO    6
#   - LOG_DEBUG   7
THROTTLING_LOG_LEVEL ?= "6"

# Default log control is disabled (can be overridden).
THROTTLING_LOG_CTRL ?= "0"

EXTRA_OECMAKE += "-DTHROTTLING_LOG_CTRL=${THROTTLING_LOG_CTRL} \
                  -DTHROTTLING_LOG_OUTPUT=${THROTTLING_LOG_OUTPUT} \
                  -DTHROTTLING_LOG_LEVEL=${THROTTLING_LOG_LEVEL} \
                  -DLIBRARY_VERSION=${PV}"

do_install() {
    install -d ${D}${libdir}
    install -d ${D}${includedir}

    install -m 0755 ${B}/libhailo-throttling.so.${PV} ${D}${libdir}/
    ln -sf libhailo-throttling.so.${PV} ${D}${libdir}/libhailo-throttling.so
    ln -sf libhailo-throttling.so.${PV} ${D}${libdir}/libhailo-throttling.so.1
    install -m 0644 ${S}/*.h ${D}${includedir}/
}

FILES_${PN} = "${libdir}/libhailo-throttling.so*"
FILES_${PN}-dev = "${includedir}/*.h ${libdir}/libhailo-throttling.so"

PACKAGE_ARCH = "${MACHINE_ARCH}"
