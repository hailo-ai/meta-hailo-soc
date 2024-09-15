SUMMARY = "Minimal Hailo Linux requirements"
DESCRIPTION = "The minimal set of packages required to boot the system"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

PACKAGEGROUP_DISABLE_COMPLEMENTARY = "1"

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-base"
RDEPENDS:${PN}-base = "\
    glibc-binary-localedata-en-us \
    hailo-base-config \
    kmod \
    openssl \
    openssl-bin \
    os-release \
    util-linux"

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-base-dev"
RDEPENDS:${PN}-base-dev = "\
    ${PN} \
    gdb \
    htop \
    lrzsz \
    perf \
    stress-ng \
    sysstat \
    tmux \
    tree \
    tzdata \
    ulimit \
    vim \
    valgrind \
    xauth \
    xeyes \
    xhost"

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-audio"
RDEPENDS:${PN}-audio = "\
    alsa-lib \
    alsa-plugins \
    alsa-state \
    alsa-topology-conf \
    alsa-utils \
    alsa-utils-scripts"

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-audio-dev"
RDEPENDS:${PN}-audio-dev = "\
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
HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-video"
RDEPENDS:${PN}-video = "\
    v4l-utils \
    ${@bb.utils.contains('ADD_GSTREAMER_TO_IMAGE', 'true', '${GSTREAMER_VERSIONS}', '', d)}"

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-video-dev"
RDEPENDS:${PN}-video-dev = "\
    ${PN}-video"

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-networking"
RDEPENDS:${PN}-networking = ""

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-networking-dev"
RDEPENDS:${PN}-networking-dev = "\
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

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-usb"
RDEPENDS:${PN}-usb = ""

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-usb-dev"
RDEPENDS:${PN}-usb-dev = "\
    ${PN}-usb \
    usbutils \
    usbutils-dbg \
    usbutils-dev \
    usbutils-doc \
    usbutils-python \
    "

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-python"
RDEPENDS:${PN}-python = "\
    ${@bb.utils.contains('ADD_PYTHON_TO_IMAGE', 'true', 'python3', '', d)} \
    ${@bb.utils.contains('ADD_PYTHON_NUMPY_TO_IMAGE', 'true', 'python3-numpy', '', d)} \
    "

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-python-dev"
RDEPENDS:${PN}-python-dev = "\
    ${PN}-python \
    "

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-iio"
RDEPENDS:${PN}-iio = "\
    libiio \
    libiio-iiod \
    "

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-iio-dev"
RDEPENDS:${PN}-iio-dev = "\
    ${PN}-iio \
    libiio-dbg \
    libiio-dev \
    libiio-tests \
    "

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-fs"
RDEPENDS:${PN}-fs = "\
    dosfstools \
    e2fsprogs \
    e2fsprogs-resize2fs \
    "

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-fs-dev"
RDEPENDS:${PN}-fs-dev = "\
    ${PN}-fs \
    exfat-utils \
    fuse-exfat \
    gptfdisk \
    "

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-gpio"
RDEPENDS:${PN}-gpio = "\
    "

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-gpio-dev"
RDEPENDS:${PN}-gpio-dev = "\
    ${PN}-gpio \
    libgpiod \
    libgpiod-tools \
    "

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-sensors"
RDEPENDS:${PN}-sensors = "\
    lmsensors-libsensors \
    lmsensors-sensors \
    sensors-config-file \
    "

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-sensors-dev"
RDEPENDS:${PN}-sensors-dev = "\
    ${PN}-sensors \
    "

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-mmc"
RDEPENDS:${PN}-mmc = "\
    "

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-mmc-dev"
RDEPENDS:${PN}-mmc-dev = "\
    ${PN}-mmc \
    mmc-utils \
    "

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-mtd"
RDEPENDS:${PN}-mtd = "\
    "

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-mtd-dev"
RDEPENDS:${PN}-mtd-dev = "\
    ${PN}-mtd \
    mtd-utils \
    "

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-pci"
RDEPENDS:${PN}-pci = "\
    "

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-pci-dev"
RDEPENDS:${PN}-pci-dev = "\
    ${PN}-pci \
    pciutils \
    "

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-ddr"
RDEPENDS:${PN}-ddr = "\
    ${@bb.utils.contains('MACHINE_FEATURES', 'ddr_ecc_en', 'edac-utils', '', d)} \
    "

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-ddr-dev"
RDEPENDS:${PN}-ddr-dev = "\
    ${PN}-ddr \
    edac-utils \
    "

HAILO_LINUX_SUB_PACKAGEGROUPS += "${PN}-display"
RDEPENDS:${PN}-display = "\
    libdrm \
    libdrm-kms \
    "

HAILO_LINUX_DEV_SUB_PACKAGEGROUPS += "${PN}-display-dev"
RDEPENDS:${PN}-display-dev = "\
    ${PN}-display \
    libdrm-dbg \
    libdrm-tests \
    "

# full packagegroups containing all the sub-packagegroups
RDEPENDS:${PN} = "${HAILO_LINUX_SUB_PACKAGEGROUPS}"
RDEPENDS:${PN}-dev = "${PN} ${HAILO_LINUX_DEV_SUB_PACKAGEGROUPS}"

PACKAGES = "${PN} ${PN}-dev ${HAILO_LINUX_SUB_PACKAGEGROUPS} ${HAILO_LINUX_DEV_SUB_PACKAGEGROUPS}"
