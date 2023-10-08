FILESEXTRAPATHS:prepend := "${THISDIR}:"

DSP_COMPILATION_MODE ??= "release"
ENABLE_XRP_FILE = "${@bb.utils.contains('DSP_COMPILATION_MODE', 'release', 'enable-xrp-release.cfg', 'enable-xrp.cfg', d)}"

SRC_URI:append = " file://cfg/${ENABLE_XRP_FILE}"
