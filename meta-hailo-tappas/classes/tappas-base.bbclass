# tappas base class - setting the base configuration for meson (target, type, includes etc...)
# deppends on meta-hailo-libhailort recipes

inherit meson pkgconfig

S = "${WORKDIR}/git/"

DEPENDS += "libgsthailo libhailort"

TAPPAS_BUILD_TYPE = "release"
PARALLEL_MAKE = "-j 4"

GST_HAILO_INCLUDE_DIR = "${STAGING_INCDIR}/gst-hailo/metadata"
HAILO_INCLUDE_DIR = "${STAGING_INCDIR}/hailort"

EXTRA_OEMESON += " \
        -Dlibargs='-I${GST_HAILO_INCLUDE_DIR},-I${HAILO_INCLUDE_DIR}' \
        -Dcpp_std='c++17' \
        --buildtype='${TAPPAS_BUILD_TYPE}' \
        "
# Add libperfetto as a PACKAGECONFIG option, off by default, enabled only for dev images
PACKAGECONFIG ??= ""
PACKAGECONFIG[perfetto] = ",,libperfetto,libperfetto"
