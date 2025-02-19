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
    sensors \
    usb \
    "

EXTRA_IMAGE_FEATURES:append = " \
    allow-root-login \
    "

ENABLE_DISTRO_FEATURES = "True"
