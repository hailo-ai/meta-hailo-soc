# Override the global ulimit for the init scripts
do_install:append() {
    RC_SCRIPT="${D}${sysconfdir}/init.d/rc"

    if [ -f "$RC_SCRIPT" ]; then
        # Check if the file is already patched
        if ! grep -q "FD_MAX_OPEN_FILES" "$RC_SCRIPT"; then

            # We use a formatted string with newlines (\n) to create the visual spacing.
            # 2i \ -> Insert at line 2.

            sed -i "2i \\
\\
# Injected by Yocto (FD_MAX_OPEN_FILES=${FD_MAX_OPEN_FILES})\\
ulimit -n ${FD_MAX_OPEN_FILES}\\
" "$RC_SCRIPT"

            bbnote "Patched /etc/init.d/rc with global ulimit ${FD_MAX_OPEN_FILES}"
        else
            bbnote "/etc/init.d/rc is already patched."
        fi
    else
        bbwarn "Could not find /etc/init.d/rc to patch!"
    fi
}