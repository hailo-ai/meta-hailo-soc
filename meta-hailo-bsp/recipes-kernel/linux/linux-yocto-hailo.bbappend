# Makes dtc generate a symbol table and add metadata that allows the DTB to be used with device tree overlays (.dtbo).
EXTRA_OEMAKE:append = " DTC_FLAGS=-@"

# Hailo15-SBC: add gyro overlay
KERNEL_DEVICETREE:append:hailo15-sbc = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sbc-gyro.dtbo" 
# Hailo15-SBC: add seneosr_1 overlay
KERNEL_DEVICETREE:append:hailo15-sbc = " ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15-sbc-sensor-1.dtbo"

# Hailo10-M.2: add CMA configuration overlays
KERNEL_DEVICETREE:append:hailo10-m2 = " \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-0.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-1.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-2.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-3.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-4.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-5.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-6.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-9.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-10.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-11.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-12.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-13.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-14.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-16.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-17.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-18.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-19.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-20.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-21.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-22.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-23.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-24.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-25.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-26.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo10-board-sku-27.dtb \
"

# Hailo15l: add CMA configuration overlays
KERNEL_DEVICETREE:append:hailo15l-sbc = " \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15l-sbc.dtb \
    ${LINUX_YOCTO_HAILO_BOARD_VENDOR}/hailo15l-sbc-1gb.dtb \
"