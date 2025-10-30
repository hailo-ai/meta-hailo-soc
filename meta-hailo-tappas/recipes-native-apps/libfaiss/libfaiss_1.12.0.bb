SUMMARY = "A library for efficient similarity search and clustering of dense vectors"
DESCRIPTION = "Faiss is a library for efficient similarity search and clustering of dense vectors. \
It contains algorithms that search in sets of vectors of any size, up to ones that possibly do not \
fit in RAM. It also contains supporting code for evaluation and parameter tuning."
HOMEPAGE = "https://github.com/facebookresearch/faiss"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=901f6cd9846257b3a9c69dbd0a49caf1"

# libopenblas is for linking at build-time.
# cmake-native provides the cmake tool needed by the build process.
DEPENDS = "libopenblas cmake-native"

# Use a specific git tag for a reproducible build instead of a branch.
SRC_URI = "git://github.com/facebookresearch/faiss.git;protocol=https;branch=main"
# For v1.12.0 tag
SRCREV = "e8234e563f1ecef5f036e83c3cfee366d3f1fbca"


S = "${WORKDIR}/git"

inherit cmake

# FAISS configuration options
EXTRA_OECMAKE = " \
    -DBLA_VENDOR=OpenBLAS \
    -DFAISS_ENABLE_GPU=OFF \
    -DFAISS_ENABLE_PYTHON=OFF \
    -DFAISS_ENABLE_C_API=ON \
    -DBUILD_TESTING=OFF \
    -DCMAKE_BUILD_TYPE=Release \
    -DBUILD_SHARED_LIBS=ON \
"

do_configure:prepend() {
    # Lower CMake minimum requirement to match CI environment (3.22.0 available)
    sed -i 's/cmake_minimum_required(VERSION 3\.24\.0 FATAL_ERROR)/cmake_minimum_required(VERSION 3.22.0 FATAL_ERROR)/' ${S}/CMakeLists.txt
}

# Use '=' to overwrite defaults and take full control due to non-standard file names.
# The main package gets ALL shared library files (.so files)
FILES:${PN} = "${libdir}/libfaiss*.so*"

# The -dev package gets headers, and cmake/pkgconfig files
FILES:${PN}-dev = " \
    ${includedir} \
    ${libdir}/pkgconfig \
    ${datadir}/faiss \
"

# The -staticdev package gets any static libraries that might be built.
FILES:${PN}-staticdev = "${libdir}/*.a"

# dev package requires the main package
RDEPENDS:${PN}-dev = "${PN}"

# At runtime, the faiss package needs the openblas package to be present.
RDEPENDS:${PN} += "libopenblas"

