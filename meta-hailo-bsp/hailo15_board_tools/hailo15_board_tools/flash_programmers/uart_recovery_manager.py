import serial

from hailo15_board_tools.flash_programmers.flash_programmer import FlashProgrammer, logger

FIRMWARE_VERSION_MAJOR = 1
FIRMWARE_VERSION_MINOR = 3


class UartRecoveryCommunicator:

    FW_VERSION_OPCODE = 0x0
    JEDEC_OPCODE = 0x1
    WRITE_OPCODE = 0x2
    READ_OPCODE = 0x3
    ERASE_SECTOR_OPCODE = 0x4
    JUMP_BOOTROM_FLASH_OPCODE = 0x5
    ERASE_CHIP_OPCODE = 0x6
    UART_BAUDRATE = 115200
    UART_TIMEOUT = 2  # seconds
    JEDEC_ID_LENGTH = 4
    ERASE_SECTOR_END_ACK = 0x55
    ERASE_CHIP_END_ACK = 0x56

    def __init__(self, serial_device_name):
        self.serial_device_name = serial_device_name

    def open_serial(self):
        self._serial = serial.Serial(self.serial_device_name, self.UART_BAUDRATE, timeout=self.UART_TIMEOUT)

    def _serial_read(self, size):
        buff = self._serial.read(size)
        if len(buff) == 0:
            raise Exception("Got serial read timeout")
        return buff

    def get_flash_programmer(self):
        return UartRecoveryFlashProgrammer(self)

    def get_fw_version(self):
        opcode = self.FW_VERSION_OPCODE
        opcode_bin = opcode.to_bytes(1, byteorder='little')
        self._serial.write(opcode_bin)
        firmware_major = int.from_bytes(self._serial_read(4), "big")
        firmware_minor = int.from_bytes(self._serial_read(4), "big")
        return firmware_major, firmware_minor

    def get_jedec_id(self):
        opcode = self.JEDEC_OPCODE
        opcode_bin = opcode.to_bytes(1, byteorder='little')
        self._serial.write(opcode_bin)
        return self._serial_read(self.JEDEC_ID_LENGTH).hex()

    def write(self, address, buffer_data):
        opcode = self.WRITE_OPCODE
        opcode_bin = opcode.to_bytes(1, byteorder='little')
        address_bin = address.to_bytes(4, byteorder='little')
        length_bin = (len(buffer_data)).to_bytes(4, byteorder='little')
        self._serial.write(opcode_bin)
        self._serial.write(address_bin)
        self._serial.write(length_bin)
        self._serial.write(bytearray(buffer_data))

    def erase(self, address):
        opcode = self.ERASE_SECTOR_OPCODE
        opcode_bin = opcode.to_bytes(1, byteorder='little')
        address_bin = address.to_bytes(4, byteorder='little')
        self._serial.write(opcode_bin)
        self._serial.write(address_bin)
        end_ack = self._serial_read(1)
        assert end_ack == self.ERASE_SECTOR_END_ACK.to_bytes(1, byteorder='little'), "Sector erase didn't succeeded"

    def read(self, address, length):
        opcode = self.READ_OPCODE
        opcode_bin = opcode.to_bytes(1, byteorder='little')
        address_bin = address.to_bytes(4, byteorder='little')
        length_bin = length.to_bytes(4, byteorder='little')
        self._serial.write(opcode_bin)
        self._serial.write(address_bin)
        self._serial.write(length_bin)
        read_data_buffer = self._serial_read(length)
        return bytearray(read_data_buffer)

    def jump_bootrom_flash(self):
        opcode = self.JUMP_BOOTROM_FLASH_OPCODE
        opcode_bin = opcode.to_bytes(1, byteorder='little')
        self._serial.write(opcode_bin)


class UartRecoveryFlashProgrammer(FlashProgrammer):
    QSPI_SECTOR_SIZE = (4*1024)

    def __init__(self, comm: UartRecoveryCommunicator):
        self.comm = comm

    def identify(self):
        try:
            firmware_major, firmware_minor = self.comm.get_fw_version()
        except Exception:
            raise Exception("could not connect to the recovery agent please \
                            try the following:\n \
                            1. Make sure bootstrap set to boot from uart.\n \
                            2. The USB cable connected correctly\n \
                            3. Reset the target and try again")

        if (firmware_major != FIRMWARE_VERSION_MAJOR) or (firmware_minor != FIRMWARE_VERSION_MINOR):
            raise Exception("Incompatibility between the FW version and the script version")

        logger.info(
            f'UART recovery load script version: {firmware_major}.{firmware_minor}'
        )

        jedec_id = self.comm.get_jedec_id()
        jedec_id = ''.join('{:02x}'.format(x) for x in bytearray.fromhex(jedec_id)[::-1])

        logger.info(
            f'flash detected, flash jedec_id: 0x{jedec_id}'
        )

    def write(self, address, buffer_data):
        self.comm.write(address, buffer_data)

    def erase(self, address, length):
        for sector_offset in range(0, length, self.QSPI_SECTOR_SIZE):
            self.comm.erase(address + sector_offset)

    def read(self, address, length):
        read_data_buffer = bytearray()
        for read_offset in range(0, length, self.QSPI_SECTOR_SIZE):
            if read_offset + self.QSPI_SECTOR_SIZE > length:
                bytes_to_read = length - read_offset
            else:
                bytes_to_read = self.QSPI_SECTOR_SIZE
            read_data_buffer += self.comm.read(address + read_offset, bytes_to_read)
        return read_data_buffer

    def open_interface(self):
        self.comm.open_serial()
        self.identify()
