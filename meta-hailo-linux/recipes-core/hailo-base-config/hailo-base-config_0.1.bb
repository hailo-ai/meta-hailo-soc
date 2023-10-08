DESCRIPTION = "Basic bash configuration for hailo images"
LICENSE = "CLOSED"

S = "${WORKDIR}"

FILESEXTRAPATHS:prepend:hailo15 := "${THISDIR}/files/:"
SRC_URI:append:hailo15 = "file://inputrc;striplevel=3 file://bashrc;striplevel=3 file://profile;striplevel=3"

do_install:append () {
  install -d                                     ${D}${ROOT_HOME}
  install -m 0755 ${S}/bashrc                    ${D}${ROOT_HOME}/.bashrc
  install -m 0755 ${S}/profile                   ${D}${ROOT_HOME}/.profile
  install -m 0600 ${S}/inputrc                   ${D}${ROOT_HOME}/.inputrc
}

FILES:${PN} += "/home/root /home/root/.bashrc /home/root/.profile /home/root/.inputrc"

