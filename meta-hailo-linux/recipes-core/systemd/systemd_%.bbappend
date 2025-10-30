FILESEXTRAPATHS:prepend := "${THISDIR}/systemd-conf:"

SRC_URI += "\
    file://dropins/wait-online-timeout.conf \
    file://journald/journald-err-level.conf \
    file://services/serial-log-level.service \
    file://system/quiet.conf \
"

inherit systemd

SYSTEMD_DIR = "${sysconfdir}/systemd"

do_install:append() {
    # Journald journald-err-level conf
    install -d ${D}${SYSTEMD_DIR}/journald.conf.d
    install -m 0644 ${WORKDIR}/journald/journald-err-level.conf ${D}${SYSTEMD_DIR}/journald.conf.d/
    # Disable systemd-journald-audit.socket to prevent audit logs
    ln -sf /dev/null ${D}${systemd_system_unitdir}/systemd-journald-audit.socket

    # Set up systemd-networkd-wait-online service with custom timeout
    install -d ${D}${systemd_system_unitdir}/systemd-networkd-wait-online.service.d
    install -m 0644 ${WORKDIR}/dropins/wait-online-timeout.conf \
        ${D}${systemd_system_unitdir}/systemd-networkd-wait-online.service.d/

    # Quiet serial service
    install -D -m 0644 ${WORKDIR}/services/*.service ${D}${systemd_system_unitdir}/
    # Create symlink to enable the services
    install -d ${D}${SYSTEMD_DIR}/system/multi-user.target.wants/
    ln -sf ${systemd_system_unitdir}/serial-log-level.service ${D}${SYSTEMD_DIR}/system/multi-user.target.wants/

    # Systemd quiet configuration - this is NECESSARY for properly controlling systemd logs
    install -d ${D}${SYSTEMD_DIR}/system.conf.d
    install -m 0644 ${WORKDIR}/system/quiet.conf ${D}${SYSTEMD_DIR}/system.conf.d/
}

# Add serial-log-level service to the main systemd package
FILES:${PN} += "${systemd_system_unitdir}/serial-log-level.service"
FILES:${PN} += "${SYSTEMD_DIR}/journald.conf.d/journald-err-level.conf"
FILES:${PN} += "${SYSTEMD_DIR}/system.conf.d/quiet.conf"

SYSTEMD_SERVICE:${PN} += "serial-log-level.service"
# Set systemd native service systemd-networkd, only if MACHINE_FEATURES does not include 'no_eth' (hence systemd-network recipe is not installed)
SYSTEMD_SERVICE:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'no_eth', '', 'systemd-networkd.service', d)}"
SYSTEMD_AUTO_ENABLE = "enable"
