FILESEXTRAPATHS:prepend:hailo15 := "${THISDIR}/files/:"

SRC_URI:append:hailo15 = "file://hailo_v4l2_meta.patch;striplevel=3;md5=65571fb36e14f661705e3eb019f6f797 \
    file://gstvideo4linux2.pc;mdplevel=0;subdir=${S};md5=9e0e459563800f12be315c68e22e1644"


do_install:append(){
    install -d ${D}${includedir}/
    install -d ${D}${includedir}/hailo_v4l2/
    install -d ${D}${libdir}/pkgconfig

    install -m 0644 ${S}/sys/v4l2/hailo_v4l2/*.h ${D}${includedir}/hailo_v4l2/
    install -m 0644 ${S}/gstvideo4linux2.pc ${D}${libdir}/pkgconfig/

    install -d ${D}/${libdir}
    ln -s -r ${D}/${libdir}/gstreamer-1.0/libgstvideo4linux2.so ${D}/${libdir}/libgstvideo4linux2.so
}

# https://docs.yoctoproject.org/dev-manual/prebuilt-libraries.html#example
FILES_SOLIBSDEV=""
INSANE_SKIP:gstreamer1.0-plugins-good = "dev-so"

FILES:${PN} += "${libdir}/libgstvideo4linux2.so"
FILES:${PN}-dev += "${includedir}/hailo_v4l2 ${includedir}/hailo_v4l2/*"
