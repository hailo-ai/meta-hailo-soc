#!/usr/bin/env python

"""
The purpose of this application is to program the Hailo-15 board's flash with the relevant software and configurations.
"""

import argparse
import hashlib
import os
import time
import subprocess
import tempfile
import struct
from contextlib import contextmanager

from hailo15_board_tools.flash_programmers.flash_programmer import FlashProgrammer, logger
from hailo15_board_tools.flash_programmers.uart_recovery_manager import UartRecoveryCommunicator
from hailo15_board_tools.flash_programmers.ftdi_flash_programmer import FtdiFlashProgrammer


class FlashDataValidationException(Exception):
    pass


class Hailo15FlashManager():

    def __init__(self, programmer: FlashProgrammer, is_b_image: bool = False):
        self.programmer = programmer
        self.ab_offset = (self.FLASH_B_IMAGE_OFFSET - self.FLASH_A_IMAGE_OFFSET) if is_b_image else 0

    FLASH_OFFSET_SCU_BL = 0
    FLASH_SECTION_SIZE_SCU_BL = 0x6000
    FLASH_OFFSET_SCU_BL_CONFIG = 0x6000
    FLASH_SECTION_SIZE_SCU_BL_CONFIG = 0x1000
    FLASH_OFFSET_SCU_FW = 0x8000
    FLASH_SECTION_SIZE_SCU_FW = 0x38000
    FLASH_OFFSET_UBOOT_DEVICE_TREE = 0x40000
    FLASH_SECTION_SIZE_UBOOT_DEVICE_TREE = 0xF000
    FLASH_OFFSET_CUSTOMER_CERTIFICATE = 0x4F000
    FLASH_SECTION_SIZE_CUSTOMER_CERTIFICATE = 0x1000
    FLASH_OFFSET_SPL_UBOOT_BIN = 0x54000
    FLASH_SECTION_SIZE_UBOOT_SPL = 0x2C000
    FLASH_OFFSET_UBOOT_ENV = 0x50000
    FLASH_SECTION_SIZE_UBOOT_ENV = 0x4000

    FLASH_B_IMAGE_OFFSET = 0x80000
    FLASH_A_IMAGE_OFFSET = 0x8000

    def _program_file(self, file_path, offset, section_size, validate, add_md5=False):
        """ This function programs a given file to the SPI flash

        Args:
            file_path (str): The file path to program to the SPI flash
            offset (hex): The start offset in the SPI flash
            section_size (hex): The section size of the given file
            validate (bool, optional): Whether to validate content has been programmed successfully . Defaults to True.
            add_md5 (bool, optional): Whether to add md5 to the end of the file. Defaults to False.

        Raises:
            FlashDataValidationException: raise if validation failed or the file is larger than the section size
            SerialFlashValueError: raise if trying to write in address larger than flash (by write function)
        """
        data_to_write = None
        with open(file_path, 'rb') as input_file:
            data_to_write = input_file.read()

        if add_md5:
            md5 = hashlib.md5()
            md5.update(data_to_write)
            data_to_write = b''.join([data_to_write, md5.digest()])

        if section_size < len(data_to_write):
            raise FlashDataValidationException("Provided file is larger than expected")

        self.programmer.write(offset, data_to_write)

        if validate:
            read_data = self.programmer.read(offset, len(data_to_write))
            if read_data != data_to_write:
                raise FlashDataValidationException("Flash was not programmed successfully")
            else:
                logger.info('Flash program validatation passed successfully')

    def erase_and_program_flash(self, file_path, offset, reserved_section_size, validate, add_md5=False):
        if offset >= self.FLASH_A_IMAGE_OFFSET:
            offset += self.ab_offset

        raw_section_size = os.path.getsize(file_path)
        if add_md5:
            raw_section_size += hashlib.md5().digest_size

        if reserved_section_size < raw_section_size:
            raise FlashDataValidationException("Provided file is larger than expected")

        logger.info(f"Erasing flash from {hex(offset)} B to {hex(offset + raw_section_size)} B...")
        # Erase function validates that offset and section size are inbounds of flash device
        self.programmer.erase(offset, raw_section_size)
        logger.info("Erased successfully")
        time.sleep(1)
        self._program_file(file_path, offset, raw_section_size, validate=validate, add_md5=add_md5)
        logger.info(f"Provided file was successfully {file_path} programmed")

    def erase_uboot_env_from_flash(self):
        logger.info("Erasing U-Boot env...")
        # Erase function validates that offset and section size are inbounds of flash device
        self.programmer.erase(self.FLASH_OFFSET_UBOOT_ENV, self.FLASH_SECTION_SIZE_UBOOT_ENV)
        time.sleep(1)

    def erase_and_program_scu_fw(self, file_path, validate=1):
        logger.info(f"Programming SCU firmware file: {file_path}...")
        self.erase_and_program_flash(file_path,
                                     offset=self.FLASH_OFFSET_SCU_FW,
                                     reserved_section_size=self.FLASH_SECTION_SIZE_SCU_FW, validate=validate)

    def erase_and_program_scu_bl_config(self, validate=1):
        logger.info("Programming SCU bootloader config file...")

        with tempfile.NamedTemporaryFile(suffix=".bin") as bl_config:
            bl_config.write(struct.pack('<I', self.ab_offset))
            bl_config.flush()
            self.erase_and_program_flash(bl_config.name,
                                         offset=self.FLASH_OFFSET_SCU_BL_CONFIG,
                                         reserved_section_size=self.FLASH_SECTION_SIZE_SCU_BL_CONFIG, validate=validate)

    def erase_and_program_scu_bl(self, file_path, validate=1):
        self.erase_and_program_scu_bl_config(validate=validate)
        logger.info(f"Programming SCU bootloader file: {file_path}...")
        self.erase_and_program_flash(file_path,
                                     offset=self.FLASH_OFFSET_SCU_BL,
                                     reserved_section_size=self.FLASH_SECTION_SIZE_SCU_BL, validate=validate)

    def erase_and_program_uboot_spl(self, file_path, validate=1):
        self.erase_uboot_env_from_flash()
        logger.info(f"Programming U-Boot SPL file: {file_path}...")
        self.erase_and_program_flash(file_path,
                                     offset=self.FLASH_OFFSET_SPL_UBOOT_BIN,
                                     reserved_section_size=self.FLASH_SECTION_SIZE_UBOOT_SPL, validate=validate)

    @contextmanager
    def create_uboot_env(self, env_path, env_size):
        env_image_path = tempfile.NamedTemporaryFile(suffix=".bin")
        try:
            subprocess.run(["mkenvimage", "-s", str(env_size), "-o", env_image_path.name, env_path])
            yield env_image_path
        finally:
            env_image_path.close()

    def erase_and_program_uboot_env(self, file_path, validate=1):
        logger.info(f"Programming U-Boot env file: {file_path}...")
        with self.create_uboot_env(file_path, self.FLASH_SECTION_SIZE_UBOOT_ENV) as env_image_path:
            logger.info(f"Programming U-Boot env file: {env_image_path.name}...")
            self.erase_and_program_flash(env_image_path.name,
                                         offset=self.FLASH_OFFSET_UBOOT_ENV,
                                         reserved_section_size=self.FLASH_SECTION_SIZE_UBOOT_ENV, validate=validate)

    def erase_and_program_uboot_device_tree(self, file_path, validate=1):
        logger.info(f"Programming u-boot device-tree file: {file_path}...")
        self.erase_and_program_flash(file_path,
                                     offset=self.FLASH_OFFSET_UBOOT_DEVICE_TREE,
                                     reserved_section_size=self.FLASH_SECTION_SIZE_UBOOT_DEVICE_TREE, validate=validate)

    def erase_and_program_customer_certificate(self, file_path, validate=1):
        logger.info(f"Programming Customer certificate file: {file_path}...")
        self.erase_and_program_flash(file_path,
                                     offset=self.FLASH_OFFSET_CUSTOMER_CERTIFICATE,
                                     reserved_section_size=self.FLASH_SECTION_SIZE_CUSTOMER_CERTIFICATE,
                                     validate=validate)


