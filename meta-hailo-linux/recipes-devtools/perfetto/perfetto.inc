SUMMARY = "Perfetto - System profiling, app tracing and trace analysis."
HOMEPAGE = "https://github.com/google/perfetto"

# Add only patches here which are used by both perfetto and libperfetto (currently there are none)
SRC_URI = "${PERFETTO_URI};nobranch=1;name=perfetto"

# original Google repo URI is "git://github.com/google/perfetto.git;protocol=https"
PERFETTO_URI ??=  "git://git@github.com/hailo-ai/Hailo-Soc-Profiler.git;protocol=ssh;"
PERFETTO_SRCREV ??=  "997ac30c3d3ada66c5cad70d8d9a94533c2dd4ac"

SRCREV_perfetto = "${PERFETTO_SRCREV}"
SRCREV_FORMAT = "perfetto"
PV = "49.0"

S = "${WORKDIR}/git"
