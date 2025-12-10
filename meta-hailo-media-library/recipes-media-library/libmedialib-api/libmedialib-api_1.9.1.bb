DESCRIPTION = "Media Library Encoder OSD API \
               compiles the medialibrary encoder osd API \
               and copies it to usr/lib/ "

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=031eb3f48c82f13ff6cdb783af612501"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.9.1"
SRCREV = "f6bb0fa252bb2958e69fc5a17328707502a315de"

inherit media-library-base

MEDIA_LIBRARY_BUILD_TARGET = "api"

# Gstreamer Dependencies
DEPENDS:append = " glib-2.0-native glib-2.0 gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good json-schema-validator"
# Hailo-15 Dependencies
DEPENDS:append = " libgstmedialib"

do_install:append() {
    install -d ${D}/${bindir}
    # top-level JSONs
    install -m 0644 ${S}/api/examples/*.json ${D}/${bindir}

    #profile dir install
    install -d ${D}/${bindir}/profile/
    install -m 0644 ${S}/api/examples/profile/* ${D}/${bindir}/profile/

    #profile_hdr dir install
    install -d ${D}/${bindir}/profile_hdr/
    install -m 0644 ${S}/api/examples/profile_hdr/* ${D}/${bindir}/profile_hdr/

    #profile_hdr_jpeg dir install
    install -d ${D}/${bindir}/profile_hdr_jpeg/
    install -m 0644 ${S}/api/examples/profile_hdr_jpeg/* ${D}/${bindir}/profile_hdr_jpeg/

    #profile_jpeg dir install
    install -d ${D}/${bindir}/profile_jpeg/
    install -m 0644 ${S}/api/examples/profile_jpeg/* ${D}/${bindir}/profile_jpeg/

    #lowlight_r0225c8_jpeg dir install
    install -d ${D}/${bindir}/lowlight_r0225c8_jpeg/
    install -m 0644 ${S}/api/examples/lowlight_r0225c8_jpeg/* ${D}/${bindir}/lowlight_r0225c8_jpeg/

    #lowlight_r0225c8 dir install
    install -d ${D}/${bindir}/lowlight_r0225c8/
    install -m 0644 ${S}/api/examples/lowlight_r0225c8/* ${D}/${bindir}/lowlight_r0225c8/
}


FILES:${PN} += "${libdir}/libhailo_media_library_api.so ${bindir}/frontend_example ${bindir}/*.json  ${incdir}/medialibrary/*.hpp"
FILES:${PN}-lib += "${libdir}/libhailo_media_library_api.so"
RDEPENDS:${PN}-staticdev = ""
RDEPENDS:${PN}-dev = ""
RDEPENDS:${PN}-dbg = ""
