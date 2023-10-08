FILESEXTRAPATHS:prepend:hailo15 := "${THISDIR}/files/:"

SRC_URI:append:hailo15 = "file://get_vsm_from_v4l_buffer.patch;striplevel=3;md5=1939fc95b44a4e82f97aad4a1c60d73f"

do_install:append(){
    install -d ${D}${includedir}/
    install -d ${D}${includedir}/v4l2_vsm/

    install -m 0644 ${S}/sys/v4l2/hailo_vsm/*.h ${D}${includedir}/v4l2_vsm/
}

FILES:${PN}-dev += "${includedir}/v4l2_vsm ${includedir}/v4l2_vsm/*"