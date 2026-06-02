require recipes-core/images/core-image-minimal.bb
require include/core-image-append.inc

IMAGE_ROOTFS_EXTRA_SPACE = "262144"

IMAGE_FEATURES:append = " hailo-dev"
