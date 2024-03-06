import serial
import time
import ctypes
import argparse
from hailo15_board_tools.flash_programmers.uart_recovery_manager\
        import UartRecoveryCommunicator  # noqa: E402

UART_BAUDRATE = 57600
UART_TIMEOUT = 2  # seconds
UART_RESPONSE_LENGTH = 3
UART_ACK = 0x55
FIRMWARE_ADDRESS = 0x20000
HEADER_ADDRESS = 0x88000
KEY_CERTIFICATE_ADDRESS = 0x88018
CONTENT_CERTIFICATE_ADDRESS = 0x886a8
MAX_CODE_RAM_SIZE = 0x50000
MAX_KEY_CERTIFICATE_SIZE = 0x690
MAX_CONTENT_CERTIFICATE_SIZE = 0x5F0
FIRMWARE_HEADER_MAGIC_HAILO15 = 0xE905DAAB
FIRMWARE_HEADER_VERSION_INITIAL = 0
SLEEP_TIME_INSTEAD_READ_SECOND = 0.1
SLEEP_TIME_AFTER_BOOT_SECOND = 2


class UploadException(Exception):
    pass


class InputException(Exception):
    pass


class FirmwareHeaderStruct(ctypes.LittleEndianStructure):
    _fields_ = [
        ("magic", ctypes.c_uint32),
        ("header_version", ctypes.c_uint32),
        ("firmware_major", ctypes.c_uint32),
        ("firmware_minor", ctypes.c_uint32),
        ("firmware_revision", ctypes.c_uint32),
        ("code_size", ctypes.c_uint32),
    ]


class SecureBootCertificateStruct(ctypes.LittleEndianStructure):
    _fields_ = [
        ("key_size", ctypes.c_uint32),
        ("content_size", ctypes.c_uint32),
    ]


def calc_checksum(data):
    sum1 = 0
    sum2 = 0

    for b in data:
        sum1 = (sum1 + b) % 255
        sum2 = (sum1 + sum2) % 255

    return ((sum2 << 8) + sum1)


