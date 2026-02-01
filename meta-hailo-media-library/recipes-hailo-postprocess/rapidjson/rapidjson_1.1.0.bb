SUMMARY = "rapidjson: A fast JSON parser/generator for C++ with both SAX/DOM style API"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://license.txt;md5=ba04aa8f65de1396a7e59d1d746c2125"

SRC_URI = "git://github.com/Tencent/rapidjson.git;protocol=https;nobranch=1"


PV = "1.0+git${SRCPV}"
SRCREV = "6a905f9311f82d306da77bd963ec5aa5da07da9c"

S = "${WORKDIR}/git"

inherit cmake

# This is a header-only library, so the main package can be empty
ALLOW_EMPTY:${PN} = "1"

EXTRA_OECMAKE = " \
    -DLIB_INSTALL_DIR=${libdir} \
    -DRAPIDJSON_BUILD_DOC=OFF \
    -DRAPIDJSON_BUILD_TESTS=OFF \
    -DRAPIDJSON_BUILD_EXAMPLES=OFF \
"
BBCLASSEXTEND = "native nativesdk"

# Header files automatically go into ${PN}-dev package
# No need to override FILES:${PN}
