DESCRIPTION = "Media Library package recipe \
               compiles medialibrary vision_pre_proc shared object and copies it to usr/lib/ "

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=031eb3f48c82f13ff6cdb783af612501"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.4.2"
SRCREV = "f05a661341aba0c39fe015cf5f25e5e4d88f79ab"

inherit media-library-base

MEDIA_LIBRARY_BUILD_TARGET = "core"

DEPENDS:append = " gstreamer1.0-plugins-good rapidjson json-schema-validator expected httplib cli11 fast-cpp-csv-parser libiio"

# Hailo-15 Dependencies
DEPENDS:append = " video-encoder libhailodsp libhailort"
# Hailo-15 Runtime-Dependencies
RDEPENDS:${PN} += " medialib-configs"

do_install:append() {
    install -d ${D}/${sysconfdir}/medialib
    install -m 0644 ${S}/media_library/src/hailo_encoder/encoder_presets.csv ${D}/${sysconfdir}/medialib
}

FILES:${PN} += "${libdir}/libdis_library.so ${libdir}/libhailo_media_library_common.so ${libdir}/libhailo_media_library_frontend.so ${libdir}/libhailo_media_library_encoder.so ${libdir}/libhailo_encoder.so ${incdir}/medialibrary/*.hpp ${sysconfdir}/medialib/encoder_presets.csv"
FILES:${PN}-lib += "${libdir}/libdis_library.so ${libdir}/libhailo_media_library_common.so ${libdir}/libhailo_media_library_frontend.so ${libdir}/libhailo_media_library_encoder.so ${libdir}/libhailo_encoder.so"
