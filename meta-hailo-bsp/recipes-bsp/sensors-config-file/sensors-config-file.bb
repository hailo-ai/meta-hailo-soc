DESCRIPTION = "Add sensor config files into Linux image"
SECTION = "apps"
LICENSE = "MIT"
SRC_URI = "file://COPYING.MIT"
LIC_FILES_CHKSUM = "file://../COPYING.MIT;md5=aa7321c8e0df442b97243c2e1d64c9ee"
targetdir = "/etc/sensors.d"

SENSOR_CONF_FILES:append:hailo15-sbc = " hailo15-sbc/ina231_precise-i2c-1.conf"
SENSOR_CONF_FILES:append:hailo15-sbc = " hailo15-sbc/tmp175-i2c-1.conf"
SENSOR_CONF_FILES:append:hailo15-evb = " hailo15-evb/ina231_precise-i2c-1.conf"
SENSOR_CONF_FILES:append:hailo15-evb = " hailo15-evb/tmp175-i2c-1.conf"
SENSOR_CONF_FILES:append = " hailo15-scmi.conf"

python () {
    import os
    sensor_files = d.getVar('SENSOR_CONF_FILES')
    if not sensor_files:
        return
    for sensor_file in sensor_files.split():
        d.appendVar('SRC_URI', ' file://' + sensor_file)
        d.appendVar('FILES:' + d.getVar('PN'), ' ${targetdir}/' + os.path.basename(sensor_file))
}

do_install() {
    install -m 0755 -d ${D}${targetdir}

    for f in ${SENSOR_CONF_FILES}; do
        install -m 0644 ${WORKDIR}/${f} ${D}${targetdir}
    done
}
