SUMMARY = "Hailo Sensor Drivers and Symlinks for Imaging Subsystem"
DESCRIPTION = "This recipe compiles the available sensor drivers, installs them in the target filesystem, and generates the required symbolic links (.drv) to meet the expectations of SensorN_config.json."
LICENSE = "CLOSED"

SRC_URI = "git://git@github.com/hailo-ai/hailo-imaging.git;protocol=https;branch=1.7.3-dv-1"
SRCREV = "7d517e896531130ca2d98c5c5db997924fef9700"
S = "${WORKDIR}/git"
ISS_LIBS_DIR = "${S}/imaging-sub-system/scripts/units/isi/drv"

inherit cmake

# The env variable ISS_BUILD tells the cmake that it shouldn't do certain copies
# that are needed when building everything as a whole
# In other words, it customizes cmake for separate build for ISS drivers only
EXTRA_OECMAKE = " -DDUMMY_BUILD=0 -DLIB_ROOT=${STAGING_DIR_TARGET}/usr/include/imaging -DLOCAL=1 -DISS_BUILD=1 "

SENSORS_LIBS ?= "HAILO_IMX334 HAILO_IMX664 HAILO_IMX675 HAILO_IMX678 HAILO_IMX715 HAILO_IMX_DUMMY"

do_configure() {
    for lib in ${SENSORS_LIBS}; do
        cmake ${ISS_LIBS_DIR}/${lib} -B${B}/${lib} ${EXTRA_OECMAKE}
    done
}

do_compile() {
    for lib in ${SENSORS_LIBS}; do
        bbnote "Compiling sensor: ${lib}"
        cmake --build ${B}/${lib}
    done
}

do_install() {
    install -d ${D}/lib/
    install -d ${D}/${bindir}/
    
    for iss_lib in ${SENSORS_LIBS}; do
        # Copy original library (.so file)
        LIB_FILE=$(basename $(find ${B}/${iss_lib}/ -type f -name "libHAILO*.so*"))
        if ! [ -e ${B}/${iss_lib}/${LIB_FILE} ]; then
            echo "ERROR: could not find lib .so file! for ${iss_lib}"
            exit 1
        fi
        install -m 0755 ${B}/${iss_lib}/${LIB_FILE} ${D}/lib/${LIB_FILE}

        # Link to the library without version
        LINK_FILE=$(basename $(find ${B}/${iss_lib}/ -type l -name "libHAILO_*.so"))
        ln -sf /lib/${LIB_FILE} ${D}/lib/${LINK_FILE}

        # Add .drv link to library
        ln -sf /lib/${LIB_FILE} ${D}/${bindir}/${iss_lib}.drv

	    # Install the XML calibration files
	    for xmlfile in ${ISS_LIBS_DIR}/${iss_lib}/calib/*/*.xml; do
	        install -m 0644 ${xmlfile} ${D}/${bindir}/
	    done
    done
}

DEPENDS =+ " imaging-sub-system-ext "

FILES_${PN} += "/lib/*.so* ${bindir}/*.drv ${bindir}/*.xml"
INSANE_SKIP:${PN} =  "file-rpaths dev-so debug-files rpaths staticdev installed-vs-shipped"

