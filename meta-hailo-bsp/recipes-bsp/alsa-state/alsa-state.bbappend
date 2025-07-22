DESCRIPTION = "Append Hailo15 default ALSA configuration"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += " file://hailo15_i2s_master_asound.conf"
SRC_URI:remove:hailo15l-sbc = " file://hailo15_i2s_master_asound.conf"
ASOUND_STATE_FILES:append:hailo15-evb = " hailo15_evb_asound.state"
ASOUND_STATE_FILES:append:hailo15-sbc = " hailo15_sbc_asound.state"
ASOUND_STATE_FILES:append:hailo15l-sbc = " hailo15l_sbc_asound.state"

asound_state_dir = "/var/lib/alsa"

python () {
    import os
    asound_state_files = d.getVar('ASOUND_STATE_FILES')
    if not asound_state_files:
        return
    for f in asound_state_files.split():
        d.appendVar('SRC_URI', ' file://' + f)
        d.appendVar('FILES:' + d.getVar('PN'), ' ${asound_state_dir}/' + os.path.basename(f))
}

do_install:append() {
    if [ -e ${WORKDIR}/hailo15_i2s_master_asound.conf ]; then
        cat ${WORKDIR}/hailo15_i2s_master_asound.conf >> ${D}/${sysconfdir}/asound.conf
    fi
    for f in ${ASOUND_STATE_FILES}; do
        cat ${WORKDIR}/${f} >> ${D}/${asound_state_dir}/asound.state
    done
}