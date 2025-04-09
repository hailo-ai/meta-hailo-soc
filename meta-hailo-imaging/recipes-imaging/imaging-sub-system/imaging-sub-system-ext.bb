SUMMARY = "Verisilicon vivante SW package user space code build with extra components and files installed"

require imaging-sub-system.inc

INHERITS += " qmake5_paths"
RDEPENDS_IMAGING_SUB_SYSTEM += " qtmultimedia"
DEPENDS_IMAGING_SUB_SYSTEM += " qtbase-native ninja-native bash cmake-native qwt-qt5 qtbase qtdeclarative qtmultimedia qmllive boost"

install_dist() {
	install -m 0755 -D  ${B}/dist/bin/tuning-server ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/tuning-lite ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/tuning-yuv-capture ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/v4l_stream_example ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/fe-read-reg ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/v4l_ctrl_example ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/fps ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/mcm_manager ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/hdr_manager ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/hdr_manager_example ${D}${bindir}
    install -m 0755 -D  ${B}/dist/bin/v4l_event_handling_example ${D}${bindir}
}

install_misc() {
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
	install -m 0755 -D ${S}/scripts/external/hailo_tuning_server.sh ${D}${bindir}
	install -m 0755 -D ${S}/scripts/external/hailo_tuning_server_nnhdr_fhd.sh ${D}${bindir}
	install -m 0755 -D ${S}/scripts/external/tuning_mcm_start.sh ${D}${bindir}
	install -m 0755 -D ${S}/scripts/external/capture_tool_sensor_params.py ${D}${bindir}


	# Add sensor/configuration specific setup scripts
	install -m 0755 -D ${S}/scripts/external/setup_imx*.sh ${D}${bindir}
	install -m 0755 -D ${S}/scripts/external/find_subdevice_path.sh ${D}${bindir}
	
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
}

do_install() {
	install -d ${D}${bindir}

    install_dist
	install_misc
}