class UartBootFWLoader():

    def __init__(self, is_secure_chip, serial_device_name):
        self.is_secure_chip = is_secure_chip
        self.serial_device_name = serial_device_name

    def load_file(self, firmware):
        firmware_binary_path = open(firmware, 'rb')
        firmware_binary_bin = firmware_binary_path.read()

        firmware_header_bin, firmware_code, key_certificate_bin, content_certificate_bin = \
            self.validate_bin_file_and_create_bin_files(firmware_binary_bin)

        end_transaction = bytes(10)

        time.sleep(SLEEP_TIME_AFTER_BOOT_SECOND)  # to make sure bootrom is out of reset

        s = serial.Serial(self.serial_device_name, UART_BAUDRATE, timeout=UART_TIMEOUT)

        if self.is_secure_chip:
            # read the first magic
            s.read(UART_RESPONSE_LENGTH)

        try:
            # write firmware
            self.write_to_uart(FIRMWARE_ADDRESS, firmware_code, s, self.is_secure_chip)

            # write header
            self.write_to_uart(HEADER_ADDRESS, firmware_header_bin, s, self.is_secure_chip)

            # write key_certificate
            self.write_to_uart(KEY_CERTIFICATE_ADDRESS, key_certificate_bin, s, self.is_secure_chip)

            # write content _certificate
            self.write_to_uart(CONTENT_CERTIFICATE_ADDRESS, content_certificate_bin, s, self.is_secure_chip)

            s.write(end_transaction)
        except UploadException:
            print("Upload failed, exiting")

    def write_to_uart(self, address, file_bin, s, secure_chip=False):
        address_bin = address.to_bytes(4, byteorder='little')
        length_bin = (len(file_bin)).to_bytes(4, byteorder='little')
        checksum = calc_checksum(address_bin + length_bin)
        checksum_bin = checksum.to_bytes(2, byteorder='little')

        s.write(address_bin)
        s.write(length_bin)
        s.write(checksum_bin)

        # on non-secure chips the UART1_TX is muxed with DFT_JTAG_TDO
        # on secure chips we should wait for ack, in non-secure chips we
        # can just ignore the checksum and add sleeps instead

        if secure_chip:
            ack = s.read(UART_RESPONSE_LENGTH)
            if (len(ack) != UART_RESPONSE_LENGTH) or (ack[0] != UART_ACK) or (checksum_bin != ack[1:3]):
                print("invalid header checksum: " + str(ack))
                raise UploadException("invalid header")
        else:
            time.sleep(SLEEP_TIME_INSTEAD_READ_SECOND)

        s.write(file_bin)

        checksum = calc_checksum(file_bin)
        checksum_bin = checksum.to_bytes(2, byteorder='little')

        if secure_chip:
            ack = s.read(UART_RESPONSE_LENGTH)
            if (len(ack) != UART_RESPONSE_LENGTH) or (ack[0] != UART_ACK) or (checksum_bin != ack[1:3]):
                print("invalid data ack")
                raise UploadException("invalid data ack")
        else:
            time.sleep(SLEEP_TIME_INSTEAD_READ_SECOND)

    def validate_bin_file_and_create_bin_files(self, firmware_binary_bin):
        header_size = ctypes.sizeof(FirmwareHeaderStruct)
        certificate_header_size = ctypes.sizeof(SecureBootCertificateStruct)

        firmware_header_bin = firmware_binary_bin[0:header_size]

        firmware_header_struct = FirmwareHeaderStruct.from_buffer_copy(firmware_header_bin)

        if FIRMWARE_HEADER_MAGIC_HAILO15 != firmware_header_struct.magic:
            raise InputException("Incorrect firmware header magic")
        if FIRMWARE_HEADER_VERSION_INITIAL != firmware_header_struct.header_version:
            raise InputException("Incorrect firmware header version")
        if MAX_CODE_RAM_SIZE < firmware_header_struct.code_size:
            raise InputException("The firmware provided is too large")

        code_size = firmware_header_struct.code_size

        print(f"UART recovery firmware version which is now loaded: {firmware_header_struct.firmware_major}.\
{firmware_header_struct.firmware_minor}")

        firmware_code = firmware_binary_bin[header_size:(header_size + code_size)]

        certificate_header_offset = header_size + code_size

        certificate_header_bin = firmware_binary_bin[certificate_header_offset:
                                                     (certificate_header_offset + certificate_header_size)]
        secure_boot_certificate_struct = SecureBootCertificateStruct.from_buffer_copy(certificate_header_bin)

        if MAX_KEY_CERTIFICATE_SIZE < secure_boot_certificate_struct.key_size:
            raise InputException("The key certificate provided is too large")
        if MAX_CONTENT_CERTIFICATE_SIZE < secure_boot_certificate_struct.content_size:
            raise InputException("The content certificate provided is too large")

        key_certificate_size = secure_boot_certificate_struct.key_size
        content_certificate_size = secure_boot_certificate_struct.content_size

        key_certificate_offset = certificate_header_offset + certificate_header_size

        content_certificate_offset = key_certificate_offset + key_certificate_size

        if (content_certificate_offset + content_certificate_size) != \
                len(firmware_binary_bin):
            raise InputException("Field sizes of either code/certificates don't match the actual firmware size")

        key_certificate_bin = firmware_binary_bin[key_certificate_offset:
                                                  (key_certificate_offset + key_certificate_size)]
        content_certificate_bin = firmware_binary_bin[content_certificate_offset:
                                                      (content_certificate_offset + content_certificate_size)]

        return firmware_header_bin, firmware_code, key_certificate_bin, content_certificate_bin


def run(firmware, is_secure_chip=True, serial_device_name='/dev/ttyUSB3'):
    try:
        uart_boot_fw_loader = UartBootFWLoader(is_secure_chip, serial_device_name)
        uart_boot_fw_loader.load_file(firmware)
        time.sleep(1)
        uart_comm = UartRecoveryCommunicator(serial_device_name)
        programmer = uart_comm.get_flash_programmer()
        programmer.open_interface()
    except Exception as e:
        print(f"Error: {e}")
    else:
        print("UART recovery firmware loaded successfully to the device")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--firmware', help='The path to the file containing the firmware binary')

    parser.add_argument('--is-secure-chip', action='store_true',
                        help='Whether the given chip is in a secure LCS')

    parser.add_argument('--serial-device-name', default='/dev/ttyUSB3',
                        help='The serial device file name (default /dev/ttyUSB3)')

    args = parser.parse_args()

    run(args.firmware, args.is_secure_chip, args.serial_device_name)


if __name__ == '__main__':
    main()
