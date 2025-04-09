require conf/machine/include/hailo15-family-base.inc

HAILO_SOC_NAME ??= "hailo15"
HAILO_HOST_NAME ??= "${HAILO_SOC_NAME}"
hostname:pn-base-files = "${HAILO_HOST_NAME}"

SCU_FW_MACHINE = "${HAILO_SOC_NAME}"
SCU_BL_MACHINE = "${HAILO_SOC_NAME}"
RECOVERY_FW_MACHINE = "${HAILO_SOC_NAME}"
DSP_FW_TARGET = "${HAILO_SOC_NAME}"