def run(scu_firmware=None, scu_bootloader=None, bootloader=None, bootloader_env=None, uboot_device_tree=None,
        customer_cert=None, verify=True, uart_load=False, serial_device_name='/dev/ttyUSB3', jump_to_flash=False,
        is_b_image=False):
    if uart_load:
        uart_comm = UartRecoveryCommunicator(serial_device_name)
        programmer = uart_comm.get_flash_programmer()
    else:
        programmer = FtdiFlashProgrammer()

    flash_manager = Hailo15FlashManager(programmer, is_b_image)

    flash_manager.programmer.open_interface()

    if scu_firmware:
        flash_manager.erase_and_program_scu_fw(scu_firmware, validate=verify)
    if scu_bootloader:
        flash_manager.erase_and_program_scu_bl(scu_bootloader, validate=verify)
    if bootloader:
        flash_manager.erase_and_program_uboot_spl(bootloader, validate=verify)
    # U-Boot env program must follow the uboot program
    if bootloader_env:
        flash_manager.erase_and_program_uboot_env(bootloader_env, validate=verify)
    if customer_cert:
        flash_manager.erase_and_program_customer_certificate(customer_cert, validate=verify)
    if uboot_device_tree:
        flash_manager.erase_and_program_uboot_device_tree(uboot_device_tree, validate=verify)

    if uart_load and jump_to_flash:
        uart_comm.jump_bootrom_flash()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--scu-firmware',
        help='The path to the file containing the SCU firmware binary.')

    parser.add_argument(
        '--scu-bootloader',
        help='The path to the file containing the SCU bootloader binary.')

    parser.add_argument(
        '--bootloader',
        help='The path to the file containing the U-Boot SPL binary.')

    parser.add_argument(
        '--bootloader-env',
        help='The path to the file containing the U-Boot env.')

    parser.add_argument(
        '--uboot-device-tree',
        help='The path to the file containing the u-boot device tree.')

    parser.add_argument(
        '--customer-certificate',
        help='The path to the file containing the customer certificate.')

    parser.add_argument('--verify', type=int, choices=[0, 1], default=1,
                        help='Verify the written data by reviewing and comparing to the written data (default is true)')

    parser.add_argument('--uart-load', action='store_true',
                        help='Use UART for programming the SPI flash (default is false).')

    parser.add_argument('--serial-device-name', default='/dev/ttyUSB3',
                        help='The serial device name (default is /dev/ttyUSB3).')

    parser.add_argument('--b-image', action='store_true', help='Program B image.')

    args = parser.parse_args()

    run(args.scu_firmware, args.scu_bootloader, args.bootloader, args.bootloader_env, args.uboot_device_tree,
        args.customer_certificate, args.verify, args.uart_load, args.serial_device_name, args.b_image)


if __name__ == '__main__':
    main()
