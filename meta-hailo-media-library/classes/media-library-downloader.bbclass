# Generic manifest-based downloader+installer (Option B, close to tappas-apps-base)
#
# Manifest line format:
#   <url> -> <app_path> -> <md5>
#
# The inheriting recipe MUST implement:
#   python set_reqs_file() { d.setVar('REQS_FILE', '...'); ... }
#
# And should set FILES:${PN} to include FILES_INSTALL_ROOT.

LICENSE = "MIT"

# Where to install on target (without ${D})
FILES_INSTALL_ROOT ?= "/home/root/apps"

# Token used for the downloaded filename suffix to avoid collisions
# (tappas uses ${HAILO_SOC_NAME}; generic default is ${MACHINE})
FILES_INSTALL_TOKEN ?= "${MACHINE}"

# Optional: base directory containing <app_name> subdirs with scripts/configs
# Example layout:
#   ${FILES_INSTALL_SCRIPTS_DIR}/ai_example_app/*.sh
#   ${FILES_INSTALL_SCRIPTS_DIR}/ai_example_app/configs/*
FILES_INSTALL_SCRIPTS_DIR ?= ""

FILES_INSTALL_COPY_SCRIPTS ?= "1"
FILES_INSTALL_COPY_CONFIGS ?= "1"

# Manifest file path; set by recipe via set_reqs_file()
REQS_FILE ?= ""

CURRENT_APP_NAME = ""
CURRENT_REQ_FILE = ""

REQS_PATH = "${FILE_DIRNAME}/files/"
REQS_HAILO15_FILE = "${REQS_PATH}download_reqs_${HAILO_SOC_NAME}.txt"

addtask install_requirements after do_install before do_package

do_fetch[prefuncs] += "do_set_requirements_src_uris"
do_unpack[prefuncs] += "do_set_requirements_src_uris"
do_cleanstate[prefuncs] += "do_set_requirements_src_uris"
do_cleanall[prefuncs] += "do_set_requirements_src_uris"
do_clean[prefuncs] += "do_set_requirements_src_uris"

do_install_requirements[depends] += "virtual/fakeroot-native:do_populate_sysroot"

fakeroot install_app_dir() {
    dest_root="${D}${FILES_INSTALL_ROOT}"

    install -d "${dest_root}/${CURRENT_APP_NAME}"
    install -d "${dest_root}/${CURRENT_APP_NAME}/resources"

    # Convert WORKDIR downloaded filename back to original name by stripping _${FILES_INSTALL_TOKEN}
    # Supports hef/bin/json
    orig_filename=$(echo "${CURRENT_REQ_FILE}" | sed -E "s/_${FILES_INSTALL_TOKEN}\.(hef|bin|json)/.\1/")
    install -m 0644 "${WORKDIR}/${CURRENT_REQ_FILE}" \
        "${dest_root}/${CURRENT_APP_NAME}/resources/${orig_filename}"

    # Optional scripts/configs
    if [ "${FILES_INSTALL_COPY_SCRIPTS}" = "1" ] && [ -n "${FILES_INSTALL_SCRIPTS_DIR}" ]; then
        if ls "${FILES_INSTALL_SCRIPTS_DIR}/${CURRENT_APP_NAME}"/*.sh >/dev/null 2>&1; then
            install -m 0755 "${FILES_INSTALL_SCRIPTS_DIR}/${CURRENT_APP_NAME}"/*.sh \
                "${dest_root}/${CURRENT_APP_NAME}"
        else
            bbnote ".sh file not found for ${CURRENT_APP_NAME}, skipping"
        fi
    fi

    if [ "${FILES_INSTALL_COPY_CONFIGS}" = "1" ] && [ -n "${FILES_INSTALL_SCRIPTS_DIR}" ]; then
        if [ -d "${FILES_INSTALL_SCRIPTS_DIR}/${CURRENT_APP_NAME}/configs" ]; then
            install -d "${dest_root}/${CURRENT_APP_NAME}/resources/configs"
            install -m 0644 "${FILES_INSTALL_SCRIPTS_DIR}/${CURRENT_APP_NAME}/configs/"* \
                "${dest_root}/${CURRENT_APP_NAME}/resources/configs" || true
        fi
    fi
}

python do_set_requirements_src_uris() {
    import os

    # Recipe chooses REQS_FILE (+ optionally FILES_INSTALL_SCRIPTS_DIR) here
    bb.build.exec_func("set_reqs_file", d)

    reqs_path = d.getVar("REQS_FILE")
    if not reqs_path:
        bb.fatal("REQS_FILE is not set. Implement set_reqs_file() in your recipe and set REQS_FILE.")

    token = d.getVar("FILES_INSTALL_TOKEN")

    with open(reqs_path, "r") as f:
        for raw in f:
            line = raw.strip()
            if not line or line.startswith("#"):
                continue

            parts = line.split(" -> ")
            if len(parts) < 3:
                bb.fatal(f"Bad manifest line (expected: url -> app_path -> md5): {line}")

            url = parts[0].strip()
            md5sum = parts[2].strip()

            fn = url.split("/")[-1]
            base, ext = os.path.splitext(fn)
            dlname = f"{base}_{token}{ext}"

            src_uri = f" {url};md5sum={md5sum};downloadfilename={dlname}"
            d.appendVar("SRC_URI", src_uri)
}

fakeroot python do_install_requirements() {
    import os

    bb.build.exec_func("set_reqs_file", d)

    reqs_path = d.getVar("REQS_FILE")
    if not reqs_path:
        bb.fatal("REQS_FILE is not set. Implement set_reqs_file() in your recipe and set REQS_FILE.")

    token = d.getVar("FILES_INSTALL_TOKEN")

    with open(reqs_path, "r") as f:
        for raw in f:
            line = raw.strip()
            if not line or line.startswith("#"):
                continue

            parts = line.split(" -> ")
            if len(parts) < 3:
                bb.fatal(f"Bad manifest line (expected: url -> app_path -> md5): {line}")

            url = parts[0].strip()
            app_path = parts[1].strip()

            fn = url.split("/")[-1]
            base, ext = os.path.splitext(fn)
            dlname = f"{base}_{token}{ext}"

            app_name = app_path.rstrip("/").split("/")[-1]

            d.setVar("CURRENT_APP_NAME", app_name)
            d.setVar("CURRENT_REQ_FILE", dlname)
            bb.build.exec_func("install_app_dir", d)
}