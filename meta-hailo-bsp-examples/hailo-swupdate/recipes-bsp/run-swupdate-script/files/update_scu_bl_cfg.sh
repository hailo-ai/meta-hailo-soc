#!/bin/bash

source /etc/boot_definitions.sh

###############################################################
## Utility functions
###############################################################

# Function to reverse the bytes of each 32-bit word
reverse_bytes() {
  local word=$1
  echo $word | awk '{printf("%s%s%s%s\n", substr($1, 7, 2), substr($1, 5, 2), substr($1, 3, 2), substr($1, 1, 2))}'
}

# Function to reverse bits in a byte
reverse_bits_in_byte() {
  local byte="$1"
  local reversed_byte=0
  for (( i=0; i<8; i++ )); do
    (( reversed_byte <<= 1 ))
    (( reversed_byte |= byte & 1 ))
    (( byte >>= 1 ))
  done

  printf "%02x" "$reversed_byte"
}

# Function to reverse bits in the whole data
reverse_bits_in_data() {
  local data="$1"
  local reversed_data=""
  for (( i=0; i<${#data}; i+=2 )); do
    byte="${data:i:2}"
    byte_dec=$(printf "%d" "0x$byte")
    reversed_byte=$(reverse_bits_in_byte "$byte_dec")
    reversed_data+="$reversed_byte"
  done
  echo "$reversed_data"
}

# Function to update a field in a scu_bl_cfg binary file, given the input/output filenames, field name, value and descriptor index
update_field()
{
  local input_file=$1
  local output_file=$2
  local field_name=$3
  local field_value=$4
  local descriptor_index=$5

  # Copy input_file to output_file
  file_size=$(stat -c%s "$input_file")
  dd if="$input_file" of="$output_file" bs=1 count="$file_size" 2>/dev/null

  # Validate the descriptor index for the relevant fields boot_image_source, boot_image_offset and boot_image_mode
  if [ "$descriptor_index" -lt 0 ] || [ "$descriptor_index" -gt 3 ]; then
    if [ "$field_name" = "boot_image_source" ] || [ "$field_name" = "boot_image_offset" ] || [ "$field_name" = "boot_image_mode" ]; then
      echo "Invalid descriptor index: $descriptor_index"
      return 1
    fi
  fi
  
  # Handle boot_image_source field, which is provided as a text string
  if [ "$field_name" = "boot_image_source" ]; then
    field_offset=$((descriptor_index * 8 + 12))
    field_size=1
    case $field_value in
      "bootstrap")
        field_value=$BOOT_SOURCE_BOOTSTRAP
        ;;
      "spi_flash")
        field_value=$BOOT_SOURCE_SPI_FLASH
        ;;
      "uart")
        field_value=$BOOT_SOURCE_UART
        ;;
      "pcie")
        field_value=$BOOT_SOURCE_PCIE
        ;;
      "emmc0")
        field_value=$BOOT_SOURCE_EMMC0
        ;;
      "emmc1")
        field_value=$BOOT_SOURCE_EMMC1
        ;;
      *)
        echo "Invalid boot_image_source value: $field_value"
        return 1
        ;;
    esac
    hex_value="\x$field_value"

  # Handle boot_image_offset field, which is provided as a hex string
  elif [ "$field_name" = "boot_image_offset" ]; then
    field_offset=$((descriptor_index * 8 + 8))
    field_size=4
    field_value=$(echo "$field_value" | sed 's/0x//')
    printf_format="%08x"
    hex_value=$(printf "\\x%s\\x%s\\x%s\\x%s" ${field_value:6:2} ${field_value:4:2} ${field_value:2:2} ${field_value:0:2})

  # Handle other fields
  else
    case $field_name in
      "boot_image_mode")
        if ! [[ $field_value =~ ^[0-7]+$ ]]; then
          echo "Invalid field_value=$field_value for field_name=$field_name. Must be in the range [0-7]."
          return 1
        fi
        field_offset=$((descriptor_index * 8 + 13))
        field_size=1
        ;;
      "boot_max_retries")
        if ! [[ $field_value =~ ^[0-9]+$ ]] || ((field_value < 0 || field_value > 255)); then
          echo "Invalid field_value=$field_value for field_name=$field_name. Must be a number in the range [0-255]."
          return 1
        fi
        field_offset=6
        field_size=1
        ;;
      "last_valid_desc_index")
        if ! [[ $field_value =~ ^[0-3]+$ ]]; then
          echo "Invalid field_value=$field_value for field_name=$field_name. Must be in the range [0-3]."
          return 1
        fi
        field_offset=40
        field_size=1
        ;;
      "debug_logs_enabled")
        if ! [[ $field_value =~ ^[0-1]+$ ]]; then
          echo "Invalid field_value=$field_value for field_name=$field_name. Must be 0 or 1."
          return 1
        fi
        field_offset=41
        field_size=1
        ;;
      *)
        echo "Invalid field name: $field_name"
        return 1
        ;;
    esac
    printf_format="\x%0${field_size}x"
    hex_value=$(printf "$printf_format" "$field_value" 2>/dev/null)
  fi

  # Modify the field at the specified offset with the specified value
  printf "${hex_value}" | dd of=$output_file bs=1 seek=$field_offset count=$field_size conv=notrunc 2>/dev/null
  return 0
}

