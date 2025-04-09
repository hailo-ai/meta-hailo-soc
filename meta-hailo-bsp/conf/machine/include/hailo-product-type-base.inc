require hailo-package-manager.inc

# Hailo product type (VPU/Accelerator) base configuration
DISTRO_FEATURES:append = " \
    ddr \
    gpio \
    iio \
    mmc \
    mtd \
    networking \
    pam \
    pci \
    pkg-manager \
    sensors \
    usb \
    "

EXTRA_IMAGE_FEATURES:append = " \
    allow-root-login \
    package-management \
    "

ENABLE_DISTRO_FEATURES = "True"
