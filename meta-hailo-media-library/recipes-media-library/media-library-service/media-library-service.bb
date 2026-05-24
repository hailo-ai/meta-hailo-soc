SUMMARY = "Hailo Media Library gRPC Service"
DESCRIPTION = "SysVinit init script for the Media Library gRPC server (media_library_service)"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://media-library-service.sh \
           file://media-library-service.config.default \
          "

S = "${WORKDIR}"

RDEPENDS:${PN} = "libmedialib"

inherit update-rc.d

INITSCRIPT_NAME = "media-library-service.sh"
INITSCRIPT_PARAMS = "stop 99 0 1 6 ."

do_install() {
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${S}/media-library-service.sh ${D}${sysconfdir}/init.d/

    install -d ${D}${sysconfdir}/default
    install -m 0644 ${S}/media-library-service.config.default ${D}${sysconfdir}/default/media-library-service.config
    install -m 0644 ${S}/media-library-service.config.default ${D}${sysconfdir}/default/media-library-service.config.default
}

FILES:${PN} = "${sysconfdir}/init.d/media-library-service.sh"
FILES:${PN} += "${sysconfdir}/default/media-library-service.config.default"
FILES:${PN} += "${sysconfdir}/default/media-library-service.config"
