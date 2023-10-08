# INITSCRIPT_PARAMS define when start and stop the system-V services
# overriding the INITSCRIPT_PARAMS by removing the auto start of rng-tools
INITSCRIPT_PARAMS="stop 30 0 6 1 ."

