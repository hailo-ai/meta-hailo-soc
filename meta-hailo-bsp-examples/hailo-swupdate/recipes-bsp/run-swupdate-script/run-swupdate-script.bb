DESCRIPTION = "Run the swupdate process"
SECTION = "apps"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://../COPYING.MIT;md5=aa7321c8e0df442b97243c2e1d64c9ee"

ACCELERATOR_CUSTOMER_KEY = "customer_pubkey.pem"
ACCELERATOR_FREENAS_PATH = "hailo@192.168.12.21:/mnt/v02/sdk/customer_generated_certificate/hailo_hsm/"

RDEPENDS:${PN} += "bash"
RDEPENDS:${PN} += "scu-bl"
RDEPENDS:${PN}:remove:hailo12l-evb = "scu-bl"
RDEPENDS:${PN}:remove:hailo12l-maple = "scu-bl"
RDEPENDS:${PN}:remove:hailo12l-mint = "scu-bl"
RDEPENDS:${PN}:remove:hailo12l-veloce = "scu-bl"
targetdir = "/etc"

CRC_VERIFY_FILE_NAME = "verify_file_crc"

SRC_URI = "file://run_swupdate.sh \
           file://get_sw_image.sh \
           file://set_sw_image.sh \
           file://get_boot_dev.sh \
           file://boot_definitions.sh \
           file://${CRC_VERIFY_FILE_NAME}.c \
           file://COPYING.MIT"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Custom fetch for accelerator customer key
do_fetch:append:accelerator() {
    bb.note("Fetching accelerator customer key from freenas...")
    import subprocess
    import os

    customer_key = d.getVar('ACCELERATOR_CUSTOMER_KEY')
    freenas_path = d.getVar('ACCELERATOR_FREENAS_PATH')
    workdir = d.getVar('WORKDIR')

    # Remove existing file if it exists
    key_file_path = os.path.join(workdir, customer_key)
    if os.path.exists(key_file_path):
        os.remove(key_file_path)

    # SCP the file from freenas
    scp_cmd = ['scp', f'{freenas_path}{customer_key}', workdir]
    bb.note(f"SCP command: {scp_cmd}")

    try:
        result = subprocess.run(scp_cmd, check=True, capture_output=True, text=True)
        bb.note(f"Successfully fetched {customer_key} from freenas")
    except subprocess.CalledProcessError as e:
        bb.fatal(f"Failed to fetch {customer_key} from freenas: {e}")
}

do_compile () {
   ${CC} ${CFLAGS} ${LDFLAGS}  ${WORKDIR}/${CRC_VERIFY_FILE_NAME}.c -o ${WORKDIR}/${CRC_VERIFY_FILE_NAME} 
}

do_install() {
    install -m 0755 -d ${D}${targetdir}
    install -m 0500 ${WORKDIR}/run_swupdate.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/get_boot_dev.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/get_sw_image.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/set_sw_image.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/boot_definitions.sh ${D}${targetdir}
    install -m 0500 ${WORKDIR}/${CRC_VERIFY_FILE_NAME} ${D}${targetdir}/${CRC_VERIFY_FILE_NAME}
}

do_install:append:accelerator() {
    install -m 0644 ${WORKDIR}/${ACCELERATOR_CUSTOMER_KEY} ${D}${targetdir}/${ACCELERATOR_CUSTOMER_KEY}
}

FILES:${PN} += "${targetdir}/run_swupdate.sh"
FILES:${PN} += "${targetdir}/get_boot_dev.sh"
FILES:${PN} += "${targetdir}/get_sw_image.sh"
FILES:${PN} += "${targetdir}/set_sw_image.sh"
FILES:${PN} += "${targetdir}/boot_definitions.sh"
FILES:${PN} += "${targetdir}/${CRC_VERIFY_FILE_NAME}"
FILES:${PN}:append:accelerator = " ${targetdir}/${ACCELERATOR_CUSTOMER_KEY}"
