SUMMARY = "Xtensor : C++ library meant for numerical analysis with multi-dimensional array expressions"
HOMEPAGE = "https://github.com/xtensor-stack/xtensor"

SRCREV_xtensor = "825c0fd8a465049c06ad89fa3911b342dbffcabf"
SRC_URI = "git://github.com/xtensor-stack/xtensor.git;name=xtensor;protocol=https;branch=master"

LICENSE = "LICENSE"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5c67ec4d3eb9c5b7eed4c37e69571b93"

S = "${WORKDIR}/git"

inherit cmake

# This is a header-only library, so the main package can be empty
ALLOW_EMPTY:${PN} = "1"

# Runtime dependency should be on the -dev package
DEPENDS:append = " \
    xtl \
    nlohmann-json \
    "
RDEPENDS:${PN}-dev += "xtl-dev"

BBCLASSEXTEND = "native nativesdk"

# Header files automatically go into ${PN}-dev package
# No need to override FILES:${PN}
