#!/bin/bash

declare -r SCRIPT=$(basename "$0")
declare -i PID=$$
declare -r LOCK_FILE="/tmp/$SCRIPT.lock"

declare separator_4=$(printf '%*s' "4" '' | tr ' ' '-')
declare separator_8=$(printf '%*s' "8" '' | tr ' ' '-')
declare separator_16=$(printf '%*s' "16" '' | tr ' ' '-')
declare separator_20=$(printf '%*s' "20" '' | tr ' ' '-')
declare separator_25=$(printf '%*s' "25" '' | tr ' ' '-')
declare separator_37=$(printf '%*s' "37" '' | tr ' ' '-')

# DMA Heaps & exporters information
declare -a exporter_names=($(find /sys/kernel/dmabuf/buffers/ -name "exporter_name" -exec cat {} \; 2>/dev/null | sort -u))
declare -i num_of_exporters=${#exporter_names[@]}
declare -A exporter_sizes
declare -i total_exporter_sizes=0
declare -i total_heaps_sizes=0

declare cma_meminfo="$(grep -i cma /proc/meminfo)"
declare -i cma_total=$(echo "$cma_meminfo" | grep -i total | awk '{print $2*1024}')
declare -i cma_free=$(echo "$cma_meminfo" | grep -i free | awk '{print $2*1024}')
declare -i cma_used=$((cma_total - cma_free))

declare dma_bufinfo=""

# Command line options variables.
declare -i F_SHOW_VERBOSE=0
declare F_UNIT_FORMAT="A" # B(Bytes)/K(KiB)/M(MiB)/G(GiB)/A(Auto)

trap 'trap_func' TERM INT
trap_func()
{
    echo "$SCRIPT interrupted. Exiting."
    rm -rf "$LOCK_FILE"
}

# @brief script usage.
function usage()
{
  echo "Dump dmabuf usage."
  echo "Usage: $SCRIPT [OPTIONS]"
  echo "       -h|--help: show help."
  echo "       -v|--verbose: show also overall DMA_BUF usage per exporter"
  echo "       -u|--unit B/K/M/G/A: show size format in Bytes/KiB/MiB/GiB units, default to A(auto) format"
  return 0
}

function format_number()
{
    local num="$1"
	local unit_fmt="$F_UNIT_FORMAT"
	local scaled=0 # fraction precision number of digits

    local KiB=$((1<<10))
    local MiB=$((1<<20))
    local GiB=$((1<<30))

	if [ "$F_UNIT_FORMAT" == "A" ]; then
        if [ $num -gt $GiB ]; then
                unit_fmt="G"
        elif [ $num -gt $MiB ]; then
                unit_fmt="M"
        elif [ $num -gt $KiB ]; then
                unit_fmt="K"
        else
                unit_fmt="B"
        fi
	fi

    case "$unit_fmt" in
    "B") printf "%s" "$num"
		 return 0
		 ;;
    "K") scaled=$(( num * 1000 / (1<<10) )) ;;
    "M") scaled=$(( num * 1000 / (1<<20) )) ;;
    "G") scaled=$(( num * 1000 / (1<<30) )) ;;
    *) printf "%s" "$num"
       return 0
       ;;
    esac

    printf "%d.%03d%s\n" $((scaled / 1000)) $((scaled % 1000)) "${unit_fmt}iB"

    return 0
}

# @brief Prepare DMA_BUF heaps & exporters info.
function dma_bufinfo_prepare()
{
    # Init Mapping [exporter-name -> int] entries
    for ((i=0; i<num_of_exporters; i++)); do
        exporter_sizes[${exporter_names[$i]}]=0
    done

    # Sample DMA buf info
    dma_bufinfo="$(grep -v 'exp_name\|bytes' /sys/kernel/debug/dma_buf/bufinfo | awk 'NF == 6')"

    # Calculate overall usage per DMA_BUF exporter
    for key in "${!exporter_sizes[@]}"; do
        exporter_sizes[$key]=$(echo "$dma_bufinfo" | grep "$key" | awk '{sum+=$1} END {printf("%d\n", sum)}')
        total_exporter_sizes=$((total_exporter_sizes + exporter_sizes[$key]))
    done
    
    return 0
}

