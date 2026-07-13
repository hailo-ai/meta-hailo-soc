OS_RELEASE_FIELDS = "\
    NAME VERSION BUILD_IMAGE_METADATA \
"
# Actual value is set in BSP meta.
HAILO_PLATFORM_NAME ??= "Hailo-Platform"

NAME = "${HAILO_PLATFORM_NAME}"
VERSION = "1.12.1-dv-4"
BUILD_IMAGE_METADATA = "/etc/build-info"
