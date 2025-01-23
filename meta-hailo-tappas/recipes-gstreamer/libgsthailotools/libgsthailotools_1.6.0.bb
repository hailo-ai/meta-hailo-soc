DESCRIPTION = "gsthailotools GStreamer plugin \
               compiles the tappas libgsthailotools gstreamer plugin \ 
               and copies it to usr/lib/gstreamer-1.0 (gstreamer's plugins directory) "

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM += "file://../../LICENSE;md5=4fbd65380cdd255951079008b364516c"

SRC_URI = "git://git@github.com/hailo-ai/hailo-camera-app-suite.git;protocol=https;branch=missing-webserver"
SRCREV = "dfb1eceda7341b0f5abde4ae798d1cdc672fea30"

inherit hailotools-base

do_install:append() {
    rm -f ${D}/${libdir}/gstreamer-1.0/libgsthailotools.so
    find ${D}/${libdir}/gstreamer-1.0/ -name 'libgsthailotools.so.[0-9]' -delete
    mv -f ${D}/${libdir}/gstreamer-1.0/libgsthailotools.so.${PV} ${D}/${libdir}/gstreamer-1.0/libgsthailotools.so
}


DEPENDS += "glib-2.0-native glib-2.0 gstreamer1.0 gstreamer1.0-plugins-base rapidjson cppzmq zeromq"
EXTRA_OEMESON += " \
    -Dlibrapidjson='${STAGING_INCDIR}/rapidjson' \
    "

# libgsthailotools requires opencv, xtensor, xtl, and libgsthailo to compile and run
TAPPAS_BUILD_TARGET = "plugins"
GST_IMAGES_UTIL = "libhailo_gst_image.so"

FILES:${PN} += "${libdir}/gstreamer-1.0/libgsthailotools.so  ${libdir}/libgsthailometa.so.${PV} ${libdir}/libhailo_tracker.so.${PV} ${incdir}/tappas/* /usr/lib/${GST_IMAGES_UTIL}.${PV}"
FILES:${PN}-lib += "${libdir}/libgsthailometa.so.${PV} ${libdir}/libhailo_tracker.so.${PV} ${libdir}/gstreamer-1.0/libgsthailotools.so /usr/lib/${GST_IMAGES_UTIL}.${PV}"
RDEPENDS:${PN}-staticdev = ""
RDEPENDS:${PN}-dev = ""
RDEPENDS:${PN}-dbg = ""

DEPENDS:append:hailo15 = " libgstmedialib "
