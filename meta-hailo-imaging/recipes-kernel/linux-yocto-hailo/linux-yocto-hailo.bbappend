FILESEXTRAPATHS:prepend := "${THISDIR}:"

SRC_URI:append = " file://cfg/media-conf.cfg"
SRC_URI:append = " file://cfg/csi-configuration.cfg"
SRC_URI:append = " file://cfg/pix-mux-conf.cfg"
SRC_URI:append = " file://cfg/rxwrapper-conf.cfg"
SRC_URI:append = " file://cfg/isp-conf.cfg"
SRC_URI:append = " file://cfg/video-conf.cfg"
SRC_URI:append = " file://cfg/af-monitor-example.cfg"
SRC_URI:append = " file://cfg/sensor-configuration.cfg"

# Introspect SV4E MIPI generator subdev. Built as =m and dormant under
# the default IMX boot (no mipi_generator,sv4e DT node = nothing to bind
# the driver to). Activated by applying the hailo15-sv4e.dtbo overlay
# at boot via U-Boot's ${dtb_overlays} env. The enable direction is
# driven by mipi_tool's board-mode subcommand; on-board rollback uses
# /usr/bin/disable_mipi_generator.sh (shipped from imaging-sub-system).
SRC_URI:append = " file://cfg/sensor-sv4e.cfg"

# Ship the SV4E overlay alongside the base DTB on every H15-family SBC
# machine whose base DTS exports the labels the overlay binds against
# (csi2rx_in_sensor / sensor_0 / i2c_0), so the fitImage carries both and
# SV4E is selectable at boot via ${dtb_overlays}. One generic overlay is
# reused across machines — it isn't board-specific.
KERNEL_DEVICETREE:append:hailo15-sbc      = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sv4e.dtbo"
KERNEL_DEVICETREE:append:hailo15-sbc-rev3 = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sv4e.dtbo"
KERNEL_DEVICETREE:append:hailo15l-sbc     = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sv4e.dtbo"

# Camera port 0 overlays, selected at boot by U-Boot from the module's Type-ID
# registers. A camera is described by two overlays applied together:
#
#   module axis (keyed on the i2c address the part answered on) — sets reg
#   sensor axis (keyed on the decoded Type-ID)                  — sets
#       compatible, INCK, link-frequencies, status
SENSOR0_DT_OVERLAYS = " \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor0-addr1a.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor0-addr10.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor0-legacy.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor0-imx307.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor0-imx334.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor0-imx662.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor0-imx664.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor0-imx675.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor0-imx678.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor0-imx715.dtbo \
"

KERNEL_DEVICETREE:append:hailo15-sbc        = "${SENSOR0_DT_OVERLAYS}"
KERNEL_DEVICETREE:append:hailo15-sbc-rev3   = "${SENSOR0_DT_OVERLAYS}"
KERNEL_DEVICETREE:append:hailo15-sbc-rev3-1 = "${SENSOR0_DT_OVERLAYS}"
KERNEL_DEVICETREE:append:hailo15l-sbc       = "${SENSOR0_DT_OVERLAYS}"
# The NAND-boot H15L SBC is the same board; it is a peer machine, not a child
# of hailo15l-sbc, so the appends above do not reach it.
KERNEL_DEVICETREE:append:hailo15l-sbc-nand  = "${SENSOR0_DT_OVERLAYS}"
# No EVB line here on purpose: CONFIG_HAILO_SENSOR_DETECT cannot be selected
# on the EVB targets, so nothing on those boards can ever name an overlay.

# Camera port 1, for the boards that have one. It mirrors the port-0 family:
# U-Boot constructs the overlay name from the model it detected, on either
# port, and a name that is not in the fitImage fails the boot -- so every
# sensor that can be detected must be describable on every port that has one.
SENSOR1_DT_OVERLAYS = " \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor1-addr1a.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor1-addr10.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor1-imx307.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor1-imx334.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor1-imx662.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor1-imx664.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor1-imx675.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor1-imx678.dtbo \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sensor1-imx715.dtbo \
"

KERNEL_DEVICETREE:append:hailo15-sbc              = "${SENSOR1_DT_OVERLAYS}"
KERNEL_DEVICETREE:append:hailo15-evb-2-camera-vpu = "${SENSOR1_DT_OVERLAYS}"
KERNEL_DEVICETREE:append:hailo15l-sbc             = "${SENSOR1_DT_OVERLAYS}"
KERNEL_DEVICETREE:append:hailo15l-sbc-nand        = "${SENSOR1_DT_OVERLAYS}"

# Hailo15L-SBC link wiring. Its single D-PHY is split 2+2 across the two
# bridges, so a camera runs on two lanes even when it is the only one fitted;
# the dual-link overlay additionally puts both bridges under that one D-PHY and
# is applied only when both ports have a sensor. Neither names a sensor, so a
# new sensor on this board needs no new wiring overlay.
KERNEL_DEVICETREE:append:hailo15l-sbc = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15l-sbc-2lane.dtbo"
KERNEL_DEVICETREE:append:hailo15l-sbc = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15l-sbc-dual-link.dtbo"
KERNEL_DEVICETREE:append:hailo15l-sbc-nand = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15l-sbc-2lane.dtbo"
KERNEL_DEVICETREE:append:hailo15l-sbc-nand = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15l-sbc-dual-link.dtbo"
