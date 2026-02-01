# media-library base class - setting the base configuration for meson (target, type, includes etc...)
# depends on 

inherit meson pkgconfig

S = "${WORKDIR}/git"

MEDIA_LIBRARY_BUILD_TYPE = "release"
PARALLEL_MAKE = "-j 4"

EXTRA_OEMESON += " \
        -Dcpp_std='c++20' \
        --buildtype='${MEDIA_LIBRARY_BUILD_TYPE}' \
        "

EXTRA_OEMESON:append:hailo15l = " \
        -Dplatform='15l' \
        "

DEPENDS:append = " opencv spdlog"

# Add libperfetto as a PACKAGECONFIG option, off by default, enabled only for dev images
PACKAGECONFIG ??= ""
PACKAGECONFIG[perfetto] = ",,libperfetto,libperfetto"
