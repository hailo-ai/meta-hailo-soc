FILESEXTRAPATHS:prepend := "${THISDIR}:"

SRC_URI:append = " file://cfg/media-conf.cfg"
SRC_URI:append = " file://cfg/csi-configuration.cfg"
SRC_URI:append = " file://cfg/pix-mux-conf.cfg"
SRC_URI:append = " file://cfg/rxwrapper-conf.cfg"
SRC_URI:append = " file://cfg/isp-conf.cfg"
SRC_URI:append = " file://cfg/video-conf.cfg"
SRC_URI:append = " file://cfg/af-monitor-example.cfg"
SRC_URI:append = " ${@bb.utils.contains('MACHINE_FEATURES', 'imx334', 'file://cfg/imx334-sensor-conf.cfg', '', d)}"
SRC_URI:append = " ${@bb.utils.contains('MACHINE_FEATURES', 'imx678', 'file://cfg/imx678-sensor-conf.cfg', '', d)}"

USE_SENSOR_IMX678 = "${@bb.utils.contains('MACHINE_FEATURES', 'imx678', '1', '', d)}"
SENSOR_H_PATH = "arch/arm64/boot/dts/hailo/hailo15-camera-sensor.h"
do_compile:prepend() {
    echo "USE_SENSOR_IMX678 is: ${USE_SENSOR_IMX678}"
    if [ "${USE_SENSOR_IMX678}" -eq 1 ]; then
        echo "using sensor imx678"
        echo "#define SENSOR_IMX678" > "${S}/${SENSOR_H_PATH}"
    else
        echo "using sensor imx334"
        echo "#define SENSOR_IMX334" > "${S}/${SENSOR_H_PATH}"
    fi
}
