#!/bin/sh

# stop on errors
set -e

if [ $# -ne 6 ]; then
    echo "Usage: ./hailo15_boot_image_sign.sh [cryptocell-312-runtime-path] [customer-keypair] [input_binary_name] [soc (hailo15/hailo15l/hailo10h2)] [binary_type (image/devicetree)] [output_signed_binary_name]"
    exit 1
fi

# get script directory
script_dir=$(realpath $(dirname "$0"))

# parse arguments
cc_runtime_path=$(realpath $1)
customer_keypair=$(realpath $2)
input_binary=$(realpath $3)
soc=$4
binary_type=$5
output_signed_binary=$(realpath $6)

# check valid soc
if [ "$soc" != "hailo15" ] && [ "$soc" != "hailo15l" ] && [ "$soc" != "hailo10h2" ]; then
    echo "Error: Invalid soc - should be either 'hailo15', 'hailo15l', 'hailo10h2'"
    exit 1
fi

if [ "$binary_type" = "image" ]; then
    load_address="0x55000364"
elif [ "$binary_type" = "devicetree" ]; then
    load_address="0x90368"
    if [ "$soc" = "hailo10h2" ]; then
        # base address is 0x80000 for hailo10h2
        load_address="0x80368"
    fi
else
    echo "Error: Invalid binary type - should be either 'image' or 'devicetree'"
    exit 1
fi

# temporary files used during the signing process
temporary_directory=$(mktemp -d)
padded_input_binary="${temporary_directory}/padded_input_file.bin"
images_table_file="${temporary_directory}/images_table.txt"
certificate_binary_file="${temporary_directory}/certificate.bin"
certificate_config_file="${temporary_directory}/certificate_config.txt"

# change to the temporary directory, so that we can cleanup the temporary files easily
cd ${temporary_directory}

# the cryptocell library expects binaries padded to 4 bytes
# so we have to add padding in case the binary is not aligned to 4 byte
dd if=${input_binary} of=${padded_input_binary} ibs=4 conv=sync

# calculate the size of the padded input binary
padded_input_binary_size=$(printf "0x%x" `stat -c "%s" "${padded_input_binary}"`)

# create images table file from template
image=${padded_input_binary} load_address=${load_address} binary_size=${padded_input_binary_size} envsubst \
    < ${script_dir}/hailo15_images_table_template.txt > ${images_table_file}

# create the certificate configuration file from template
cert_keypair=${customer_keypair} images_table=${images_table_file} content_certificate=${certificate_binary_file} envsubst \
    < ${script_dir}/hailo15_content_certificate_config_template.txt > ${certificate_config_file}

# sign the binary using the cryptocell library
python3 ${cc_runtime_path}/utils/bin/cert_sb_content_util.py ${certificate_config_file} -cfg_file ${cc_runtime_path}/utils/src/proj.cfg

# generate the signed binary by concatenating the certificate and the padded input binary
cat ${certificate_binary_file} ${padded_input_binary} > ${output_signed_binary}

# cleanup temporary files
rm -rf ${temporary_directory}
