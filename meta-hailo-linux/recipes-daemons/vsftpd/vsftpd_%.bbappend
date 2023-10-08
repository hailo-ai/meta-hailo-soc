FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://0001-re-added-ssl-patch.patch"

LDFLAGS:append = " -I/usr/include/openssl/ -lcrypto -lssl"
