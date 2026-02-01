do_install:append() {
    # Replace commented kernel.core_pattern with new coredump configuration in sysctl.conf
    # Check if sysctl.conf exists
    if [ -f ${D}${sysconfdir}/sysctl.conf ]; then
        # Replace commented kernel.core_pattern line with the new pattern
        sed -i 's|^#kernel.core_pattern.*|kernel.core_pattern=/home/root/%e.%p.core|' ${D}${sysconfdir}/sysctl.conf
        # If the pattern wasn't found (no commented line), append it
        if ! grep -q "^kernel.core_pattern=" ${D}${sysconfdir}/sysctl.conf; then
            echo "" >> ${D}${sysconfdir}/sysctl.conf
            echo "# Coredump configuration" >> ${D}${sysconfdir}/sysctl.conf
            echo "# %e = executable filename, %p = process ID" >> ${D}${sysconfdir}/sysctl.conf
            echo "kernel.core_pattern=/home/root/%e.%p.core" >> ${D}${sysconfdir}/sysctl.conf
        fi
    else
        echo "error - no sysctl.conf"
    fi
}

