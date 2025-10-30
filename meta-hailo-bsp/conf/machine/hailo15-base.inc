require conf/machine/include/hailo1x-base.inc

HAILO_SOC_NAME ??= "hailo15"
HAILO_HOST_NAME ??= "${HAILO_SOC_NAME}"

HAILO_PLATFORM_NAME = "${@d.getVar('HAILO_SOC_NAME').capitalize()}"
hostname:pn-base-files ??= "${HAILO_HOST_NAME}"

SCU_FW_MACHINE = "${HAILO_SOC_NAME}"
SCU_BL_MACHINE = "${HAILO_SOC_NAME}"
RECOVERY_FW_MACHINE = "${HAILO_SOC_NAME}"
DSP_FW_TARGET = "${HAILO_SOC_NAME}"
