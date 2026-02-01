
CONFIG_JSONS:append = " scu_bl_cfg_a_remote_update.json"
CONFIG_JSONS:append = " scu_bl_cfg_b.json"

# Additional json files may be added to the list CONFIG_JSONS as above
# These should have the same filename template, and json structure

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
FILESEXTRAPATHS:prepend:accelerator := "${THISDIR}/files/h10-usb/:"

