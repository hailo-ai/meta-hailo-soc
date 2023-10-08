/******************************************************************************
*  Legal notice:
* Copyright (C) 2017-2023 Hailo Technologies Ltd.
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 1. Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimer in the
* documentation and/or other materials provided with the distribution.
* 3. Neither the name of the copyright holder nor the names of its
* contributors may be used to endorse or promote products derived from
* this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*
******************************************************************************/

#ifndef REGCONFIG_FILENAME
#error "Please define REGCONFIG_FILENAME (-D to the compiler) to the path of your regconfig file surrounded by qoutation marks"
#endif
#include REGCONFIG_FILENAME

#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <assert.h>
#include <openssl/sha.h>

#define ARRAY_SIZE(arr) (sizeof(arr)/sizeof(0[arr]))

#define DDR_CONFIGURATION_HEADER_MAGIC (0xB725E43D)
#define DDR_CONFIGURATION_HEADER_REVISION (3)

#define DDR_CTRL_REGS_COUNT (0x19F)
#define DDR_PI_REGS_COUNT (0x12C)
#define DDR_PHY_REGS_COUNT (0x58F)

_Static_assert(sizeof(DDR_ctrl_registers) == sizeof(uint32_t) * DDR_CTRL_REGS_COUNT, "wrong size of DDR_ctrl_registers");
_Static_assert(sizeof(DDR_PI_registers) == sizeof(uint32_t) * DDR_PI_REGS_COUNT, "wrong size of DDR_PI_registers");
_Static_assert(sizeof(DDR_PHY_registers) == sizeof(uint32_t) * DDR_PHY_REGS_COUNT, "wrong size of DDR_PHY_registers");

enum ddr_working_mode {
    DDR_WORKING_MODE_NORMAL,
    DDR_WORKING_MODE_DDRAPP,
    DDR_WORKING_MODE_INTEGRATION,
};

enum ddr_ctrl_ecc_mode {
    DDR_CTRL_ECC_MODE_DISABLED,
    DDR_CTRL_ECC_MODE_ENABLED, /* ECC enabled, detection disabled, correction disabled */
    DDR_CTRL_ECC_MODE_DETECTION, /* ECC enabled, detection enabled, correction disabled */
    DDR_CTRL_ECC_MODE_CORRECTION, /* ECC enabled, detection enabled, correction enabled */
};

struct ddr_config_space {
    uint32_t header_magic;                                  /* offset: 0x0 */
    uint32_t header_revision;                               /* offset: 0x4 */
    uint32_t identifier;                                    /* offset: 0x8 */
    uint32_t ctrl_regs[DDR_CTRL_REGS_COUNT];                /* offset: 0xC */
    uint32_t pi_regs[DDR_PI_REGS_COUNT];                    /* offset: 0x68C */
    uint32_t phy_regs[DDR_PHY_REGS_COUNT];                  /* offset: 0xB3C */
    uint32_t working_mode;                                  /* offset: 0x2174 */
    uint32_t ecc_mode;                                      /* offset: 0x2178 */
    uint32_t bist_enable;                                   /* offset: 0x217C */
    uint32_t operational_freq;                              /* offset: 0x2180 */
    uint32_t stop_before_controller_start;                  /* offset: 0x2184 */
    uint32_t f1_frequency;                                  /* offset: 0x2188 */
    uint32_t f2_frequency;                                  /* offset: 0x218C */
    uint32_t temperature_poll_period_ms;                    /* offset: 0x2190 */
    uint32_t temperature_retraining_threshold_millicelsius; /* offset: 0x2194 */
    uint32_t periodic_io_calibration_disable;               /* offset: 0x2198 */
    uint32_t periodic_vref_training_enable;                 /* offset: 0x219C */
    uint32_t periodic_calvl_training_enable;                /* offset: 0x21A0 */
    uint32_t periodic_wrlvl_training_enable;                /* offset: 0x21A4 */
    uint32_t periodic_rdlvl_training_enable;                /* offset: 0x21A8 */
    uint32_t periodic_rdlvl_gate_training_enable;           /* offset: 0x21AC */
    uint32_t periodic_wdqlvl_training_enable;               /* offset: 0x21B0 */
};

const uint32_t allowed_frequencies[] = {
    50000000, 100000000, 1598000000, 200000000,
    400000000, 800000000, 1200000000, 1600000000,
    2000000000, 2130000000, 2132000000, 2133000000
};

static bool is_frequency_allowed(uint32_t freq)
{
    for (unsigned int i = 0; i < ARRAY_SIZE(allowed_frequencies); i++) {
        if (freq == allowed_frequencies[i]) {
            return true;
        }
    }
    return false;
}

uint32_t calculate_configuration_identifier(const void* config, uint32_t size) {
    unsigned char hash[SHA256_DIGEST_LENGTH] = {0};
    uint32_t identifier = 0;
    SHA256_CTX sha256;

    SHA256(config, size, hash);

    memcpy(&identifier, hash, sizeof(identifier));
    return identifier;
}

