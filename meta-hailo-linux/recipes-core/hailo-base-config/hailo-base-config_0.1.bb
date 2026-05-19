DESCRIPTION = "Basic bash configuration for hailo images"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

S = "${WORKDIR}"

FILESEXTRAPATHS:prepend:hailo15 := "${THISDIR}/files/:"
SRC_URI:append:hailo15 = "file://inputrc;striplevel=3 file://bashrc;striplevel=3 file://profile;striplevel=3 file://glib_always_malloc.sh;striplevel=3 file://bashrc-securefs;striplevel=3"

do_install:append () {
  install -d                                     ${D}${ROOT_HOME}

  # Install base bashrc
  install -m 0755 ${S}/bashrc                    ${D}${ROOT_HOME}/.bashrc

  # Conditionally append securefs-specific configuration
  if ${@bb.utils.contains('DISTRO_FEATURES', 'securefs', 'true', 'false', d)}; then
    cat ${S}/bashrc-securefs >> ${D}${ROOT_HOME}/.bashrc
  fi

  install -m 0755 ${S}/profile                   ${D}${ROOT_HOME}/.profile
  install -m 0600 ${S}/inputrc                   ${D}${ROOT_HOME}/.inputrc

  install -d ${D}${sysconfdir}/profile.d
  install -m 0600 ${S}/glib_always_malloc.sh     ${D}${sysconfdir}/profile.d/glib_always_malloc.sh
}

FILES:${PN} += "/home/root /home/root/.bashrc /home/root/.profile /home/root/.inputrc /etc/profile.d/glib_always_malloc.sh"

