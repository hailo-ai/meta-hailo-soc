#!/bin/bash

base=/sys/devices/hailo_ddr_pmu

# Used externally
counter0=/sys/devices/hailo_ddr_pmu/counter0
counter1=/sys/devices/hailo_ddr_pmu/counter1
counter2=/sys/devices/hailo_ddr_pmu/counter2
counter3=/sys/devices/hailo_ddr_pmu/counter3

# $1: sample period in microseconds
# $2: running mode (0 (periodic) or 1 (accumulated))
# $3: output filename
ddr_measure() {
    if [ -n "$1" ]; then
        sample_time_us=",sample_time_us=$1"
    fi
    if [ -n "$2" ]; then
        running_mode=",running_mode=$2"
    fi
    output=measurement.txt
    if [ -n "$3" ]; then
        output=$3
    fi
    echo "Starting measurement, press Ctrl+C to stop"
    perf record -a -e hailo_ddr_pmu/hailo_ddr_bw$sample_time_us$running_mode/
    perf script > $output
}

# $1: time to run the measurement in seconds
# $1: sample period in microseconds
# $3: running mode (0 (periodic) or 1 (accumulated))
# $4: output filename
ddr_measure_sleep() {
    sleeptime=10
    if [ -n "$1" ]; then
        sleeptime=$1
    fi
    if [ -n "$2" ]; then
        sample_time_us=",sample_time_us=$2"
    fi
    if [ -n "$3" ]; then
        running_mode=",running_mode=$3"
    fi
    output=measurement.txt
    if [ -n "$4" ]; then
        output=$4
    fi
    echo "Starting measurement for $sleeptime seconds, press Ctrl+C to stop"
    perf record -a -e hailo_ddr_pmu/hailo_ddr_bw$sample_time_us$running_mode/ sleep $sleeptime
    perf script > $output
}

# $1: command to measure
# $2: sample period in microseconds
# $3: running mode (0 (periodic) or 1 (accumulated))
# $4: output filename
ddr_measure_command() {
    if [ -n "$2" ]; then
        if ! [[ $2 =~ ^[0-9]+$ ]]; then
            echo "Invalid sample time: $2" >> /dev/stderr
            return 1
        fi
        sample_time_us=",sample_time_us=$2"
    fi
    if [ -n "$3" ]; then
        if ! [[ $3 =~ ^[01]$ ]]; then
            echo "Invalid running mode: $3" >> /dev/stderr
            echo "Valid running modes are: 0 1" >> /dev/stderr
            return 1
        fi
        running_mode=",running_mode=$3"
    fi
    output=measurement.txt
    if [ -n "$4" ]; then
        output=$4
    fi
    echo "Starting measurement for \"$1\", press Ctrl+C to stop"
    perf record -a -e hailo_ddr_pmu/hailo_ddr_bw$sample_time_us$running_mode/ $1
    perf script > $output
}

###########################
###########################
## Counter configuration ##
###########################
###########################

