SRC_URI = "git://github.com/ARM-software/cryptocell-312-runtime.git;protocol=https;branch=update-cc110-bu-00000-r1p4"
SRCREV = "91539d62a67662e40e7d925694e55bbc7e679f84"
LIC_FILES_CHKSUM += "file://BSD-3-Clause.txt;md5=d2debfe1305a4e8cd5673d2b1f5e86ba"
LICENSE = "BSD-3-Clause"

DEPENDS = "openssl-native chrpath-native"
RDEPENDS:${PN} = "python3-native"
RDEPENDS:${PN} = "openssl-native"

S = "${WORKDIR}/git"

inherit native

CFLAGS[unexport] = "1"
LDFLAGS[unexport] = "1"
AS[unexport] = "1"
LD[unexport] = "1"
CP_ARGS="-Prf --preserve=mode,timestamps --no-preserve=ownership"

# The utils Makefiles are not parallel-safe: two targets mkdir the same bin directory.
PARALLEL_MAKE = ""

do_compile () {
    oe_runmake -C ${S}/utils/src/ OPENSSL_INC_DIR=${STAGING_INCDIR_NATIVE} OPENSSL_LIB_DIR=${STAGING_LIBDIR_NATIVE}
}

do_install () {
    install -d ${D}${sysconfdir}/cc312
    cp ${CP_ARGS} ${S}/. ${D}${sysconfdir}/cc312
    sed -i 's|^#!/usr/local/bin/python3|#!/usr/bin/env python3|' ${D}${sysconfdir}/cc312/utils/bin/*.py
    # Fix the rpath of the cryptocell libraries so they are relative instead of absolute so they can find the openssl libraries
    # after they are installed in sysroot of other recipes
    chrpath -r '$ORIGIN/'$(realpath --canonicalize-missing --relative-to=${D}${sysconfdir}/cc312/utils/lib/ ${D}${libdir}) ${D}${sysconfdir}/cc312/utils/lib/*
}
