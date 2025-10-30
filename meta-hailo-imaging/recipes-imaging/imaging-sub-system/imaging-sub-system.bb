SUMMARY = "Verisilicon vivante SW package user space code build with basic components installed"

require imaging-sub-system.inc
inherit ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}

SYSTEMD_SERVICE:${PN} = "isp_media_server.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"


LIBS_FILES_TO_COPY = "${@bb.utils.contains('HAILO_TARGET', 'core-image-hailo', '.so*', '', d)}"
copy_lib_files() {
	cp -R --no-dereference --preserve=mode,links -v ${B}/dist/lib/*${LIBS_FILES_TO_COPY} ${D}/lib
	cp -R --no-dereference --preserve=mode,links -v ${B}/dist/${BUILD_TYPE}/lib/*${LIBS_FILES_TO_COPY} ${D}/lib
}

install_isp_media_server() {
	install -m 0755 -D  ${B}/dist/${BUILD_TYPE}/bin/isp_media_server ${D}${bindir}
	# Install systemd files only if systemd is enabled
	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		install -d ${D}${systemd_unitdir}/system
		install -m 0644 ${S}/hailo_cfg/isp_media_server.service ${D}${systemd_unitdir}/system/
	else
		install -m 0755 -D  ${S}/hailo_cfg/isp_media_server ${D}/etc/init.d
		ln -s -r ${D}/etc/init.d/isp_media_server ${D}/etc/rc5.d/S20isp_media_server
	fi
}

install_dist() {
	install -m 0755 -D  ${B}/dist/bin/*.so ${D}${bindir}
	install -m 0755 -D  ${B}/dist/${BUILD_TYPE}/bin/*.json ${D}${bindir}
	install -m 0755 -D  ${B}/dist/${BUILD_TYPE}/bin/*.cfg ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/raw_image_capture ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/hailo_ctrl ${D}${bindir}
	install -m 0755 -D  ${S}/units/hailo/hdr_lib/hefs/hdr*.hef ${D}${bindir}
}

install_misc() {
	install -m 0755 -D  ${S}/mediacontrol/server/json_files/media_server_cfg*.json ${D}${bindir}

	install -d ${D}${includedir}/imaging
	cp ${S}/units/hailo/hdr_lib/src/*.hpp ${D}${includedir}/imaging
	cp ${S}/units/3av2/include/aaa_config/* ${D}${includedir}/imaging
}

do_install() {
	install -d ${D}/lib
	install -d ${D}/etc
	install -d ${D}/etc/init.d
	install -d ${D}/etc/rc5.d
	install -d ${D}${bindir}

	copy_lib_files
	install_isp_media_server
	install_dist
	install_misc
}