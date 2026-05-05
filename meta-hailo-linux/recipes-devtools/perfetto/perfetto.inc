SUMMARY = "Perfetto - System profiling, app tracing and trace analysis."
HOMEPAGE = "https://github.com/google/perfetto"

# Add only patches here which are used by both perfetto and libperfetto (currently there are none)
SRC_URI = "${PERFETTO_URI};branch=${PERFETTO_BRANCH};name=perfetto"

# original Google repo URI is "git://github.com/google/perfetto.git;protocol=https"
PERFETTO_URI ??=  "git://git@github.com/hailo-ai/Hailo-Soc-Profiler.git;protocol=ssh;"
PERFETTO_BRANCH ??=  "0.0.0.LGL-dv-LGL_14"
PERFETTO_SRCREV ??=  "55da2ee84d148f2bd5b3e267d4d74e8a08a11c2a"

SRCREV_perfetto = "${PERFETTO_SRCREV}"
SRCREV_FORMAT = "perfetto"
PV = "49.0"

S = "${WORKDIR}/git"
