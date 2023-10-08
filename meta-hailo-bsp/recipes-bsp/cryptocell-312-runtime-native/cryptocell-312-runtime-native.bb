SRC_URI = "git://github.com/ARM-software/cryptocell-312-runtime.git;protocol=https;branch=update-cc110-bu-00000-r1p4"
SRCREV = "91539d62a67662e40e7d925694e55bbc7e679f84"
LIC_FILES_CHKSUM += "file://BSD-3-Clause.txt;md5=d2debfe1305a4e8cd5673d2b1f5e86ba"
LICENSE = "BSD-3-Clause"

DEPENDS = "openssl-native"
RDEPENDS:${PN} = "python3-native"

S = "${WORKDIR}/git"

inherit native

CFLAGS[unexport] = "1"
LDFLAGS[unexport] = "1"
AS[unexport] = "1"
LD[unexport] = "1"
CP_ARGS="-Prf --preserve=mode,timestamps --no-preserve=ownership"

do_compile () {
    oe_runmake -C ${S}/utils/src/ OPENSSL_INC_DIR=${STAGING_INCDIR_NATIVE} OPENSSL_LIB_DIR=${STAGING_LIBDIR_NATIVE}
}

do_install () {
    install -d ${D}${bindir}
    install -d ${D}${libdir}
    install -d ${D}${sysconfdir}
    cp ${CP_ARGS} ${S}/utils/bin/. ${D}${bindir}
    cp ${CP_ARGS} ${S}/utils/lib/. ${D}${libdir}
    cp ${CP_ARGS} ${S}/utils/src/proj.cfg ${D}${sysconfdir}/cc_proj.cfg
    sed -i 's|^#!/usr/local/bin/python3|#!/usr/bin/env python3|' ${D}${bindir}/*.py
}
