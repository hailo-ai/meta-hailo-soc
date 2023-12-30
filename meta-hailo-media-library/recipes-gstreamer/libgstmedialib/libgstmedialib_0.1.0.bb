DESCRIPTION = "Media Library GStreamer plugin \
               compiles the medialibrary gstreamer plugin \
               and copies it to usr/lib/gstreamer-1.0 (gstreamer's plugins directory) "

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8349eaff29531f0a3c4f4c8b31185958"

S = "${S}/hailo-media-library"
SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.2.0"
SRCREV = "a4c13c389c3ebe31aa067610fb6706d9ed421f8a"

inherit media-library-base

MEDIA_LIBRARY_BUILD_TARGET = "gst"

do_install:append() {
    rm -f ${D}/${libdir}/gstreamer-1.0/libgstmedialib.so
    find ${D}/${libdir}/gstreamer-1.0/ -name 'libgstmedialib.so.[0-9]' -delete
    mv -f ${D}/${libdir}/gstreamer-1.0/libgstmedialib.so.${PV} ${D}/${libdir}/gstreamer-1.0/libgstmedialib.so
}

# Gstreamer Dependencies
DEPENDS:append = " glib-2.0-native glib-2.0 gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good"
# Hailo-15 Dependencies
DEPENDS:append = " libhailodsp libmedialib"

PACKAGECONFIG:append:pn-opencv = "freetype "

FILES:${PN} += "${libdir}/gstreamer-1.0/libgstmedialib.so"
FILES:${PN}-lib += "${libdir}/gstreamer-1.0/libgstmedialib.so"
RDEPENDS:${PN}-staticdev = ""
RDEPENDS:${PN}-dev = ""
RDEPENDS:${PN}-dbg = ""
