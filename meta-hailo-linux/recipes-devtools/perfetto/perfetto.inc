SUMMARY = "Perfetto - System profiling, app tracing and trace analysis."
HOMEPAGE = "https://github.com/google/perfetto"

# Add only patches here which are used by both perfetto and libperfetto (currently there are none)
SRC_URI = "${PERFETTO_URI};branch=${PERFETTO_BRANCH};name=perfetto"

# original Google repo URI is "git://github.com/google/perfetto.git;protocol=https"
PERFETTO_URI ??=  "git://git@github.com/hailo-ai/Hailo-Soc-Profiler.git;protocol=ssh;"
PERFETTO_BRANCH ??=  "1.12.0-dv-4"
PERFETTO_SRCREV ??=  "169e9b8861315a5697bcee4d50a9cecd1f27b8d6"

SRCREV_perfetto = "${PERFETTO_SRCREV}"
SRCREV_FORMAT = "perfetto"
PV = "49.0"

S = "${WORKDIR}/git"
