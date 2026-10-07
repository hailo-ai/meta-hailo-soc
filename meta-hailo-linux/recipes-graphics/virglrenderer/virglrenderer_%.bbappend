# anongit.freedesktop.org accepts git:// connections but never answers, so the
# fetch hangs before bitbake can fall back to MIRRORS. Fetch from gitlab first.
PREMIRRORS:prepend = "git://anongit.freedesktop.org/git/virglrenderer git://gitlab.freedesktop.org/virgl/virglrenderer.git;protocol=https \n"
