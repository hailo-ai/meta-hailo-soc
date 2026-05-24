DESCRIPTION = "Media Library package recipe \
               compiles hailo media library and copies shared objects to usr/lib/ "

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.12.0-dv-5"
SRCREV = "e1a73c77113a69a13dfdf191bc91138095e32abd"

inherit media-library-base

S = "${WORKDIR}/git"

MESON_SOURCEPATH = "${S}/hailo-media-library"

# Perfetto tracing: off by default, enable for dev images via PACKAGECONFIG:append:pn-libmedialib = " perfetto"
PACKAGECONFIG ??= ""
PACKAGECONFIG[perfetto] = "-Dperfetto=true,-Dperfetto=false,libperfetto,libperfetto"

DEPENDS:append = " \
    cli11 \
    expected \
    fast-cpp-csv-parser \
    gcc-sanitizers \
    glib-2.0 \
    glib-2.0-native \
    grpc \
    grpc-native \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    hailo-camera-configurations \
    imaging-sub-system \
    json-schema-validator \
    libgsthailo \
    libhailodsp \
    libhailo-throttling \
    libhailort \
    libiio \
    protobuf \
    protobuf-native \
    rapidjson \
    video-encoder \
    "
# Hailo-15 Runtime-Dependencies
RDEPENDS:${PN} += " medialib-configs imaging-sub-system protobuf grpc libhailo-throttling libasan media-library-service"
RDEPENDS:${PN}-lib += " libhailo-throttling"

FILES:${PN} += "${libdir}/gstreamer-1.0/libgstmedialib.so* ${libdir}/gstreamer-1.0/libgstmedialib_api.so*"

do_install:append() {
    export DESTDIR="${D}"
    ninja -C ${B} install
}

