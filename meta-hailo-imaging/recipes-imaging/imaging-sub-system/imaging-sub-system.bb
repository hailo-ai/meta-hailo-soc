SUMMARY = "Verisilicon vivante SW package - ISP server runtime and tuning/development tools"
LICENSE = "MIT & Proprietary-VSI"
LIC_FILES_CHKSUM = "file://${B}/LICENSE;md5=805d1be5d56ae9500316a754de03ab5f \
					file://${S}/LICENSE;md5=8349eaff29531f0a3c4f4c8b31185958"

inherit externalsrc ccache
inherit ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}

DEPENDS += "libdrm v4l-utils"

IMAGING_COMPILATION_MODE ?= 'release'
BUILD_TYPE = "${@'release' if '${IMAGING_COMPILATION_MODE}' == 'release' else 'debug'}"

# Note: external_releaser.py sets the following variables for us when releasing version
# IMAGING_SRC_PATH, IMAGING_BINS_PATH, SRC_URI[bins.sha256sum]
# Variable controlling which branch/binary to fetch, being modified by external_releaser.py
IMAGING_SRC_PATH = "git://git@github.com/hailo-ai/hailo-imaging.git;protocol=https;branch=1.12.0-dv-3"
IMAGING_BINS_PATH = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/CrossProducts/1.12.0-dv-3/imaging-sub-system.tar.gz"

# We have 2 URIs: "bins" for our binaries (S3), "source" for source code (github)
SRC_URI = "${IMAGING_BINS_PATH};name=bins \
		${IMAGING_SRC_PATH};name=source"

# Hash of binaries and hash of fetched commits
SRC_URI[bins.sha256sum] = "20a63845db1a843b817c56d37b20f6ec390ec27bd163140127da73e727294598"

# Specify the commit hash of imaging repo - filled and uncommented by external_releaser
SRCREV:pn-imaging-sub-system = "b9e45854e6f1a3b42163884f5aa9b60ec3268a0d"

B = "${WORKDIR}/imaging-sub-system/build"
S = "${WORKDIR}/imaging-sub-system/scripts"
SRC_GIT="${WORKDIR}/git"

# This recipe produces two packages from one build:
#   ${PN}     — Core: ISP server runtime for production images (always installed)
#   ${PN}-ext — Extended: tuning/dev tools + full headers (non-production images only)
# ext is listed first so it claims its files before ${PN}'s catch-all glob.
PACKAGES = "${PN}-ext ${PN} ${PN}-dev"

INSANE_SKIP:${PN}     = "file-rpaths dev-so debug-files rpaths staticdev installed-vs-shipped"
INSANE_SKIP:${PN}-ext = "file-rpaths dev-so debug-files rpaths staticdev installed-vs-shipped"
INSANE_SKIP:${PN}-dev = "file-rpaths dev-so debug-files rpaths staticdev installed-vs-shipped"
do_package_qa[noexec] = "1"
EXCLUDE_FROM_SHLIBS = "1"

###############################################################################
# Core package (${PN}) — ISP server runtime for production images
###############################################################################

RDEPENDS:${PN} += "v4l-utils"
SYSTEMD_SERVICE:${PN} = "isp_media_server.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

FILES:${PN} += "/lib/* /lib/*/*"
FILES:${PN} += "${bindir}/*"
FILES:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${systemd_unitdir}/system/isp_media_server.service', '', d)}"

