#!/bin/bash

declare -r SCRIPT=$(basename "$0")
declare -i PID=$$
declare -r LOCK_FILE="/tmp/$SCRIPT.lock"

trap 'trap_func' TERM INT
trap_func()
{
    echo "$SCRIPT interrupted. Exiting."
    rm -rf "$LOCK_FILE"
}

# @brief script usage.
function usage()
{
  echo "Dump CMA heaps info & usage"
  echo "Usage: $SCRIPT [OPTIONS]"
  echo "       -h|--help: show this help."
  return 0
}

# @brief print CMA heap info header.
function print_cma_heap_info_hdr()
{
	printf "%s\n" \
	       "---------------- ------- ------- ------- --------- ------------ -------------------------------------"
	printf "                 page    free    used    Max-chunk  (pages)     (bytes)\n"
	printf "heap-name        count   pages   pages   of pages   base_pfn    address-range\n"
	printf "%s\n" \
	       "---------------- ------- ------- ------- --------- ------------ -------------------------------------"
	return 0
}

# @brief print CMA heap info.
function print_cma_heap_info_data()
{
	local heap_name=$1
	
	[ ! -d /sys/kernel/debug/cma/${heap_name} ] && echo "${heap_name} not exist, either ${heap_name} isn't declared in DTB or kernel CONFIG_CMA_DEBUGFS not selected. Aborting..." && return 0
	pushd /sys/kernel/debug/cma/${heap_name} &> /dev/null

	# Read the base PFN and bitmap values into arrays.
	local -i heap_base_pfn=$(cat base_pfn)
	local -ai bitmap_arr=($(cat bitmap))
	local -i bitmap_arr_size=${#bitmap_arr[@]}
	local -i count=$(cat count)
	local -i used=$(cat used)
	local -i free=$((count - used))
	local -i maxchunk=$(cat maxchunk)
	local -i order_per_bit=$(cat order_per_bit)
	local -i pages_per_bit=$((2**order_per_bit))
	local -i page_size=$(getconf PAGESIZE)
	local -i heap_addr_start=$((heap_base_pfn*page_size)) 
	local -i heap_addr_end=$(((heap_base_pfn + count) * page_size - 1))

	# Print CMA heap info.
	printf "%-16s %-7d %-7d %-7d %-9d %-12d [%X - %X)\n" \
			"$heap_name" "$count" "$free" "$used" "$maxchunk" "$heap_base_pfn" $heap_addr_start $heap_addr_end

	popd &> /dev/null
	return 0
}

# @brief print CMA heap used/free pages bitmap.
function print_cma_heap_bitmap()
{
	local heap_name=$1
	
	[ ! -d /sys/kernel/debug/cma/${heap_name} ] && echo "${heap_name} not exist, aborting..." && return 0
	pushd /sys/kernel/debug/cma/${heap_name} &> /dev/null

	# Read the base PFN and bitmap values into arrays.
	local -i i
	local -i heap_base_pfn=$(cat base_pfn)
	local -ai bitmap_arr=($(cat bitmap))
	local -i bitmap_arr_size=${#bitmap_arr[@]}
	local -i order_per_bit=$(cat order_per_bit)
	local -i pages_per_bit=$((2**order_per_bit))
	local -i page_size=$(getconf PAGESIZE)

	# Print pages bitmap.
	printf ">>> $heap_name heap pages bitmap dump. (A bit represent $pages_per_bit pages. {1-used, 0-free}.\n"
	printf "%s\n" "---------------- -------- -------------------------------------"
	printf "%-16s %-8s %s\n" "Address" "pfn" "bitmap"
	printf "%s\n" "---------------- -------- -------------------------------------"

	# Initialize variables for detecting repeated lines.
	prev_bitmap=""
	prev_address=""
	prev_pfn=""
	repeat_count=0

	for ((i=0, pfn=heap_base_pfn; i<bitmap_arr_size; i++, pfn+=(32*pages_per_bit))); do
		# Calculate current address and bitmap.
		current_address=$((pfn * page_size))
		current_bitmap=""

		# Check if the current line matches the previous line.
		if [ $i -gt 0 ] && [ ${bitmap_arr[$i]} -eq ${bitmap_arr[$((i-1))]} ]; then
			((repeat_count++))
		else
			# Build the bitmap string for the current line.
			for ((j=0; j<32; j++)); do
				if ((j > 0 && j % 8 == 0)); then
					# add space between after each 8 bits.
					current_bitmap+=" "
				fi
				current_bitmap+=$(((${bitmap_arr[$i]} >> j) & 0x1))
			done

			# Print the previous line if there were repetitions.
			if ((repeat_count > 0)); then
				printf "  [repeats %d times]\n" "$repeat_count"
			fi

			# Print the current line.
			printf "%-016X %-8s %s\n" "$current_address" "$pfn" "$current_bitmap"

			# Reset repetition tracking.
			prev_bitmap="$current_bitmap"
			prev_address="$current_address"
			prev_pfn="$pfn"
			repeat_count=0
		fi
	done

	# Handle any remaining repeated line at the end.
	if ((repeat_count > 0)); then
		printf "  [repeats %d times]\n" "$repeat_count"
	fi
	printf "\n\n"

	popd &> /dev/null
	return 0
}

# @brief main.
main()
{
	local -a cma_heaps=("cma-hailo_media" "cma-linux,cma")
	local -i cma_heaps_nr=${#cma_heaps[@]}
	local -i i=0
	
	print_cma_heap_info_hdr
	for((i=0; i<cma_heaps_nr; i++)); do
		print_cma_heap_info_data "${cma_heaps[$i]}"
	done
	printf "\n\n"

	for((i=0; i<cma_heaps_nr; i++)); do
		print_cma_heap_bitmap "${cma_heaps[$i]}"
	done

    return 0
}

#------------------------------------------------------------------------------
#                               MAIN
#------------------------------------------------------------------------------
(
    flock -xn 200 || { echo "$SCRIPT is already running."; exit 1; }

    OPTS_SHORT="h"   # Legal short options
    OPTS_LONG="help" # Legal long options
    # $PARSED_OPTIONS will contain the legal arguments out of "$@".
    PARSED_OPTIONS=$(getopt -n "$0" -o $OPTS_SHORT -l $OPTS_LONG -- "$@") || {
        rm -rf "$LOCK_FILE"
        exit 1
    }

    eval set -- "$PARSED_OPTIONS"     # Set the positional parameters ($1, $2, etc)

    while true; do
        case "$1" in
        --help|-h) usage && exit 0 ;;
        --) shift; break ;;
        *) echo "Argument [$1] not handled."; shift; break ;;
        esac
    done

    main "$@"
    rm -rf "$LOCK_FILE"
    exit $?
) 200>"$LOCK_FILE"

