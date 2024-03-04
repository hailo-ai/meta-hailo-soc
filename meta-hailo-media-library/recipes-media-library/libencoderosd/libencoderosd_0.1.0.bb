DESCRIPTION = "Media Library Encoder OSD API \
               compiles the medialibrary encoder osd API \
               and copies it to usr/lib/ "

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8349eaff29531f0a3c4f4c8b31185958"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.2.2"
SRCREV = "1a115b3af37400edbfb6136d91b0b7d7266008dd"

inherit media-library-base

MEDIA_LIBRARY_BUILD_TARGET = "api"

# Gstreamer Dependencies
DEPENDS:append = " glib-2.0-native glib-2.0 gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good json-schema-validator"
# Hailo-15 Dependencies
DEPENDS:append = " libgstmedialib"

do_install:append(){
    install -d ${D}/${bindir}
    install -m 0644 ${S}/api/examples/*.json ${D}/${bindir}
}

FILES:${PN} += "${libdir}/libencoderosd.so ${bindir}/vision_preproc_example ${bindir}/*.json  ${incdir}/medialibrary/*.hpp"
FILES:${PN}-lib += "${libdir}/libencoderosd.so"
RDEPENDS:${PN}-staticdev = ""
RDEPENDS:${PN}-dev = ""
RDEPENDS:${PN}-dbg = ""
