# Enable securefs feature for hailo recipes
DISTRO_FEATURES:append = " securefs"

# Enable built-in meta-security features
DISTRO_FEATURES:append = " security"

DM_VERITY_IMAGE = "core-image-hailo"
DM_VERITY_IMAGE_TYPE ?= "ext4"
IMAGE_CLASSES += "dm-verity-img"
IMAGE_FEATURES:append = " read-only-rootfs"

# Align data-block size with default hash-block size
DM_VERITY_IMAGE_DATA_BLOCK_SIZE = "4096"

# Pad rootfs to be aligned to 4K boundary
IMAGE_ROOTFS_ALIGNMENT = "4"

# Set block size of ext4 image to 4K (necessary in boot time)
# In addition, disable journal and resize inode since the rootfs is read-only
EXTRA_IMAGECMD:ext4 += " -m 0 -O ^has_journal,^resize_inode -i 4096 -b 4096"