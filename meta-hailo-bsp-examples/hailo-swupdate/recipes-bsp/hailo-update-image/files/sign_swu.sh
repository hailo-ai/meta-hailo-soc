#!/bin/sh
#
# sign_swu.sh — External signer for SWUpdate (Hailo accelerator builds)
#
# This script is called by SWUpdate during the packaging process to sign
# the sw-description file using Hailo's secure boot infrastructure.
#
# Purpose:
#   - Signs sw-description files for Hailo accelerator builds
#   - Uses hailo_mkimage_wrapper_atftp.py for signature generation
#   - Integrates with Hailo's HSM-based signing workflow
#
# Arguments (passed by SWUpdate):
#   $1 - BINDIR: Directory containing hailo_mkimage_wrapper_atftp.py
#   $2 - KEYDIR: Directory containing signing keys
#   $3 - MKARGS: Additional arguments for mkimage wrapper
#   $4 - SWDESC: Directory containing sw-description file to be signed
#
# Output:
#   - Creates sw-description.sig in SIGOUT directory
#
# Dependencies:
#   - hailo-secureboot-scripts-native (provides hailo_mkimage_wrapper_atftp.py)
#   - Valid signing keys in KEYDIR
#   - Network access to Hailo HSM server (for accelerator mode)
#
# Usage:
#   Called automatically by SWUpdate, not intended for direct execution
#
#
# Inputs from SWUpdate:
BINDIR="$1"          # Directory containing hailo_mkimage_wrapper_atftp.py
KEYDIR="$2"          # Directory containing signing keys
MKARGS="$3"          # Additional mkimage wrapper arguments
SWDESC="$4"          # Directory containing sw-description file
WRAPPER="${BINDIR}/hailo_mkimage_wrapper_atftp.py"

# Debug output - show all input parameters
echo "[sign_swu] === Hailo SWU Signing Process ==="
echo "[sign_swu] BINDIR   = $BINDIR"
echo "[sign_swu] KEYDIR   = $KEYDIR"
echo "[sign_swu] MKARGS   = $MKARGS"
echo "[sign_swu] SWDESC   = $SWDESC"
echo "[sign_swu] WRAPPER  = $WRAPPER"

# Pre-flight checks - ensure all required files and tools exist
if [ ! -f "$SWDESC/sw-description" ]; then
    echo "[sign_swu] ERROR: sw-description not found: $SWDESC/sw-description"
    exit 1
fi

if [ ! -x "$WRAPPER" ]; then
    echo "[sign_swu] ERROR: Wrapper tool not found: $WRAPPER"
    exit 2
fi

# Remove any existing signature file to ensure clean signing
if [ -f "$SWDESC/sw-description.sig" ]; then
    echo "[sign_swu] Removing existing signature file: $SWDESC/sw-description.sig"
    rm -f "$SWDESC/sw-description.sig"
fi

# Build the signing command using Hailo mkimage wrapper
# Format: wrapper -F (FIT format) -k keydir -r (raw mode) input_file additional_args
CMD="$WRAPPER -F -k $KEYDIR -r $SWDESC/sw-description $MKARGS"

echo "[sign_swu] Running: $CMD"

# Execute the signing command and capture all output (stdout + stderr)
OUTPUT=$($CMD 2>&1)
RET=$?

# Report execution results
echo "[sign_swu] Exit code: $RET"
echo "[sign_swu] Output:"
echo "$OUTPUT"

# Check if signing was successful
if [ $RET -ne 0 ]; then
    echo "[sign_swu] ERROR: Signing failed"
    exit $RET
fi

echo "[sign_swu] Created signature file: $SWDESC/sw-description.sig"

# Success - signature created and deployed
echo "[sign_swu] === Signing completed successfully ==="
exit 0
