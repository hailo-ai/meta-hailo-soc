OS_RELEASE_FIELDS = "\
    NAME VERSION BUILD_IMAGE_METADATA \
"
# Actual value is set in BSP meta.
HAILO_PLATFORM_NAME ??= "Hailo-Platform"

NAME = "${HAILO_PLATFORM_NAME}"
VERSION = "1.11.0-dv-LGL_34"
BUILD_IMAGE_METADATA = "/etc/build-info"
