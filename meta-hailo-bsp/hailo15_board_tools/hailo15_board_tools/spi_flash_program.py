import argparse

from hailo15_board_tools.hailo15_spi_flash_program import Hailo15FlashManager
from hailo15_board_tools.flash_programmers.uart_recovery_manager import UartRecoveryCommunicator
from hailo15_board_tools.flash_programmers.ftdi_flash_programmer import FtdiFlashProgrammer


def convert_file_size_to_int(file_size):
    if file_size.startswith('0x'):
        size = int(file_size, 16)
    else:
        size = int(file_size)
    return size


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--file', type=str, required=True,
        help='Path to the binary image file to be programmed')
    parser.add_argument('--offset', type=int, default=0,
                        help='Start offset (in bytes) of the given image  from the beginning of the SPI flash.')
    parser.add_argument('--size', required=True,
                        help='Size (in bytes) of the given image.')
    parser.add_argument('--verify', type=int, choices=[0, 1], default=1,
                        help='Verify the written data by reading and comparing to the written data (default is true).')
    parser.add_argument('--uart-load', action='store_true',
                        help='Use UART for programming the SPI flash, (default is false).')
    parser.add_argument('--serial-device-name', default='/dev/ttyUSB3',
                        help='The serial device name (default is /dev/ttyUSB3).')

    args = parser.parse_args()
    file_size = convert_file_size_to_int(args.size)

    if args.uart_load:
        uart_comm = UartRecoveryCommunicator(args.serial_device_name)
        programmer = uart_comm.get_flash_programmer()
    else:
        programmer = FtdiFlashProgrammer()

    flash_manager = Hailo15FlashManager(programmer)

    flash_manager.programmer.open_interface()

    flash_manager.erase_and_program_flash(args.file, args.offset, file_size, args.verify)


if __name__ == '__main__':
    main()
