require hailo-product-type-base.inc

DISTRO_FEATURES:append = " \
    gen-ai \
    hrt-gen-ai \
    hrt-server \
    pci-server \
    "

DISTROOVERRIDES =. "accelerator:"