int main(int argc, char* argv[])
{
    char *ecc_mode_str, *bist_enable_str, *operational_freq_str, *f1_freq_str, *f2_freq_str, *out_filename;
    struct ddr_config_space config_space = {0};
    uint32_t ecc_mode, operational_freq_index, f1_freq, f2_freq;
    bool bist_enable;
    FILE *file;
    size_t bytes_written;

    if (argc != 7) {
        fprintf(stderr, "Argument count error. Usage: %s [ecc-mode=disabled/enabled/detection/correction] [bist-enable=enable/disable] [operational-freq-index=f0/f1/f2] [f1-frequency-hz] [f2-frequency-hz] [out-file]\n", argv[0]);
        return 1;
    }

    ecc_mode_str = argv[1];
    bist_enable_str = argv[2];
    operational_freq_str = argv[3];
    f1_freq_str = argv[4];
    f2_freq_str = argv[5];
    out_filename = argv[6];

    if (!strcmp(ecc_mode_str, "disabled")) {
        ecc_mode = DDR_CTRL_ECC_MODE_DISABLED;
    } else if (!strcmp(ecc_mode_str, "enabled")) {
        ecc_mode = DDR_CTRL_ECC_MODE_ENABLED;
    } else if (!strcmp(ecc_mode_str, "detection")) {
        ecc_mode = DDR_CTRL_ECC_MODE_DETECTION;
    } else if (!strcmp(ecc_mode_str, "correction")) {
        ecc_mode = DDR_CTRL_ECC_MODE_CORRECTION;
    } else {
        fprintf(stderr, "bad ecc-mode parameter\n");
        return 2;
    }

    if (!strcmp(bist_enable_str, "disabled")) {
        bist_enable = false;
    } else if (!strcmp(bist_enable_str, "enabled")) {
        bist_enable = true;
    } else {
        fprintf(stderr, "bad bist-enable parameter\n");
        return 3;
    }

    if (!strcmp(operational_freq_str, "f0")) {
        operational_freq_index = 0;
    } else if (!strcmp(operational_freq_str, "f1")) {
        operational_freq_index = 1;
    } else if (!strcmp(operational_freq_str, "f2")) {
        operational_freq_index = 2;
    } else {
        fprintf(stderr, "bad operational-freq-index parameter\n");
        return 4;
    }

    f1_freq = strtoul(f1_freq_str, NULL, 10);
    if (!is_frequency_allowed(f1_freq)) {
        fprintf(stderr, "bad f1-frquency-hz parameter\n");
        return 5;
    }

    f2_freq = strtoul(f2_freq_str, NULL, 10);
    if (!is_frequency_allowed(f2_freq)) {
        fprintf(stderr, "bad f2-frquency-hz parameter\n");
        return 6;
    }

    config_space.header_magic = DDR_CONFIGURATION_HEADER_MAGIC;
    config_space.header_revision = DDR_CONFIGURATION_HEADER_REVISION;
    memcpy(config_space.ctrl_regs, DDR_ctrl_registers, sizeof(DDR_ctrl_registers));
    memcpy(config_space.pi_regs, DDR_PI_registers, sizeof(DDR_PI_registers));
    memcpy(config_space.phy_regs, DDR_PHY_registers, sizeof(DDR_PHY_registers));
    config_space.working_mode = DDR_WORKING_MODE_NORMAL;
    config_space.ecc_mode = ecc_mode;
    config_space.bist_enable = bist_enable;
    config_space.operational_freq = operational_freq_index;
    config_space.f1_frequency = f1_freq;
    config_space.f2_frequency = f2_freq;
    config_space.temperature_poll_period_ms = 1000;
    config_space.temperature_retraining_threshold_millicelsius = 5000;
    config_space.periodic_io_calibration_disable = false;
    config_space.periodic_vref_training_enable = false;
    config_space.periodic_calvl_training_enable = false;
    config_space.periodic_wrlvl_training_enable = false;
    config_space.periodic_rdlvl_training_enable = false;
    config_space.periodic_rdlvl_gate_training_enable = false;
    config_space.periodic_wdqlvl_training_enable = false;

    /* must be computed after configuration is populated */
    config_space.identifier = calculate_configuration_identifier(&config_space, sizeof(config_space));

    file = fopen(out_filename, "w");
    if (!file) {
        fprintf(stderr, "Could not open output file for writing\n");
        return 7;
    }

    bytes_written = fwrite(&config_space, 1, sizeof(config_space), file);
    if (bytes_written != sizeof(config_space)) {
        fprintf(stderr, "Writing config to output file failed\n");
        return 8;
    }

    if(fclose(file)) {
        fprintf(stderr, "Closing output file failed\n");
        return 9;
    }

    return 0;
}
