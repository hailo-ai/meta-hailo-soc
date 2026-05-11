DESCRIPTION = "Media Library package recipe \
               compiles hailo media library and copies shared objects to usr/lib/ "

LICENSE = "MIT"
MD5SUM = "4f9220a5c4c232aa3971ad6ef826474a"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=${MD5SUM}"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=0.0.0.LGL-dv-LGL_20"
SRCREV = "386c9971ed14693e0fd237f4a5d4525e688d4835"

inherit media-library-base

S = "${WORKDIR}/git/hailo-media-library"

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
RDEPENDS:${PN} += " medialib-configs imaging-sub-system protobuf grpc libhailo-throttling libasan"
RDEPENDS:${PN}-lib += " libhailo-throttling"

FILES:${PN} += "${libdir}/gstreamer-1.0/libgstmedialib.so* ${libdir}/gstreamer-1.0/libgstmedialib_api.so*"

do_install:append() {
    export DESTDIR="${D}"
    ninja -C ${B} install
}

