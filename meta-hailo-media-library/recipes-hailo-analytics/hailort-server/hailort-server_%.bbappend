do_install:append() {
    install -d ${D}${libdir}
    install -m 0644 ${WORKDIR}/genai_servers/libtokenizers_cpp.a ${D}${libdir}/
    install -m 0644 ${WORKDIR}/genai_servers/libtokenizers_c.a ${D}${libdir}/

    install -d ${D}${includedir}
    install -m 0755 ${WORKDIR}/genai_servers/include/tokenizers_c.h ${D}${includedir}/
    install -m 0755 ${WORKDIR}/genai_servers/include/tokenizers_cpp.h ${D}${includedir}/
}

FILES:${PN}-staticdev += "${libdir}/*.a ${includedir}/*.h"