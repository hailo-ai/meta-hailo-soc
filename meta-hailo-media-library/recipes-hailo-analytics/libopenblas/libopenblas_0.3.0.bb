
DESCRIPTION = "OpenBLAS is an optimized BLAS library based on GotoBLAS2 1.13 BSD version."
HOMEPAGE = "http://www.openblas.net/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5adf4792c949a00013ce25d476a2abc0"

# The RDEPENDS on libgomp is only needed if USE_OPENMP is enabled
# which is a good default for performance.
RDEPENDS:${PN} += "libgomp"

SRCREV = "993fad6aebbce34a97d3f8c34d6d79d35b64cc48"
SRC_URI = "git://github.com/xianyi/OpenBLAS.git;protocol=https;branch=release-0.3.0"

S = "${WORKDIR}/git"

inherit siteinfo

# Map Yocto's TUNE_ARCH to OpenBLAS's TARGET architecture.
# Add other architectures here if you build for more than armv8.
OPENBLAS_TARGET:aarch64 = "ARMV8"
OPENBLAS_TARGET:armv7a = "ARMV7"
OPENBLAS_TARGET:x86-64 = "HASWELL"
OPENBLAS_TARGET ?= "GENERIC"

EXTRA_OEMAKE += " \
    TARGET=${OPENBLAS_TARGET} \
    BINARY=${SITEINFO_BITS} \
    NOFORTRAN=1 \
    USE_OPENMP=1 \
    HOSTCC="${BUILD_CC}" \
    CC="${CC}" \
    AR="${AR}" \
    LD="${LD}" \
    FC= \
    CROSS_SUFFIX=${HOST_PREFIX} \
    PREFIX="${prefix}" \
    DESTDIR="${D}" \
"

# FORCE_TARGET=1 can be used to bypass OpenBLAS's auto-detection script,
# which is good for cross-compilation.
EXTRA_OEMAKE += "FORCE_TARGET=1"

# The default 'make' target builds the necessary libraries.
do_compile() {
	oe_runmake
}

do_install() {
	oe_runmake install
    # OpenBLAS might create an empty bin directory, which can cause packaging QA issues.
    # This command removes it if it's empty.
	if [ -d ${D}${bindir} ]; then
		rmdir --ignore-fail-on-non-empty ${D}${bindir}
	fi
}

# Due to OpenBLAS's non-standard naming, we must be explicit.
# By using '=', we overwrite Yocto's defaults, which prevents them
# from mis-packaging our files.

# The main package gets the REAL shared library and the versioned soname symlink.
# These are required for runtime
FILES:${PN} = " \
    ${libdir}/libopenblas.so.* \
    ${libdir}/libopenblas_*.so \
"

# The -dev package gets headers and the UNVERSIONED symlink for linking.
FILES:${PN}-dev = " \
    ${includedir} \
    ${libdir}/libopenblas.so \
    ${libdir}/pkgconfig \
    ${libdir}/cmake \
"

# The -staticdev package gets all .a files (the real one and the symlink).
FILES:${PN}-staticdev = "${libdir}/*.a"