from spiflash import serialflash
import math

from hailo15_board_tools.flash_programmers.flash_programmer import FlashProgrammer, logger

# Override with our device values
serialflash.N25QFlashDevice.DEVICES.clear()
serialflash.N25QFlashDevice.DEVICES = ({0xBA: 'Micron N25Q', 0xBB: 'Micron MT25'})
serialflash.N25QFlashDevice.SIZES.clear()
serialflash.N25QFlashDevice.SIZES = {0x15: 1 << 21, 0x16: 1 << 22, 0x17: 1 << 23, 0x18: 1 << 24, 0x21: 1 << 30}

serialflash.W25xFlashDevice.DEVICES.clear()
serialflash.W25xFlashDevice.DEVICES = {0x30: 'Winbond W25X', 0x40: 'Winbond W25Q', 0x60: 'Winbond W25Q'}
serialflash.W25xFlashDevice.SIZES.clear()
serialflash.W25xFlashDevice.SIZES = {0x11: 1 << 17, 0x12: 1 << 18, 0x13: 1 << 19, 0x14: 1 << 20,
                                     0x15: 2 << 20, 0x17: 8 << 20, 0x18: 16 << 20, 0x16: 32 << 20, 0x19: 256 << 20}

serialflash.Mx25lFlashDevice.DEVICES.clear()
serialflash.Mx25lFlashDevice.DEVICES = {0x9E: 'Macronix MX25D',
                                        0x26: 'Macronix MX25E',
                                        0x20: 'Macronix MX25E06',
                                        0x25: 'Macronix MX25U'}
serialflash.Mx25lFlashDevice.SIZES.clear()
serialflash.Mx25lFlashDevice.SIZES = {0x15: 2 << 20, 0x16: 4 << 20, 0x17: 8 << 20, 0x18: 16 << 20, 0x36: 32 << 20}


class FtdiFlashProgrammer(FlashProgrammer):

    FTDI_INTERFACE = 2
    FTDI_URL = f'ftdi://ftdi:4232h/{FTDI_INTERFACE}'

    def __init__(self, url=FTDI_URL, freq=30E6):
        self.url = url
        self.freq = freq

    def open_interface(self):
        self._flash_device = serialflash.SerialFlashManager().get_flash_device(url=self.url, cs=0, freq=self.freq)
        self.identify()

    def write(self, address, buffer_data):
        return self._flash_device.write(address, buffer_data)

    def read(self, address, length):
        return self._flash_device.read(address, length)

    def erase(self, address, length):
        subsector_size = self._flash_device.get_size('subsector')
        block_amount = math.ceil(length / subsector_size)
        section_size = block_amount * subsector_size
        return self._flash_device.erase(address, section_size)

    def identify(self):
        logger.info(f'flash detected "{self._flash_device}"')
