DESCRIPTION = "Hailo SCU bootloader. \
               This recipe will download the SCU bootloader binary from AWS and add it to deploy"

inherit deploy
inherit hailo-cc312-sign

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://../LICENSE;md5=263ee034adc02556d59ab1ebdaea2cda"

BASE_URI = "https://hailo-hailort.s3.eu-west-2.amazonaws.com/${HAILO_PLATFORM_NAME}/1.12.1/scu-bl"
BL = "${HAILO_SOC_NAME}_scu_bl.bin"
BL_UNSIGNED = "${HAILO_SOC_NAME}_scu_bl.unsigned.bin"
BL_CUSTOMER_SIGNED = "${SCU_BL_CUSTOMER_SIGNED_BINARY_NAME}"
LICENSE_FILE = "LICENSE"

scu_bl_cfg_targetdir = "/etc/scu_bl_cfg"

CONFIG_MANAGER_PY = "scu_bootloader_config_manager.py"

BL_FILES = "${BASE_URI}/${BL};name=bl_${HAILO_SOC_NAME} \
            ${BASE_URI}/${BL_UNSIGNED};name=bl_unsigned_${HAILO_SOC_NAME} \
            ${BASE_URI}/${LICENSE_FILE};name=lic"

# hailo10-usb-dongle:
# - Flash size fits to a dual partition.
# - scu_bl_cfg_a should includes 2 available boots source
# Otherwise use VPUs scu_bl_cfg:
# - VPU scu_bl_cfg_a includes only 1 boots source (partition A)
# - Note:
#   - hailo10-sbc-rev3: flash size fits to a single partition.
#   - hailo10-m2: doesn't have any flash device
FILESEXTRAPATHS:prepend:hailo10-usb-dongle := "${THISDIR}/files/h10-usb/:"

CONFIG_JSONS = "scu_bl_cfg_a.json"
SRC_URI = "file://scu_bootloader_config_manager.py \
           ${BL_FILES}"

SRC_URI[bl_hailo15.sha256sum] = "bea89a5e8e77125195cd9581447e908f996828df878ae460e224efe5cda4c239"
SRC_URI[bl_hailo15l.sha256sum] = "a4f3d348e61d8e3ac28fff49bb003460a2cef5caae323ad7fb33ad1ad3a6a936"
SRC_URI[bl_unsigned_hailo15.sha256sum] = "c84ab0ffb12a5f5e9c4ba69b2391d1eda16c8a4ff6d5783a881bcff5272ab1f8"
SRC_URI[bl_unsigned_hailo15l.sha256sum] = "c5e518cb7261a837fd91daca2211c0e56ca9f5903cb7f1e9355dab29b79785fd"
SRC_URI[lic.sha256sum] = "ca96445e6e33ae0a82170ea847b0925c864492f0cbb6342d42c54fd647133608"

python() {
    import os

    config_jsons = d.getVar('CONFIG_JSONS')

    for config_json in config_jsons.split():
        d.appendVar('SRC_URI', ' file://' + config_json)
        d.appendVar('FILES:' + d.getVar('PN'), ' ${scu_bl_cfg_targetdir}/' + os.path.basename(config_json))
}

do_sign() {
  if [ -n "${HAS_CUSTOMER_ROOT_KEY}" ]; then
    hailo15_scu_firmware_sign ${WORKDIR}/${BL_UNSIGNED} ${WORKDIR}/${BL_CUSTOMER_SIGNED}
  fi
}

addtask sign after do_compile before do_install

do_install() {
  install -m 0755 -d ${D}${scu_bl_cfg_targetdir}

  # Loop through each json file
  for json_file_name in ${CONFIG_JSONS}; do
    # Run the python script to create the binary file
    ${WORKDIR}/${CONFIG_MANAGER_PY} --scu-bl-cfg-json ${WORKDIR}/${json_file_name} --scu-bl-cfg-bin ${WORKDIR}/"${json_file_name%.json}.bin"

    # Install the binary files to the appropriate location
    install -m 0500 ${WORKDIR}/"${json_file_name%.json}.bin" ${D}/${scu_bl_cfg_targetdir}
  done
}


do_deploy() {
  if [ -z "${HAS_CUSTOMER_ROOT_KEY}" ]; then
    install -m 644 -D ${WORKDIR}/${BL} ${DEPLOYDIR}/${BL}
  else
    install -m 644 -D ${WORKDIR}/${BL_CUSTOMER_SIGNED} ${DEPLOYDIR}/${BL_CUSTOMER_SIGNED}
    ln -s -r ${DEPLOYDIR}/${BL_CUSTOMER_SIGNED} ${DEPLOYDIR}/${BL}
  fi

  # Install all the scu bl cfg binary files to the deploy directory
  for json in ${CONFIG_JSONS}; do
    install -m 644 -D ${WORKDIR}/"${json%.json}.bin" ${DEPLOYDIR}
  done

  install -m 644 -D ${WORKDIR}/${BL_UNSIGNED} ${DEPLOYDIR}/${BL_UNSIGNED}
}

addtask deploy after do_install
