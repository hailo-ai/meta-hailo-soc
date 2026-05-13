SUMMARY = "Xtl: Basic tools (containers, algorithms) used by other quantstack packages"
HOMEPAGE = "https://github.com/xtensor-stack/xtl"

SRCREV_xtl = "46f8a9390db2c52aaf41de8f93ed0dab97af012d"
SRC_URI = "git://github.com/xtensor-stack/xtl.git;name=xtl;protocol=https"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c12cbcb0f50cce3b0c58db4e3db8c2da"

S = "${WORKDIR}/git"

inherit cmake

# This is a header-only library, so the main package can be empty
ALLOW_EMPTY:${PN} = "1"

DEPENDS:append = " \
    nlohmann-json \
    "

EXTRA_OECMAKE = " \
    -DBUILD_TESTS=OFF \
"

BBCLASSEXTEND = "native nativesdk"

# Header files automatically go into ${PN}-dev package
# No need to override FILES:${PN}