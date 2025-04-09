require hailo-product-type-base.inc

DISTRO_FEATURES:append = " \
    audio \
    display \
    fs \
    kernel-modules \
    video \
    x11 \
    "
DISTROOVERRIDES =. "vpu:"
