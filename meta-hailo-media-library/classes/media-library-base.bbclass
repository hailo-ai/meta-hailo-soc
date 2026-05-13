# media-library base class - setting the base configuration for meson (target, type, includes etc...)
# depends on

inherit meson pkgconfig

S = "${WORKDIR}/git"

PACKAGECONFIG ??= ""
PACKAGECONFIG[asan] = ",,gcc-sanitizers,"

MEDIA_LIBRARY_BUILD_TYPE = "${@bb.utils.contains('PACKAGECONFIG', 'asan', 'debug', 'release', d)}"
PARALLEL_MAKE = "-j ${@min(int(oe.utils.cpu_count()), 8)}"

EXTRA_OEMESON += " \
        --buildtype='${MEDIA_LIBRARY_BUILD_TYPE}' \
        ${@bb.utils.contains('PACKAGECONFIG', 'asan', \
            '-Dcpp_args=-fsanitize=hwaddress -Dcpp_link_args=-fsanitize=hwaddress -Dc_args=-fsanitize=hwaddress -Dc_link_args=-fsanitize=hwaddress', \
            '', d)} \
        "

EXTRA_OEMESON:append:hailo15l = " \
        -Dplatform='15l' \
        "

DEPENDS:append = " opencv spdlog"

# Skip ldflags QA check when building with HWASan - the sanitizer linker flags
# override Yocto's default LDFLAGS causing missing GNU_HASH in binaries
INSANE_SKIP:${PN}:append = "${@bb.utils.contains('PACKAGECONFIG', 'asan', ' ldflags', '', d)}"
