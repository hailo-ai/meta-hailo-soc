SUMMARY = "Hailo Camera Configuration Files"
DESCRIPTION = "Configuration files for Hailo camera imaging pipeline including schemas and rules for validation"

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

SRC_URI = "git://git@github.com/hailo-ai/hailo-camera-configurations.git;protocol=https;branch=0.0.0.LGL-dv-LGL_26"
SRCREV = "326f39e3bfd115a861009d1c602e07adfb97262b"

S = "${WORKDIR}/git"

# Meson build is in the validation/rule_checker_lib subdirectory
MESON_SOURCEPATH = "${S}/validation/rule_checker_lib"

# Build dependencies
DEPENDS = "meson-native ninja-native pkgconfig-native nlohmann-json"

inherit meson

# If you have meson options 'build_tests' / 'build_bindings', pass them here:
EXTRA_OEMESON += "-Dbuild_tests=false -Dbuild_bindings=false"

# Override meson_do_configure to run from the subdirectory
meson_do_configure() {
    bbnote "Configuring Meson project from ${S}/validation/rule_checker_lib"
    meson setup ${MESONOPTS} "${S}/validation/rule_checker_lib" "${B}" ${MESON_CROSS_FILE} ${EXTRA_OEMESON}
}

do_install() {
    # Install meson-built library
    DESTDIR=${D} ninja -C ${B} install

    # Install header files for medialib to use
    install -d ${D}${includedir}/rule_checker
    install -m 0644 ${S}/validation/rule_checker_lib/include/*.h ${D}${includedir}/rule_checker/

    # Create base directory structure
    install -d ${D}${sysconfdir}/imaging
    install -d ${D}${sysconfdir}/imaging/validation/hailo15h/schemas
    install -d ${D}${sysconfdir}/imaging/validation/hailo15h/rules
    install -d ${D}${sysconfdir}/imaging/validation/hailo15l/schemas
    install -d ${D}${sysconfdir}/imaging/validation/hailo15l/rules

    # Install validation schemas for hailo15h
    if [ -d "${S}/validation/hailo15h/schemas" ]; then
        install -m 0644 ${S}/validation/hailo15h/schemas/*.json ${D}${sysconfdir}/imaging/validation/hailo15h/schemas/ || true
    fi

    # Install validation rules for hailo15h
    if [ -d "${S}/validation/hailo15h/rules" ]; then
        install -m 0644 ${S}/validation/hailo15h/rules/*.yaml ${D}${sysconfdir}/imaging/validation/hailo15h/rules/ || true
    fi

    # Install validation schemas for hailo15l
    if [ -d "${S}/validation/hailo15l/schemas" ]; then
        install -m 0644 ${S}/validation/hailo15l/schemas/*.json ${D}${sysconfdir}/imaging/validation/hailo15l/schemas/ || true
    fi

    # Install validation rules for hailo15l
    if [ -d "${S}/validation/hailo15l/rules" ]; then
        install -m 0644 ${S}/validation/hailo15l/rules/*.yaml ${D}${sysconfdir}/imaging/validation/hailo15l/rules/ || true
    fi

    # Install entire cfg directory tree
    # Exclude setup_hailo_sensor.sh as it goes to /usr/bin
    if [ -d "${S}/configurations_tree/cfg" ]; then
        cp -r ${S}/configurations_tree/cfg ${D}${sysconfdir}/imaging/
        # Set proper permissions for all files
        find ${D}${sysconfdir}/imaging/cfg -type f -exec chmod 644 {} \;
        find ${D}${sysconfdir}/imaging/cfg -type d -exec chmod 755 {} \;
    fi

    # Create platform-specific symlink for medialib_configs
    # Default to hailo15h if HAILO_PLATFORM not set
    PLATFORM="${HAILO_PLATFORM}"
    if [ -z "$PLATFORM" ]; then
        PLATFORM="15h"
    fi
    
    if [ "$PLATFORM" = "15h" ]; then
        DEFAULT_MEDIALIB_CONFIGS="hailo15h/imx678/theia_sl410m/4k/medialib_configs"
    else
        DEFAULT_MEDIALIB_CONFIGS="hailo15l/imx675/theia_sl410m/5mp/medialib_configs"
    fi
    
    # Create the symlink
    if [ -d "${D}${sysconfdir}/imaging/cfg/$DEFAULT_MEDIALIB_CONFIGS" ]; then
        ln -sf "$DEFAULT_MEDIALIB_CONFIGS" ${D}${sysconfdir}/imaging/cfg/medialib_configs
    fi

    # Install setup_hailo_sensor.sh to /usr/bin (note: script name might differ)
    if [ -f "${S}/configurations_tree/setup_hailo_sensor.sh" ]; then
        install -d ${D}${bindir}
        install -m 0755 ${S}/configurations_tree/setup_hailo_sensor.sh ${D}${bindir}/
    elif [ -f "${S}/configurations_tree/cfg/setup_hailo_sensor.sh" ]; then
        install -d ${D}${bindir}
        install -m 0755 ${S}/configurations_tree/cfg/setup_hailo_sensor.sh ${D}${bindir}/
    fi
}

FILES:${PN} += "${libdir}/*.so.* ${sysconfdir}/imaging/cfg/* ${sysconfdir}/imaging/validation/* ${bindir}/setup_hailo_sensor.sh"
FILES:${PN}-dev = "${includedir}/* ${libdir}/*.so"

# Runtime dependencies
RDEPENDS:${PN} = "bash"
ALLOW_EMPTY:${PN} = "1"


