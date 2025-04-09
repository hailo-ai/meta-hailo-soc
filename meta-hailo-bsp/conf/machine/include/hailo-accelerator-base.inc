require hailo-product-type-base.inc

DISTRO_FEATURES:append = " \
    hrt-gen-ai \
    hrt-server \
    "
DISTROOVERRIDES =. "accelerator:"