LIBS_FILES_TO_COPY = "${@bb.utils.contains('HAILO_TARGET', 'core-image-hailo', '.so*', '', d)}"
copy_lib_files() {
	cp -R --no-dereference --preserve=mode,links -v ${B}/dist/lib/*${LIBS_FILES_TO_COPY} ${D}/lib
	cp -R --no-dereference --preserve=mode,links -v ${B}/dist/${BUILD_TYPE}/lib/*${LIBS_FILES_TO_COPY} ${D}/lib
}

HAILO_CFG = "${S}/hailo_cfg"

install_isp_media_server() {
	install -m 0755 -D  ${B}/dist/${BUILD_TYPE}/bin/isp_media_server ${D}${bindir}
	install -m 0755 -D  ${B}/dist/${BUILD_TYPE}/bin/wdog_isp_media_server ${D}${bindir}
	install -m 0755 -D  ${B}/dist/${BUILD_TYPE}/bin/stats_query ${D}${bindir}
	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		install -d ${D}${systemd_unitdir}/system
		install -m 0644 ${HAILO_CFG}/isp_media_server.service ${D}${systemd_unitdir}/system/
	else
		install -m 0755 -D  ${HAILO_CFG}/isp_media_server ${D}/etc/init.d
		ln -s -r ${D}/etc/init.d/isp_media_server ${D}/etc/rc5.d/S20isp_media_server
	fi
	# Install ISP log config if available (release tarballs may not include these yet)
	if [ -f ${HAILO_CFG}/isp_log.cfg ]; then
		install -m 0644 -D ${HAILO_CFG}/isp_log.cfg ${D}${bindir}/isp_log.cfg
	elif [ -f ${HAILO_CFG}/isp_log.cfg.default ]; then
		install -m 0644 -D ${HAILO_CFG}/isp_log.cfg.default ${D}${bindir}/isp_log.cfg
	fi
	if [ -f ${HAILO_CFG}/reload_isp_log_cfg.sh ]; then
		install -m 0755 -D ${HAILO_CFG}/reload_isp_log_cfg.sh ${D}${bindir}/reload_isp_log_cfg.sh
	fi
}

install_dist() {
	install -m 0755 -D  ${B}/dist/bin/*.so ${D}${bindir}
	install -m 0755 -D  ${B}/dist/${BUILD_TYPE}/bin/*.json ${D}${bindir}
	install -m 0755 -D  ${B}/dist/${BUILD_TYPE}/bin/*.cfg ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/raw_image_capture ${D}${bindir}
	install -m 0755 -D  ${B}/dist/bin/hailo_ctrl ${D}${bindir}
}

install_misc() {
	install -m 0755 -D  ${S}/mediacontrol/server/json_files/media_server_cfg*.json ${D}${bindir}
	install -d ${D}${includedir}/imaging
	cp ${S}/units/hailo/hdr_lib/src/*.hpp ${D}${includedir}/imaging
	cp ${S}/units/3av2/include/aaa_config/* ${D}${includedir}/imaging
}

###############################################################################
# Extended package (${PN}-ext) — Tuning/dev tools for non-production images
#
# Installed only when DISTRO_FEATURES does NOT contain 'hailo-core'.
# To add a new tuning tool binary: add its name to IMAGING_EXT_TOOLS below.
# Both install_dist_ext() and FILES:${PN}-ext are auto-derived from that list.
###############################################################################

IMAGING_EXT_TOOLS = " \
    tuning-server \
    tuning-lite \
    tuning-yuv-capture \
    v4l_stream_example \
    fe-read-reg \
    v4l_ctrl_example \
    fps \
    mcm_manager \
    hdr_manager \
    hdr_manager_example \
    v4l_event_handling_example \
    get_pipeline_state \
    unit_tests \
"

RDEPENDS:${PN}-ext += "qtmultimedia ${PN}"

FILES:${PN}-ext = " \
    ${@ ' '.join('${bindir}/' + t for t in '${IMAGING_EXT_TOOLS}'.split()) } \
    ${bindir}/hailo_tuning_server.sh \
    ${bindir}/hailo_tuning_server_nnhdr_fhd.sh \
    ${bindir}/tuning_mcm_start.sh \
    ${bindir}/capture_tool_sensor_params.py \
    ${bindir}/setup_imx*.sh \
    ${bindir}/find_subdevice_path.sh \
    ${includedir}/imaging \
"

install_dist_ext() {
	for tool in ${IMAGING_EXT_TOOLS}; do
		install -m 0755 -D ${B}/dist/bin/${tool} ${D}${bindir}
	done
}

install_headers() {
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
	cp ${S}/units/cam_device/include/cam_device_2dnr/* ${D}${includedir}/imaging
	cp ${S}/units/cam_device/include/cam_device_3dnr/* ${D}${includedir}/imaging
	cp ${S}/units/cam_device/include/cam_device_gc2/* ${D}${includedir}/imaging
	cp ${S}/units/cam_device/include/cam_device_demosaic2/* ${D}${includedir}/imaging
	cp ${S}/units/cam_device/include/cam_device_wdr4/* ${D}${includedir}/imaging
	cp ${S}/units/isi/include/* ${D}${includedir}/imaging
	cp ${S}/units/isi/include_priv/* ${D}${includedir}/imaging
	cp ${S}/units/3av2/include/*.h ${D}${includedir}/imaging
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
	cp ${S}/units/common/include/* ${D}${includedir}/imaging/common
	cp ${S}/units/hal/include/* ${D}${includedir}/imaging/hal
	cp ${S}/units/oslayer/include/* ${D}${includedir}/imaging/oslayer
	cp ${S}/units/fpga/fpga/include/* ${D}${includedir}/imaging/fpga
	cp ${S}/units/isi/include/* ${D}${includedir}/imaging/isi
	cp ${S}/units/cameric_drv/include/cameric_drv_common.h ${D}${includedir}/imaging/cameric_drv
}

install_scripts_ext() {
	install -m 0755 -D ${S}/scripts/external/hailo_tuning_server.sh ${D}${bindir}
	install -m 0755 -D ${S}/scripts/external/hailo_tuning_server_nnhdr_fhd.sh ${D}${bindir}
	install -m 0755 -D ${S}/scripts/external/tuning_mcm_start.sh ${D}${bindir}
	install -m 0755 -D ${S}/scripts/external/capture_tool_sensor_params.py ${D}${bindir}

	install -m 0755 -D ${S}/scripts/external/setup_imx*.sh ${D}${bindir}
	install -m 0755 -D ${S}/scripts/external/find_subdevice_path.sh ${D}${bindir}
}

###############################################################################
# do_install — entry point
###############################################################################

do_install() {
	install -d ${D}/lib
	install -d ${D}/etc
	install -d ${D}/etc/init.d
	install -d ${D}/etc/rc5.d
	install -d ${D}${bindir}

	# Core
	copy_lib_files
	install_isp_media_server
	install_dist
	install_misc

	# Headers — always installed to populate the sysroot for downstream recipes (e.g. iss-drivers).
	# Packaged into ${PN}-ext but not installed on core images at runtime.
	install_headers

	# Extended binaries/scripts — only for non-production images
	if ${@bb.utils.contains('DISTRO_FEATURES', 'hailo-core', 'false', 'true', d)}; then
		install_dist_ext
		install_scripts_ext
	fi
}
