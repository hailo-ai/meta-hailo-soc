# media-library base class - setting the base configuration for meson (target, type, includes etc...)
# depends on

inherit meson pkgconfig

S = "${WORKDIR}/git"

MEDIA_LIBRARY_BUILD_TYPE = "release"
PARALLEL_MAKE = "-j ${@min(int(oe.utils.cpu_count()), 8)}"

EXTRA_OEMESON += " \
        -Dcpp_std='c++20' \
        --buildtype='${MEDIA_LIBRARY_BUILD_TYPE}' \
        "

EXTRA_OEMESON:append:hailo15l = " \
        -Dplatform='15l' \
        "

DEPENDS:append = " opencv spdlog"
