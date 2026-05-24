SUMMARY = "Perfetto - System profiling, app tracing and trace analysis."
HOMEPAGE = "https://github.com/google/perfetto"

# Add only patches here which are used by both perfetto and libperfetto (currently there are none)
SRC_URI = "${PERFETTO_URI};branch=${PERFETTO_BRANCH};name=perfetto"

# original Google repo URI is "git://github.com/google/perfetto.git;protocol=https"
PERFETTO_URI ??=  "git://git@github.com/hailo-ai/Hailo-Soc-Profiler.git;protocol=ssh;"
PERFETTO_BRANCH ??=  "1.12.0-dv-5"
PERFETTO_SRCREV ??=  "554a71c691b4d31ea156df1f4383558dff2d11a0"

SRCREV_perfetto = "${PERFETTO_SRCREV}"
SRCREV_FORMAT = "perfetto"
PV = "49.0"

S = "${WORKDIR}/git"
