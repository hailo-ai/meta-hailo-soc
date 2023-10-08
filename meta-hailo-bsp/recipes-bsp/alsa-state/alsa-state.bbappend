DESCRIPTION = "Append Hailo15 default ALSA configuration"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://hailo15_i2s_master_asound.conf"
do_install:append() {
    cat ${WORKDIR}/hailo15_i2s_master_asound.conf >> ${D}/etc/asound.conf
}
FILES:${PN} += "/etc/asound.conf"

