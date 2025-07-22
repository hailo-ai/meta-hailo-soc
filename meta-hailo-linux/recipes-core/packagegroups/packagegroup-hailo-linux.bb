SUMMARY = "Minimal Hailo Linux requirements"
DESCRIPTION = "The minimal set of packages required to boot the system"

PACKAGE_ARCH = "${MACHINE_ARCH}"
# The packagegroup is just configuration/grouping mechanisms, hence tracking it in buildhistory doesn't provide useful information
BUILDHISTORY_FEATURES:remove = "image package"

inherit packagegroup hailo-feature-control

PACKAGEGROUP_DISABLE_COMPLEMENTARY = "1"

RDEPENDS:${PN}-base = "\
    glibc-binary-localedata-en-us \
    hailo-base-config \
    kmod \
    openssl \
    openssl-bin \
    os-release \
    util-linux"

RDEPENDS:${PN}-base-dev-pkg = "\
    ${PN} \
    gdb \
    hailo-soc-profiler \
    hailo-soc-profiler-service \
    htop \
    libperfetto \
    lrzsz \
    lsof \
    perf \
    perfetto \
    stress-ng \
    sysstat \
    tmux \
    tree \
    tzdata \
    vim \
    valgrind \
    xauth \
    xeyes \
    xhost"

RDEPENDS:${PN}-audio = "\
    alsa-lib \
    alsa-plugins \
    alsa-state \
    alsa-topology-conf \
    alsa-utils \
    alsa-utils-scripts"

RDEPENDS:${PN}-audio-dev-pkg = "\
    ${PN}-audio \
    alsa-tools"

# video
# Adding gstreamer to image only if env var ADD_GSTREAMER_TO_IMAGE is set to "true"
GSTREAMER_VERSIONS = " \
    gstreamer1.0 \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-rtsp-server"
RDEPENDS:${PN}-video = "\
    v4l-utils \
    ${@bb.utils.contains('ADD_GSTREAMER_TO_IMAGE', 'true', '${GSTREAMER_VERSIONS}', '', d)}"

RDEPENDS:${PN}-video-dev-pkg = "\
    ${PN}-video"

RDEPENDS:${PN}-networking = ""

RDEPENDS:${PN}-networking-dev-pkg = "\
    ${PN}-networking \
    ethtool \
    libpam \
    linux-firmware-rtl-nic \
    nfs-utils-client \
    ntp \
    openssh-sftp-server \
    phytool \
    rsync \
    ssmtp \
    tcpdump \
    vsftpd \
    "

RDEPENDS:${PN}-usb = ""

RDEPENDS:${PN}-usb-dev-pkg = "\
    ${PN}-usb \
    usbutils \
    usbutils-dbg \
    usbutils-dev \
    usbutils-doc \
    usbutils-python \
    "

RDEPENDS:${PN}-python = "\
    ${@bb.utils.contains('ADD_PYTHON_TO_IMAGE', 'true', 'python3', '', d)} \
    ${@bb.utils.contains('ADD_PYTHON_NUMPY_TO_IMAGE', 'true', 'python3-numpy', '', d)} \
    "

RDEPENDS:${PN}-python-dev-pkg = "\
    ${PN}-python \
    "

RDEPENDS:${PN}-iio = "\
    libiio \
    libiio-iiod \
    "

RDEPENDS:${PN}-iio-dev-pkg = "\
    ${PN}-iio \
    libiio-dbg \
    libiio-dev \
    libiio-tests \
    "

RDEPENDS:${PN}-fs = "\
    dosfstools \
    e2fsprogs \
    e2fsprogs-mke2fs \
    e2fsprogs-resize2fs \
    "

RDEPENDS:${PN}-fs-dev-pkg = "\
    ${PN}-fs \
    exfat-utils \
    fuse-exfat \
    gptfdisk \
    "

RDEPENDS:${PN}-gpio = "\
    "

RDEPENDS:${PN}-gpio-dev-pkg = "\
    ${PN}-gpio \
    libgpiod \
    libgpiod-tools \
    "

RDEPENDS:${PN}-sensors = "\
    lmsensors-libsensors \
    lmsensors-sensors \
    sensors-config-file \
    "

RDEPENDS:${PN}-sensors-dev-pkg = "\
    ${PN}-sensors \
    "

RDEPENDS:${PN}-mmc = "\
    "

RDEPENDS:${PN}-mmc-dev-pkg = "\
    ${PN}-mmc \
    mmc-utils \
    "

RDEPENDS:${PN}-mtd = "\
    "

RDEPENDS:${PN}-mtd-dev-pkg = "\
    ${PN}-mtd \
    mtd-utils \
    "

RDEPENDS:${PN}-pkg-manager = "\
    opkg \
    libopkg \
    "

RDEPENDS:${PN}-pkg-manager-dev-pkg = "\
    ${PN}-pkg-manager \
    "

RDEPENDS:${PN}-pci = "\
    "

RDEPENDS:${PN}-pci-dev-pkg = "\
    ${PN}-pci \
    pciutils \
    "

RDEPENDS:${PN}-spi-dev-pkg:hailo15l = "\
    spidev-test \
    spitools \
    "

RDEPENDS:${PN}-ddr = "\
    ${@bb.utils.contains('MACHINE_FEATURES', 'ddr_ecc_en', 'edac-utils', '', d)} \
    "

RDEPENDS:${PN}-ddr-dev-pkg = "\
    ${PN}-ddr \
    edac-utils \
    "

RDEPENDS:${PN}-display = "\
    libdrm \
    libdrm-kms \
    "

RDEPENDS:${PN}-display-dev-pkg = "\
    ${PN}-display \
    libdrm-dbg \
    libdrm-tests \
    "

# full packagegroups containing all the sub-packagegroups
LINUX_FEATURES = "\
    audio \
    ddr \
    display \
    fs \
    gpio \
    iio \
    mmc \
    mtd \
    networking \
    pci \
    pkg-manager \
    python \
    sensors \
    spi \
    usb \
    video"

HAILO_LINUX_SUB_PACKAGEGROUPS = "\
    ${PN}-base \
    ${@get_packagegroups_to_install(d, d.getVar("LINUX_FEATURES"), d.getVar("PN"))}"

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS = "${@" ".join([sub_packagegroup + "-dev-pkg" for sub_packagegroup in d.getVar("HAILO_LINUX_SUB_PACKAGEGROUPS").split()])}"

RDEPENDS:${PN} = "${HAILO_LINUX_SUB_PACKAGEGROUPS}"
RDEPENDS:${PN}-dev-pkg = "${PN} ${HAILO_LINUX_DEV_SUB_PACKAGEGROUPS}"

PACKAGES = "\
    ${PN} \
    ${HAILO_LINUX_SUB_PACKAGEGROUPS} \
    ${PN}-dev-pkg \
    ${HAILO_LINUX_DEV_SUB_PACKAGEGROUPS} \
    "
