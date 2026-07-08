SUMMARY = "Perfetto - System profiling, app tracing and trace analysis."
HOMEPAGE = "https://github.com/google/perfetto"

# Add only patches here which are used by both perfetto and libperfetto (currently there are none)
SRC_URI = "${PERFETTO_URI};branch=${PERFETTO_BRANCH};name=perfetto"

# original Google repo URI is "git://github.com/google/perfetto.git;protocol=https"
PERFETTO_URI ??=  "git://git@github.com/hailo-ai/Hailo-Soc-Profiler.git;protocol=ssh;"
PERFETTO_BRANCH ??=  "1.12.1-dv-2"
PERFETTO_SRCREV ??=  "ab23d9b2085ac4def6a359a204813c971355e98e"

SRCREV_perfetto = "${PERFETTO_SRCREV}"
SRCREV_FORMAT = "perfetto"
PV = "49.0"

S = "${WORKDIR}/git"
