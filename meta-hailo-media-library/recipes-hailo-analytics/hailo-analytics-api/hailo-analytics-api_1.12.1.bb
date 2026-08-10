DESCRIPTION = "Hailo Analytics package recipe \
               compiles hailo analytics library and copies shared objects to usr/lib/ "

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "git://git@github.com/hailo-ai/hailo-media-library.git;protocol=https;branch=1.12.1"
SRCREV = "19571509768f7005665df04645d2ad31e5b791a6"

inherit media-library-base media-library-downloader

S = "${WORKDIR}/git"

# Meson source path - the hailo-analytics subdirectory contains the meson.build
MESON_SOURCEPATH = "${S}/hailo-analytics"

# Map PACKAGECONFIG to download targets dynamically
# This ensures we only download HEFs/resources for apps we're actually building
python set_download_targets() {
    packageconfig = d.getVar('PACKAGECONFIG') or ''
    targets = []

    # App-level mappings (match download_requirements.yaml target names)
    app_map = {
        'face-landmarks': 'face_landmarks',
        'clip': 'clip',
        'case-studies': 'dynamic_privacy_mask',
        'lpr': 'license_plate_recognition',
        # Add more apps as they get HEF requirements in download_requirements.yaml
    }

    for config_key, target_name in app_map.items():
        if config_key in packageconfig:
            targets.append(target_name)

    # 'shared' holds detection HEFs (yolov8s/n_384_640) consumed by every app above
    shared_consumers = {'face-landmarks', 'clip', 'webserver', 'case-studies', 'lpr'}
    if any(consumer in packageconfig for consumer in shared_consumers):
        targets.append('shared')

    if targets:
        d.setVar('DOWNLOAD_TARGET', ','.join(targets))
        bb.note(f"Download targets: {','.join(targets)}")
    else:
        # No apps enabled = skip downloads entirely (just building libhailo_analytics.so)
        d.setVar('DOWNLOAD_TARGET', '')
}

# Call before do_fetch_requirements to set DOWNLOAD_TARGET based on PACKAGECONFIG
do_fetch_requirements[prefuncs] += "set_download_targets"

DEPENDS:append = " \
    libmedialib \
    hailo-postprocess-tools \
    cxxopts \
    libdatachannel \
    libhailopp \
    yaml-cpp \
    protobuf \
    protobuf-native \
    "

# PACKAGECONFIG for selective app building
#
# Default: Conditional based on DISTRO_FEATURES
#   - hailo-core (production): face-landmarks only (minimal footprint)
#   - hailo-dev-pkg (development): all apps, case studies, and verification tests
#
# External users can override via kas/local.conf:
#   Full override:  PACKAGECONFIG:pn-hailo-analytics-api = "clip webserver"
#   Append:         PACKAGECONFIG:append:pn-hailo-analytics-api = " native"
#   Remove:         PACKAGECONFIG:remove:pn-hailo-analytics-api = "face-landmarks"

# Production apps (minimal footprint)
PRODUCTION_APPS = "face-landmarks native"

# Dev package composition (full development image)
DEFAULT_DEV_PKG_APPS = "${PRODUCTION_APPS} case-studies clip lpr webserver vlm-event-monitor"
DEFAULT_INFRA = "verification"
DEV_PACKAGECONFIG = "${DEFAULT_DEV_PKG_APPS} ${DEFAULT_INFRA}"

# Set default based on DISTRO_FEATURES
PACKAGECONFIG ??= "${@bb.utils.contains('DISTRO_FEATURES', 'hailo-core', '${PRODUCTION_APPS}', '${DEV_PACKAGECONFIG}', d)}"

# App Groups
PACKAGECONFIG[case-studies] = "-Dbuild_case_studies=true,-Dbuild_case_studies=false"
PACKAGECONFIG[face-landmarks] = "-Dbuild_face_landmarks=true,-Dbuild_face_landmarks=false"
PACKAGECONFIG[clip] = "-Dbuild_clip=true,-Dbuild_clip=false,libfaiss ffmpeg hailort-server httplib"
PACKAGECONFIG[lpr] = "-Dbuild_lpr=true,-Dbuild_lpr=false"
PACKAGECONFIG[webserver] = "-Dbuild_webserver=true,-Dbuild_webserver=false,httplib"
PACKAGECONFIG[native] = "-Dbuild_native=true,-Dbuild_native=false"
PACKAGECONFIG[vlm-event-monitor] = "-Dbuild_vlm_event_monitor=true,-Dbuild_vlm_event_monitor=false,httplib yaml-cpp libjpeg-turbo"

# Verification (unit tests + test apps)
PACKAGECONFIG[verification] = "-Dbuild_verification=true,-Dbuild_verification=false,googletest"

# Perfetto tracing: off by default, enable for dev images via PACKAGECONFIG:append:pn-hailo-analytics-api = " perfetto"
PACKAGECONFIG[perfetto] = "-Dperfetto=true,-Dperfetto=false,libperfetto,libperfetto"

RDEPENDS:${PN} += " bash"

EXTRA_OEMESON += " \
        -Dapps_install_dir='/home/root/apps' \
        -Dstrip=true \
        "

FILES:${PN} += " \
    ${libdir}/libhailo_analytics.so* \
    /home/root/apps/* \
    ${@bb.utils.contains('DISTRO_FEATURES', 'hailo-dev-pkg', '/home/root/tests/*', '', d)} \
    "

do_install:append() {
    export DESTDIR="${D}"
    ninja -C ${B} install

    install -d ${D}/home/root/apps
    install -m 0755 ${S}/tools/gst_apps/manage_config_tuning.sh \
        ${D}/home/root/apps/manage_config_tuning.sh
}

INSANE_SKIP:${PN} += "already-stripped"
