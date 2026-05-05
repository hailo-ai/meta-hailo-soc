# media-library-downloader.bbclass
#
# Resource management for Media Library applications.
#
# This class downloads requirement files (HEFs, BINs, JSONs, etc.) using the
# download_requirements.py script from the media-library source tree.
# The script reads connection settings and file checksums from
# download_requirements.yaml.
#
# Cache invalidation is based on SRCREV + platform + target. When media-library
# SRCREV changes, the cache is invalidated and files are re-downloaded.
#

LICENSE = "MIT"

# =============================================================================
# Configuration
# =============================================================================
# Target to download - leave empty for --all, or set to specific target name from tools/download_reqs/download_requirements.yaml
DOWNLOAD_TARGET ?= ""

# Installation path on target
DOWNLOAD_INSTALL_ROOT ?= "/home/root/apps"

# Subdirectory in WORKDIR for downloaded files
DOWNLOAD_EXTRACT_SUBDIR = "download-extract"

# Path to download script relative to media-library source
DOWNLOAD_SCRIPT ?= "tools/download_reqs/download_requirements.py"

# Platform mapping: hailo15 -> hailo15h, hailo15l stays as-is
DOWNLOAD_PLATFORM = "${@'hailo15h' if d.getVar('HAILO_SOC_NAME') == 'hailo15' else d.getVar('HAILO_SOC_NAME')}"

# =============================================================================
# Download Task
# =============================================================================

python do_fetch_requirements() {
    import os
    import subprocess

    # Get variables
    platform = d.getVar('DOWNLOAD_PLATFORM')
    target = d.getVar('DOWNLOAD_TARGET') or ''
    workdir = d.getVar('WORKDIR')
    s_dir = d.getVar('S') + '/../'
    extract_subdir = d.getVar('DOWNLOAD_EXTRACT_SUBDIR')
    script_rel_path = d.getVar('DOWNLOAD_SCRIPT')

    # Skip download if no targets configured (e.g., building only library)
    if not target:
        bb.note("Downloader: No download targets configured, skipping resource downloads")
        return

    target_key = target if target else 'all'
    download_dest = os.path.join(workdir, extract_subdir)

    bb.note(f"Downloader: platform={platform}, target={target_key}")

    # -------------------------------------------------------------------------
    # Find and run download script
    # -------------------------------------------------------------------------
    script_path = os.path.join(s_dir, script_rel_path)
    if not os.path.exists(script_path):
        bb.fatal(f"Downloader: Download script not found: {script_path}")

    # Create destination directory
    os.makedirs(download_dest, exist_ok=True)

    # Get native Python path from BitBake environment
    native_sysroot = d.getVar('STAGING_DIR_NATIVE')
    python_path = os.path.join(native_sysroot, 'usr', 'bin', 'python3-native', 'python3')
    if not os.path.exists(python_path):
        python_path = 'nativepython3'

    # Build command: --all for all targets, or --target <name> for specific
    cmd = [python_path, script_path]
    if target:
        cmd.extend(['--target', target])
    else:
        cmd.append('--all')
    cmd.extend(['--platform', platform, '--workspace-root', download_dest])

    bb.note(f"Downloader: Running {' '.join(cmd)}")

    try:
        result = subprocess.run(
            cmd,
            check=True,
            capture_output=True,
            text=True,
            cwd=s_dir
        )
        if result.stdout:
            for line in result.stdout.strip().split('\n'):
                bb.note(f"Downloader: {line}")
    except subprocess.CalledProcessError as e:
        bb.error(f"Downloader: Download script failed with exit code {e.returncode}")
        if e.stdout:
            bb.error("Downloader: STDOUT:")
            for line in e.stdout.strip().split('\n'):
                bb.error(f"Downloader: {line}")
        if e.stderr:
            bb.error("Downloader: STDERR:")
            for line in e.stderr.strip().split('\n'):
                bb.error(f"Downloader: {line}")
        bb.fatal(f"Downloader: Download failed with exit code {e.returncode}")

    bb.note("Downloader: Download complete")
}

do_fetch_requirements[network] = "1"
do_fetch_requirements[depends] += "rsync-native:do_populate_sysroot wget-native:do_populate_sysroot python3-native:do_populate_sysroot python3-pyyaml-native:do_populate_sysroot"

addtask fetch_requirements after do_unpack before do_configure

# =============================================================================
# Installation
# =============================================================================

fakeroot do_install_requirements() {
    local EXTRACT_DIR="${WORKDIR}/${DOWNLOAD_EXTRACT_SUBDIR}"
    local TARGET_DIR="${D}${DOWNLOAD_INSTALL_ROOT}"

    if [ ! -d "${EXTRACT_DIR}" ] || [ -z "$(ls -A ${EXTRACT_DIR} 2>/dev/null)" ]; then
        bbnote "Downloader: No downloaded resources to install"
        return 0
    fi

    bbnote "Downloader: Installing resources to ${TARGET_DIR}"

    install -d "${TARGET_DIR}"

    # Fix permissions locally in WORKDIR before moving to ${D}
    # This ensures we don't touch files installed by Meson in ${D}
    find "${EXTRACT_DIR}" -type d -exec chmod 0755 {} \;
    find "${EXTRACT_DIR}" -type f -exec chmod 0644 {} \;

    # Use cp -a to preserve the 0755/0644 bits we just set
    # -a is (archive) which is -dR --preserve=all
    cp -a "${EXTRACT_DIR}"/. "${TARGET_DIR}/"

    bbnote "Downloader: Installation complete and scoped to downloaded assets"
}

do_install_requirements[depends] += "virtual/fakeroot-native:do_populate_sysroot"

addtask install_requirements after do_install before do_package
