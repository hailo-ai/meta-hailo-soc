FILESEXTRAPATHS:prepend:hailo15 := "${THISDIR}/files/:"

SRC_URI:append:hailo15 = "file://hailo_v4l2_meta.patch;striplevel=3;md5=b2acc69cddc5c1473fa193e5a9b57375"

do_install:append(){
    install -d ${D}${includedir}/
    install -d ${D}${includedir}/hailo_v4l2/

    install -m 0644 ${S}/sys/v4l2/hailo_v4l2/*.h ${D}${includedir}/hailo_v4l2/
}

FILES:${PN}-dev += "${includedir}/hailo_v4l2 ${includedir}/hailo_v4l2/*"