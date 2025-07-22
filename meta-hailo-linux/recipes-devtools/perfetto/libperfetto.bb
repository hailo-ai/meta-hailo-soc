LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=65fc11c16d093b463bafae828ec00d41"

require perfetto.inc

inherit meson

SRC_URI:append = " file://0001-meson-add-pc-file-for-lib_perfetto.patch"
SRC_URI:append = " file://0002-Add-hailo_perfetto-to-meson.build.patch"

LDFLAGS += "-Wl,--as-needed -latomic -Wl,--no-as-needed"

FILES:${PN} += "${datadir}"

BBCLASSEXTEND = "native nativesdk"