declare -A FILTER_ROUTE_BASES=(
    ["dsp_idma_ro_0"]=0
    ["dsp_idma_ro_1"]=0x8000
    ["dsp_idma_ro_2"]=0x10000
    ["dsp_idma_wo_0"]=0x20000
    ["dsp_idma_wo_1"]=0x28000
    ["dsp_idma_wo_2"]=0x30000
    ["dram_dma0_ddrbus"]=0x40000
    ["dram_dma0_ddrbus_lp"]=0x48000
    ["dram_dma1_ddrbus"]=0x50000
    ["dram_dma1_ddrbus_lp"]=0x58000
    ["dram_dma2_ddrbus"]=0x60000
    ["dram_dma2_ddrbus_lp"]=0x68000
    ["ap_cluster_ace"]=0x70000
    ["csi_rx0"]=0x78000
    ["csi_rx1"]=0x80000
    ["csi_tx0"]=0x88000
    ["debug_etr"]=0x90000
    ["dram_dma0_fastbus_ro"]=0x98000
    ["dram_dma0_fastbus_wo"]=0xa0000
    ["dram_dma1_fastbus_ro"]=0xa8000
    ["dram_dma1_fastbus_wo"]=0xb0000
    ["dram_dma2_fastbus_ro"]=0xb8000
    ["dram_dma2_fastbus_wo"]=0xc0000
    ["dram_dma3_ddrbus_ro"]=0xc8000
    ["dram_dma3_ddrbus_wo"]=0xd0000
    ["dram_dma_ddrbus_desc"]=0xd8000
    ["dsp_ddrbus"]=0xe0000
    ["ethernet"]=0xe8000
    ["gic_master"]=0xf0000
    ["h265"]=0xf8000
    ["isp_ddrbus0"]=0x100000
    ["isp_ddrbus1"]=0x108000
    ["isp_fastbus"]=0x110000
    ["main2fast"]=0x118000
    ["noc_firewall_servic"]=0x120000
    ["noc_pcie_firewall_srvice"]=0x128000
    ["noc_service"]=0x130000
    ["pcie_aux"]=0x138000
    ["pcie_desc"]=0x140000
    ["pcie_main"]=0x148000
    ["sdio1"]=0x150000
    ["usb"]=0x158000
)

ROUTE_MASK=0x1f8000

__ddr_check_counter_number() {
    if ! [[ $1 =~ ^[0-3]$ ]]; then
        echo "Invalid counter number: $1" >> /dev/stderr
        echo "Valid counter numbers are: 0 1 2 3" >> /dev/stderr
        return 1
    fi
}

# $1: counter number (0-3)
ddr_enable_counter() {
    __ddr_check_counter_number $1 || return 1
    echo "1" > $base/counter$1/enabled
}

# $1: counter number (0-3)
ddr_disable_counter() {
    __ddr_check_counter_number $1 || return 1
    echo "0" > $base/counter$1/enabled
}

# $1: counter number (0-3)
ddr_reset_counter_config() {
    __ddr_check_counter_number $1 || return 1
    ddr_disable_counter $1
    counter=$base/counter$1

    echo "filter" > $counter/mode

    # Set default values
    echo "0" > $counter/route_id_base
    echo "0" > $counter/route_id_mask
    echo "0" > $counter/address_base
    echo "0x3f" > $counter/window_size
    echo "0xf" > $counter/opcode
    echo "0xf" > $counter/length
    echo "0" > $counter/urgency
}

# $1: counter number (0-3)
ddr_set_counter_total() {
    __ddr_check_counter_number $1 || return 1
    counter=$base/counter$1
    echo "total" > $counter/mode
    ddr_enable_counter $1
}

# $1: counter number (0-3)
# $2: filter name
# $3: opcode (optional)
ddr_set_counter_filter() {
    __ddr_check_counter_number $1 || return 1

    ddr_reset_counter_config $1
    counter=$base/counter$1

    route_base=${FILTER_ROUTE_BASES[$2]}
    if [ -n "$route_base" ]; then
        echo "$route_base" > $counter/route_id_base
        echo "$ROUTE_MASK" > $counter/route_id_mask
    else
        # check if the filter name is "all"
        if [ "$2" = "all" ]; then
            echo "0" > $counter/route_id_base
            echo "0" > $counter/route_id_mask
        else
            echo "Invalid filter name: $2" >> /dev/stderr
            echo "Valid filter names are: ${!FILTER_ROUTE_BASES[@]} all" >> /dev/stderr
            return 1
        fi
    fi

    if [ -n "$3" ]; then
        case $3 in
            r|read)
                echo "0x1" > $counter/opcode
                ;;
            w|write)
                echo "0x2" > $counter/opcode
                ;;
            *)
                echo "Invalid optional opcode: $3" >> /dev/stderr
                echo "Valid opcodes are: r w" >> /dev/stderr
                return 1
                ;;
        esac
    fi

    ddr_enable_counter $1
}
