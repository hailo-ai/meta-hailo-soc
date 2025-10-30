FILESEXTRAPATHS:prepend := "${THISDIR}/:"
SRC_URI += " file://cfg/fragment.cfg"
SRC_URI += " file://0001-Add-FAT32-filesystem-support.patch"

# disable init script
INITSCRIPT_PARAMS = "remove"

# disable systemd services
SYSTEMD_PACKAGES = ""
SYSTEMD_SERVICE:${PN} = ""
SYSTEMD_SERVICE:${PN}-progress = ""
SYSTEMD_SERVICE:${PN}-usb = ""
SYSTEMD_AUTO_ENABLE = "disable"

do_install:append () {
    rm -rf ${D}${sysconfdir}/init.d/swupdate
}
