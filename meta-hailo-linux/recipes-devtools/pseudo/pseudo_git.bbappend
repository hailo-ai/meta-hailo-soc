FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Host tar carrying the CVE-2025-45582 fix uses the openat2() syscall, which
# pseudo 1.9.0 does not intercept. The directory fd tar obtains that way is
# never registered in pseudo's fd table, so pseudo returns EFAULT and every
# fakeroot task fails with "Cannot open: Bad address". Matches the pseudo
# revision carried by poky kirkstone b5f43fb19e59 (yocto-4.0.34).
SRCREV = "43cbd8fb4914328094ccdb4bb827d74b1bac2046"
PV = "1.9.3+git"

# Both are part of pseudo as of 1.9.1 and no longer apply to this SRCREV.
SRC_URI:remove = "file://0001-configure-Prune-PIE-flags.patch file://glibc238.patch"