usage ()
{
  echo "This script updates scu_bl_cfg binary file with new values, and recalculates CRC32"
  echo "Usage: update_scu_bl_cfg.sh <input_filename> <output_filename> <field name> <field value> <descriptor index>"
  return 0
}

# function to calculate the CRC32 value of a binary file and update the CRC32 value in the first 4 bytes of the file
function update_scu_bl_cfg_crc()
{
  input_file=$1

  # Read the binary file into a buffer
  binary_data=$(hexdump -v -e '4/1 "%02X" ""' "$input_file" | awk '{for(i=1;i<=NF;i+=4) printf "%s%s%s%s", $(i+3), $(i+2), $(i+1), $i}')

  # Remove the first 4 bytes (old CRC value) from the binary data
  binary_data=${binary_data:8}

  # Reverse the bytes of each 32-bit word
  reversed_data=""
  for ((i=0; i<${#binary_data}; i+=8)); do
    word=${binary_data:i:8}
    byte_reversed_data+=$(reverse_bytes $word)
  done

  # Bit reverse each byte of the binary data
  bit_reversed_data=$(reverse_bits_in_data "$byte_reversed_data")

  # Convert hex to binary and write to a temp binary file
  binary_buffer=$(echo "$bit_reversed_data" | sed 's/\(..\)/\\x\1/g')

  printf "$binary_buffer" > binary_output.bin

  # Calculate the CRC32 value
  crc32_value=$(crc32 binary_output.bin | awk '{print $1}')
  
  # remove the temp binary file
  rm binary_output.bin

  # Bit reverse the CRC32 value
  bit_reversed_crc=$(reverse_bits_in_data "$crc32_value")

  # XOR the result with 0xFFFFFFFF
  final_crc=$((0x$bit_reversed_crc ^ 0xFFFFFFFF))
  
  # Convert final_crc to binary format
  binary_crc=$(printf "%08x" $final_crc | sed 's/../\\x&/g')

  # Write the final CRC32 value to the first 4 bytes of the binary file
  printf "$binary_crc" | dd of="$input_file" bs=1 seek=0 count=4 conv=notrunc 2>/dev/null

  return 0
}

############################################################
## Main script starts here
############################################################

# Check if the correct number of arguments are provided
if [ "$#" -lt 3 ]; then
  usage
  exit 1
fi

# Assign the input filename to a variable
input_file=$1
output_file=$2
field_name=$3
field_value=$4

if [ "$#" -eq 5 ]; then
  descriptor_index=$5
else
  descriptor_index=-1
fi

# Update the relevant field with the relevant value in scu_bl_cfg
if ! update_field "$input_file" "$output_file" "$field_name" "$field_value" "$descriptor_index"; then
    echo "Failed to update field $field_name"
    exit 1
fi

# Update the CRC value in the output file
if ! update_scu_bl_cfg_crc "$output_file"; then
    echo "Failed to update CRC32 for file $output_file"
    exit 1
fi

