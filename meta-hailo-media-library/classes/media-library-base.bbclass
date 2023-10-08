# media-library base class - setting the base configuration for meson (target, type, includes etc...)
# depends on 

inherit meson pkgconfig

S = "${WORKDIR}/git"

MEDIA_LIBRARY_BUILD_TARGET = "all"
MEDIA_LIBRARY_BUILD_TYPE = "release"
PARALLEL_MAKE = "-j 4"

EXTRA_OEMESON += " \
        -Dcpp_std='c++17' \
        -Dtargets='${MEDIA_LIBRARY_BUILD_TARGET}' \
        --buildtype='${MEDIA_LIBRARY_BUILD_TYPE}' \
        "
