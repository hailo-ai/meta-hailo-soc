SUMMARY = "Verisilicon vivante SW package user space code build"
LICENSE = "MIT & Proprietary-VSI"
LIC_FILES_CHKSUM = "file://${B}/LICENSE;md5=805d1be5d56ae9500316a754de03ab5f \
					file://${S}/LICENSE;md5=8349eaff29531f0a3c4f4c8b31185958"
inherit externalsrc ccache qmake5_paths

RDEPENDS:${PN} += " qtmultimedia"
DEPENDS += "qtbase-native ninja-native libdrm bash cmake-native qwt-qt5 qtbase qtdeclarative qtmultimedia qmllive boost"

SRC_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/Hailo15/1.2.1/imaging-sub-system.tar.gz"
SRC_URI[sha256sum] = "9be62aabf4c2456f20edd75248d8e433d11e92f16d3768208c6aab4cefa13041"

B = "${WORKDIR}/imaging-sub-system/build"
S = "${WORKDIR}/imaging-sub-system/scripts"

do_install() {
	install -d ${D}/lib
	install -d ${D}/etc
	install -d ${D}/etc/init.d
	install -d ${D}/etc/rc5.d
	install -d ${D}${bindir}
	cp -R --no-dereference --preserve=mode,links -v ${B}/dist/lib/* ${D}/lib
	cp -R --no-dereference --preserve=mode,links -v ${B}/dist/release/lib/* ${D}/lib

	install -m 0755 -D  ${B}/dist/bin/tuning-server ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/tuning-lite ${D}${bindir}
	install -m 0755 -D  ${B}/dist/release/bin/isp_media_server ${D}${bindir}

	install -m 0755 -D  ${S}/hailo_cfg/isp_media_server ${D}/etc/init.d
	ln -s -r ${D}/etc/init.d/isp_media_server ${D}/etc/rc5.d/S20isp_media_server

	install -m 0755 -D  ${B}/dist/bin/tuning-yuv-capture ${D}${bindir}

	install -m 0644 -D  ${B}/dist/bin/sony_imx334.xml ${D}${bindir}
	install -m 0644 -D  ${B}/dist/bin/HAILO_IMX334*.xml ${D}${bindir}
	install -m 0644 -D  ${B}/dist/bin/sony_imx678*.xml ${D}${bindir}
	install -m 0644 -D  ${B}/dist/bin/HAILO_IMX678*.xml ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/*.so ${D}${bindir}
	install -m 0755 -D  ${B}/dist/release/bin/*.json ${D}${bindir}
	install -m 0755 -D  ${B}/dist/release/bin/*.cfg ${D}${bindir}

	install -m 0755 -D  ${B}/dist/bin/raw_image_capture ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/v4l_stream_example ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/v4l_ctrl_example ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/hailo_ctrl ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/fps ${D}${bindir}

	install -m 0755 -D  ${S}/mediacontrol/server/media_server_cfg.json ${D}${bindir}

	install -d ${D}${includedir}/imaging
	install -d ${D}${includedir}/imaging/cam_device
	install -d ${D}${includedir}/imaging/ebase
	install -d ${D}${includedir}/imaging/scmi
	install -d ${D}${includedir}/imaging/bufferpool
	install -d ${D}${includedir}/imaging/json
	install -d ${D}${includedir}/imaging/common
	install -d ${D}${includedir}/imaging/hal
	install -d ${D}${includedir}/imaging/oslayer
	install -d ${D}${includedir}/imaging/fpga
	install -d ${D}${includedir}/imaging/isi
	install -d ${D}${includedir}/imaging/cameric_drv
	
	cp -R --no-dereference --preserve=mode,links -v ${B}/dist/include/* ${D}${includedir}/imaging
	install -m 0755 -D ${S}/scripts/hailo_tuning_server.sh ${D}${bindir}

	# Add sensor/configuration specific setup scripts
	install -m 0755 -D ${S}/scripts/setup_imx*.sh ${D}${bindir}
	
	#install -m 0755 -D ${S}/scripts/*  ${D}${TARGET_SBIN_DIR}/scripts

	cp ${S}/units/cam_device/include/cam_device_2dnr/* ${D}${includedir}/imaging
	cp ${S}/units/cam_device/include/cam_device_3dnr/* ${D}${includedir}/imaging
	cp ${S}/units/cam_device/include/cam_device_gc2/* ${D}${includedir}/imaging
	cp ${S}/units/cam_device/include/cam_device_demosaic2/* ${D}${includedir}/imaging
	cp ${S}/units/cam_device/include/cam_device_wdr4/* ${D}${includedir}/imaging
	cp -R ${S}/units/common/include/* ${D}${includedir}/imaging
	cp ${S}/units/isi/include/* ${D}${includedir}/imaging
	cp ${S}/units/isi/include_priv/* ${D}${includedir}/imaging
	cp ${S}/units/3av2/include/* ${D}${includedir}/imaging
	cp -R ${S}/tuning-common/include/* ${D}${includedir}/imaging
	cp -R ${S}/utils3rd/include/* ${D}${includedir}/imaging
	cp -R ${S}/vvcam/v4l2/common/* ${D}${includedir}/imaging
	cp ${S}/vvcam/common/vvsensor.h ${D}${includedir}/imaging
	cp ${S}/vvcam/common/viv_video_kevent.h ${D}${includedir}/imaging
	cp -R ${S}/units/cam_device/include/* ${D}${includedir}/imaging/cam_device
	cp ${S}/units/ebase/include/* ${D}${includedir}/imaging/ebase
	cp ${S}/units/scmi/include/* ${D}${includedir}/imaging/scmi
	cp ${S}/units/bufferpool/include/* ${D}${includedir}/imaging/bufferpool
	cp ${S}/utils3rd/3rd/jsoncpp/include/json/* ${D}${includedir}/imaging/json
	cp ${S}/appshell/common/include/* ${D}${includedir}/imaging/common
	cp ${S}/units/hal/include/* ${D}${includedir}/imaging/hal
	cp ${S}/units/oslayer/include/* ${D}${includedir}/imaging/oslayer
	cp ${S}/units/fpga/fpga/include/* ${D}${includedir}/imaging/fpga
	cp ${S}/units/isi/include/* ${D}${includedir}/imaging/isi
	cp ${S}/units/cameric_drv/include/cameric_drv_common.h ${D}${includedir}/imaging/cameric_drv

	ln -s -r ${D}/lib/libHAILO_IMX334.so ${D}${bindir}/HAILO_IMX334.drv
	ln -s -r ${D}/lib/libHAILO_IMX678.so ${D}${bindir}/HAILO_IMX678.drv
	ln -s -r ${D}/lib/libHAILO_IMX678_HDR.so ${D}${bindir}/HAILO_IMX678_HDR.drv
}

PACKAGES = "${PN} ${PN}-dev"
INSANE_SKIP:${PN}-dev =  "file-rpaths dev-so debug-files rpaths staticdev installed-vs-shipped"
INSANE_SKIP:${PN} =  "file-rpaths dev-so debug-files rpaths staticdev installed-vs-shipped"

# FIXME: why? arch? 
do_package_qa[noexec] = "1"
EXCLUDE_FROM_SHLIBS = "1"

FILES:${PN}     += "/lib/* /lib/*/*"
FILES:${PN}     += "${bindir}/*"