# @brief Resolve device tree reserved-memory area by reg or by alloc-ranges and size.
# @param heap_name The name of the heap to resolve
# @return Sets global variables: dt_heap_size, dt_range_base, dt_range_end, dt_resolved
function resolve_dt_reserved_memory()
{
    local heap_name="$1"
    local reseved_mem_path="/sys/firmware/devicetree/base/reserved-memory"
    local heap_path="${reseved_mem_path}/$heap_name"
    
    # Initialize return variables
    dt_heap_size=0
    dt_range_base=0
    dt_range_end=0
    dt_resolved=0
    
    # Check if the heap directory exists
    [ ! -d "$heap_path" ] && return 1
    
    # Method 1: Try to read from 'reg' property (address + size)
    if [ -f "$heap_path/reg" ]; then
        local reg_values=($(hexdump -v -e '"0x" 8/1 "%02x"' -e '"\n"' "$heap_path/reg" 2>/dev/null))
        if [ ${#reg_values[@]} -ge 2 ]; then
            dt_range_base=$((${reg_values[0]}))
            dt_heap_size=$((${reg_values[1]}))
            dt_range_end=$((dt_range_base + dt_heap_size - 1))
            dt_resolved=1
            return 0
        fi
    fi
    
    # Method 2: Try to read from 'alloc-ranges' and 'size' properties
    if [ -f "$heap_path/size" ] && [ -f "$heap_path/alloc-ranges" ]; then
        local size_hex=$(hexdump -v -e '"0x" 8/1 "%02x"' -e '"\n"' "$heap_path/size" 2>/dev/null)
        local alloc_ranges=($(hexdump -v -e '"0x" 8/1 "%02x"' -e '"\n"' "$heap_path/alloc-ranges" 2>/dev/null))
        
        if [ -n "$size_hex" ] && [ ${#alloc_ranges[@}} -ge 2 ]; then
            dt_heap_size=$(($size_hex))
            local pool_base=$((${alloc_ranges[0]}))
            local pool_size=$((${alloc_ranges[1]}))
            
            # Compare allocated size vs pool size
            if [ "$dt_heap_size" -eq "$pool_size" ]; then
                # Size matches pool - static allocation using entire pool
                dt_range_base=$pool_base
                dt_range_end=$((pool_base + pool_size - 1))
            else
                # Dynamic allocation - exact addresses unknown, allocated from pool
                # Set range to 0 to indicate dynamic allocation
                dt_range_base=0
                dt_range_end=0
            fi
            dt_resolved=1
            return 0
        fi
    fi
    
    # Method 3: Try size only (if available)
    if [ -f "$heap_path/size" ]; then
        local size_hex=$(hexdump -v -e '"0x" 8/1 "%02x"' -e '"\n"' "$heap_path/size" 2>/dev/null)
        if [ -n "$size_hex" ]; then
            dt_heap_size=$(($size_hex))
            dt_range_base=0
            dt_range_end=0
            dt_resolved=1
            return 0
        fi
    fi
    
    return 1
}

# @brief show CMA info.
function cma_proc_mem_info()
{
    local hdr_separator=$(printf "%-20s  %-16s\n" "$separator_20" "$separator_16")
    local hdr_info=$(printf "%-20s  %-16s" "CMA" "Size")

    [ -z "$cma_total" ] && return 0

    echo "$hdr_separator"
    echo "$hdr_info"
    echo "$hdr_separator"
    printf "%-20s  %-16s\n" "Used" "$(format_number "$cma_used")"
    printf "%-20s  %-16s\n" "Free" "$(format_number "$cma_free")"
    echo "$hdr_separator"
    printf "%-20s  %-16s\n\n\n" "Total" "$(format_number "$cma_total")"

    return 0
}

# @brief List DMA_BUF heaps info & usage.
function dma_heap_info()
{
	local reseved_mem_path="/sys/firmware/devicetree/base/reserved-memory"
    local hdr_separator=$(printf "%-20s  %-16s  %-16s  %4s  %-16s  %-37s\n" "$separator_20" "$separator_16" "$separator_16" "$separator_4" "$separator_16" "$separator_37")
    local hdr_info=$(printf "%-20s  %-16s  %-16s  %4s  %-16s  %-37s\n" "Heap-Name" "Size" "Used" "Use%" "Free" "Physical-Allocation-Range")
    local -i used_hailo_media_buf_cma=$(echo "$dma_bufinfo" | grep 'hailo_media_buf,cma' | awk '{sum+=$1} END {printf("%d", sum)}')
    local -i total_used=0
    local -i total_free=0

    [ ! -e /dev/dma_heap/ ] && echo "No heaps found" && return 0

    echo "$hdr_separator"
    echo "$hdr_info"
    echo "$hdr_separator"
    for heap in $(ls /dev/dma_heap/); do
        # Use the device tree resolver function
        resolve_dt_reserved_memory "$heap"
        
        # Check if resolution was successful
        if [ "$dt_resolved" -eq 0 ]; then
            printf "%-20s  %-16s  %-16s  %4s  %-16s  %s\n" \
                "$heap" \
                "N/A" \
                "N/A" \
                "N/A" \
                "N/A" \
                "[Device tree info not available]"
            continue
        fi
        
        # Skip if heap_size is 0 to avoid division by zero
        if [ "$dt_heap_size" -eq 0 ]; then
            printf "%-20s  %-16s  %-16s  %4s  %-16s  %s\n" \
                "$heap" \
                "N/A" \
                "N/A" \
                "N/A" \
                "N/A" \
                "[Invalid heap size]"
            continue
        fi
        
        if [ "$heap" == "hailo_media_buf,cma" ]; then
            used_dma_buf=$used_hailo_media_buf_cma
        else
            used_dma_buf=$((cma_used - used_hailo_media_buf_cma))
        fi
        
        total_heaps_sizes=$((total_heaps_sizes + dt_heap_size))
        total_used=$((total_used + used_dma_buf))
        total_free=$((total_free + (dt_heap_size - used_dma_buf)))

        printf "%-20s  %-16s  %-16s  %4s  %-16s  [%016x - %016x]\n" \
            "$heap" \
            "$(format_number "$dt_heap_size")" \
            "$(format_number "$used_dma_buf")" \
            "$((used_dma_buf * 100 / dt_heap_size))" \
            "$(format_number $((dt_heap_size - used_dma_buf)))" \
            "$dt_range_base" "$dt_range_end"
    done
    echo "$hdr_separator"
    printf "%-20s  %-16s  %-16s  %4s  %-16s\n\n\n" \
        "Total" \
        "$(format_number "$total_heaps_sizes")" \
        "$(format_number "$total_used")" \
        "$((total_used * 100 / total_heaps_sizes))" \
        "$(format_number "$total_free")"

    return 0
}

# @brief List overall DMA_BUF usage per exporter.
function dmabuf_per_exporter_info()
{
    [ "$total_exporter_sizes" -eq 0 ] && return 0

    local hdr_separator=$(printf "%-25s  %-16s\n" "$separator_25" "$separator_16")
    local hdr_info=$(printf "%-25s  %-16s\n" "Exporter-Name" "Used")

    echo "$hdr_separator"
    echo "$hdr_info"
    echo "$hdr_separator"
    for key in "${!exporter_sizes[@]}"; do
        printf "%-25s  %-16s\n" "$key" "$(format_number "${exporter_sizes[$key]}")"
    done
    echo "$hdr_separator"
    printf "%-25s  %-16s\n\n\n" "Total" "$(format_number "$total_exporter_sizes")"
    
    return 0
}

# @brief main.
main()
{
    case "$F_UNIT_FORMAT" in
    "A"|"B"|"K"|"M"|"G") ;;
    *) usage && return 0 ;;
    esac
    
    cma_proc_mem_info
    dma_bufinfo_prepare
    dma_heap_info
    [ "$F_SHOW_VERBOSE" -eq 1 ] && dmabuf_per_exporter_info

    return 0
}

#------------------------------------------------------------------------------
#                               MAIN
#------------------------------------------------------------------------------
(
    flock -xn 200 || { echo "$SCRIPT is already running."; exit 1; }

    OPTS_SHORT="hvu:"   # Legal short options
    OPTS_LONG="help,verbose,unit:" # Legal long options
    # $PARSED_OPTIONS will contain the legal arguments out of "$@".
    PARSED_OPTIONS=$(getopt -n "$0" -o $OPTS_SHORT -l $OPTS_LONG -- "$@") || {
        rm -rf "$LOCK_FILE"
        exit 1
    }

    eval set -- "$PARSED_OPTIONS"     # Set the positional parameters ($1, $2, etc)

    while true; do
        case "$1" in
        --help|-h) usage && exit 0 ;;
        --verbose|-v)  F_SHOW_VERBOSE=1; shift 1 ;;
        --unit|-u)  F_UNIT_FORMAT="$2"; shift 2 ;;
        --) shift; break ;;
        *) echo "Argument [$1] not handled."; shift; break ;;
        esac
    done

    main "$@"
    rm -rf "$LOCK_FILE"
    exit $?
) 200>"$LOCK_FILE"
