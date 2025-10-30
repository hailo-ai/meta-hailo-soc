# Custom build info + UID class
# Writes /etc/build-info and drops a single .uid file in deploy/images.
inherit image-artifact-names

# Where to write the build info inside the image
HAILO_IMAGE_BUILDINFO_FILE ?= "${sysconfdir}/build-info"

# Default vars to display
HAILO_IMAGE_BUILDINFO_VARS ?= "DISTRO DISTRO_VERSION TARGET_SYS MACHINE IMAGE_NAME IMAGE_UID"

IMAGE_UID = ""
BUILD_DOC_ID = ""

# Add our prefunc to capture UID before buildinfo is written
do_image[prefuncs] += "create_buildinfo_file"
do_image[postfuncs] += "create_image_uid_file"
do_populate_lic_deploy[postfuncs] += "print_buildinfo_summary"

# Utility: format selected vars
def try_get_var(d, variable: str) -> str:
    var = d.getVar(variable)
    if not var:
        raise ValueError(f"Variable [{variable}] is not set")
    return var

# Compute IMAGE_UID from do_image taskhash
def set_image_uid(d) -> None:
    taskhash = try_get_var(d, "BB_TASKHASH")
    d.setVar("IMAGE_UID", taskhash)
    bb.note(f"IMAGE_UID set to {taskhash}")

def set_doc_id(d) -> None:
    import os
    build_doc_id = os.getenv("BUILD_DOC_ID")
    if build_doc_id:
        d.setVar("BUILD_DOC_ID", build_doc_id)
        d.appendVar('HAILO_IMAGE_BUILDINFO_VARS', " BUILD_DOC_ID")
    else:
        bb.note("BUILD_DOC_ID env var not set, skipping")

# Utility: format selected vars
def image_buildinfo_outputvars(d) -> str:
    vars = d.getVar("HAILO_IMAGE_BUILDINFO_VARS") or ""
    vars = vars.split()
    ret = ""
    for var in vars:
        try:
            value = d.getVar(var) or ""
            if (d.getVarFlag(var, 'type') == "list"):
                value = oe.utils.squashspaces(value)
            ret += "%s = %s\n" % (var, value)
        except Exception as e:
            bb.note(f"[image_buildinfo_outputvars] Failed to get variable {var}: {e}")
    return ret.rstrip('\n')

# Write build-info file into rootfs and .uid sidecar into deploy
python create_buildinfo_file () {
    import pathlib

    if not d.getVar('HAILO_IMAGE_BUILDINFO_FILE'):
        bb.plain(f"[HAILO_IMAGE_BUILDINFO_FILE] not set, skipping buildinfo write")
        return

    set_image_uid(d)  # Set IMAGE_UID
    set_doc_id(d)  # Set BUILD_DOC_ID if available

    # Collect vars
    buildinfo = image_buildinfo_outputvars(d)

    try:
        outfile = pathlib.Path(d.expand("${IMAGE_ROOTFS}${HAILO_IMAGE_BUILDINFO_FILE}"))
        outfile.write_text(
            "-----------------------\n"
            "Build Configuration:  |\n"
            "-----------------------\n"
            f"{buildinfo}\n"
        )
        bb.note(f"Wrote build-info to {outfile}")
    except Exception as e:
        bb.warn(f"Failed to write build-info file: {e}")
}

# Write a single UID file in deploy dir
python create_image_uid_file () {
    import pathlib
    import os
    try:
        deploy_dir_image = try_get_var(d, "DEPLOY_DIR_IMAGE")
        deploy_dir = pathlib.Path(deploy_dir_image)
        # Remove older .uid files
        remove_old_uid_files(deploy_dir)
        # Write the new one
        uid_file = create_uid_file(d, deploy_dir)
        # Create symlink with IMAGE_LINK_NAME if available
        create_image_uid_link_file(d, uid_file, deploy_dir)
    except Exception as e:
        bb.warn(f"Failed to create image UID file: {e}")
}

def remove_old_uid_files(deploy_dir: 'pathlib.Path') -> None:
    from pathlib import Path
    for old in deploy_dir.glob("*.uid"):
        try:
            old.unlink()
            bb.note(f"Removed old UID file: {old.name}")
        except Exception as e:
            bb.note(f"Could not remove {old}: {e}")

def create_uid_file(d, deploy_dir: 'pathlib.Path') -> 'pathlib.Path':
    from pathlib import Path
    image_name = try_get_var(d, "IMAGE_NAME")
    uid = try_get_var(d, "IMAGE_UID")
    uid_file = deploy_dir / f"{image_name}.uid"
    uid_file.write_text(f"{uid}\n")
    bb.note(f"Created UID file {uid_file}")
    return uid_file

def create_image_uid_link_file(d, uid_file: 'pathlib.Path', deploy_dir: 'pathlib.Path') -> None:
    import os
    from pathlib import Path
    # Create symlink with IMAGE_LINK_NAME if available
    image_link_name = d.getVar("IMAGE_LINK_NAME")
    link_uid_file = deploy_dir / f"{image_link_name}.uid"
    try:
        # Remove existing symlink if it exists
        if link_uid_file.exists() or link_uid_file.is_symlink():
            link_uid_file.unlink()
        # Create symlink pointing to the original UID file (using just the filename)
        os.symlink(uid_file.name, str(link_uid_file))
        bb.note(f"Created UID symlink {link_uid_file} -> {uid_file.name}")
    except Exception as e:
        bb.warn(f"Failed to create UID symlink: {e}")

# Print build info summary to the log after image is complete
python print_buildinfo_summary () {
    import pathlib

    if not d.getVar('HAILO_IMAGE_BUILDINFO_FILE'):
        return

    # Read content from the build-info file
    buildinfo_file = pathlib.Path(d.expand("${IMAGE_ROOTFS}${HAILO_IMAGE_BUILDINFO_FILE}"))

    if not buildinfo_file.exists():
        bb.warn("Build info file not found at: " + str(buildinfo_file))
        return

    try:
        content = buildinfo_file.read_text()
        for line in content.splitlines():
            bb.plain(line)
        bb.plain("-----------------------")
    except Exception as e:
        bb.warn(f"Failed to read build info file: {e}")
}
