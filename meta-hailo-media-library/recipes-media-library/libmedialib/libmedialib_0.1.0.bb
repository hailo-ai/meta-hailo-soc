DESCRIPTION = "Media Library package recipe \
               compiles medialibrary vision_pre_proc shared object and copies it to usr/lib/ "

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8349eaff29531f0a3c4f4c8b31185958"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.2.0"
SRCREV = "a4c13c389c3ebe31aa067610fb6706d9ed421f8a"

inherit media-library-base

MEDIA_LIBRARY_BUILD_TARGET = "core"

DEPENDS:append = " gstreamer1.0-plugins-good rapidjson json-schema-validator expected"

# Hailo-15 Dependencies
DEPENDS:append = " video-encoder libhailodsp"

FILES:${PN} += "${libdir}/libdis_library.so ${libdir}/libhailo_media_library_common.so ${libdir}/libhailo_media_library_frontend.so ${libdir}/libhailo_media_library_encoder.so ${libdir}/libhailo_encoder.so ${incdir}/medialibrary/*.hpp"
FILES:${PN}-lib += "${libdir}/libdis_library.so ${libdir}/libhailo_media_library_common.so ${libdir}/libhailo_media_library_frontend.so ${libdir}/libhailo_media_library_encoder.so ${libdir}/libhailo_encoder.so"
