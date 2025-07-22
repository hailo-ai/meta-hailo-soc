SUMMARY = "Hailo BSP requirements"
DESCRIPTION = "The set of packages required to enable BSP functionality"

PACKAGE_ARCH = "${MACHINE_ARCH}"
# The packagegroup is just configuration/grouping mechanisms, hence tracking it in buildhistory doesn't provide useful information
BUILDHISTORY_FEATURES:remove = "image package"

inherit packagegroup hailo-feature-control

PACKAGEGROUP_DISABLE_COMPLEMENTARY = "1"
PACKAGES = "${PN} ${PN}-dev-pkg"

BSP_DISTRO_FEATURES = "kernel-modules"

RDEPENDS:${PN} = "\
    hailo-cma-usage \
    hailo-dma-usage \
    ${@" ".join(get_features_to_enable(d, d.getVar("BSP_DISTRO_FEATURES")))} \
    recovery-fw \
    scu-bl \
    scu-fw \
    libhailo-throttling \
    u-boot-env \
    libubootenv-bin \
    hailo-linux-init \
    ncurses libnl libnl-genl \
    hailo-thermal-engine \
    hailo-thermal-service"

# Development package group
RDEPENDS:${PN}-dev-pkg = "\
    packagegroup-hailo-bsp \
    hailo-noc-measurement-script \
    ncurses-dev libnl libnl-genl \
    libhailo-throttling-dev \
    hailo-thermal-engine-dev \
    hailo-simple-throttling-app"

# Recovery-FW and SCU bootloader are not implemented
RDEPENDS:${PN}:remove:hailo10h2-maple = "recovery-fw scu-bl"
RDEPENDS:${PN}:remove:hailo10h2-mint = "recovery-fw scu-bl"
RDEPENDS:${PN}:remove:hailo10h2-veloce = "recovery-fw scu-bl"