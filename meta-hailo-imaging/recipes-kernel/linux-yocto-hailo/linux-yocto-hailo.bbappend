FILESEXTRAPATHS:prepend := "${THISDIR}:"

SRC_URI:append = " file://cfg/media-conf.cfg"
SRC_URI:append = " file://cfg/csi-configuration.cfg"
SRC_URI:append = " file://cfg/pix-mux-conf.cfg"
SRC_URI:append = " file://cfg/rxwrapper-conf.cfg"
SRC_URI:append = " file://cfg/isp-conf.cfg"
SRC_URI:append = " file://cfg/video-conf.cfg"
SRC_URI:append = " file://cfg/af-monitor-example.cfg"
SRC_URI:append = " file://cfg/sensor-configuration.cfg"

# Hailo15L-SBC: add IMX662 sensor DT overlay
KERNEL_DEVICETREE:append:hailo15l-sbc = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15l-sbc-sensor0-imx662.dtbo"
