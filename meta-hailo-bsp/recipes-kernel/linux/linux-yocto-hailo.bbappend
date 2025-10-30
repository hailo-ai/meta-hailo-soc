# Makes dtc generate a symbol table and add metadata that allows the DTB to be used with device tree overlays (.dtbo).
EXTRA_OEMAKE:append = " DTC_FLAGS=-@"

# Hailo15-SBC: add gyro overlay
KERNEL_DEVICETREE:append:hailo15-sbc = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sbc-gyro.dtbo" 

# Hailo10-M.2: add CMA configuration overlays
KERNEL_DEVICETREE:append:hailo10-m2 = " \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-0.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-1.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-3.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-4.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-5.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-6.dtb \
"