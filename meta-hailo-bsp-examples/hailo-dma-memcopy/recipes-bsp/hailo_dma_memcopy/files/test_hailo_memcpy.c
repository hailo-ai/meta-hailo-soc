#include <fcntl.h>
#include <asm/types.h>
#include <sys/ioctl.h>
#include <stddef.h>
#include <sys/mman.h>
#include <stdio.h>
#include <stdbool.h>
#include <stdint.h>
#include <unistd.h>
#include <string.h>
// Should be included when compiled with the kernel insted of line after ## $$ ##
#include <linux/dma-heap.h>
#include <linux/dma-buf.h>

struct dma_copy_info {
	int src_fd;
    int dst_fd;
	unsigned long length;
    int status;
    bool is_dma_buff;
    unsigned long virt_src_addr;
    unsigned long virt_dst_addr;
};

#define GP_DMA_XFER  _IOWR('a', 'a', struct dma_copy_info *)

static int dmabuf_sync(int fd, int start_stop)
{
	struct dma_buf_sync sync = {
		.flags = start_stop | DMA_BUF_SYNC_RW,
	};

	return ioctl(fd, DMA_BUF_IOCTL_SYNC, &sync);
}

void dmabuf_write(char *buf, int len)
{
	printf("%s[%d] buf-virt-addr[%p], len[%d]\n", __func__, __LINE__, buf,
	       len);
	for (size_t i = 0; i < len; ++i) {
		buf[i] = (i % 2) ? 0x55 : 0xAA;
	}
}

static int map_dma_memory(char *name,
			  struct dma_heap_allocation_data *heap_data,
			  void **mapped_memory)
{
	int ret = 0;

	int fd = open(name, O_RDWR);
	if (fd < 0) {
		perror("Error on open");
		goto exit;
	}
	ret = ioctl(fd, DMA_HEAP_IOCTL_ALLOC, heap_data);
	if (ret)
		goto exit;

	*mapped_memory = mmap(NULL, heap_data->len, PROT_READ | PROT_WRITE,
			     MAP_SHARED | MAP_POPULATE, heap_data->fd, 0);
	if (*mapped_memory == MAP_FAILED) {
		perror("mmap");
		close(fd);
		ret = -1;
		goto exit;
	}

	ret = 0;
exit:
	return ret;
}

static void unmap_dma_memory(void *mapped_memory,
			     struct dma_heap_allocation_data *heap_data)
{
	if (munmap(mapped_memory, heap_data->len) == -1)
		printf("munmap");

	close(heap_data->fd);
}

void virt_2_phys(char *prefix, void *virtual_address)
{
	const uint64_t page_size = getpagesize();
	const uint64_t page_length = 8;
	const uint64_t page_shift = 12;
	uint64_t page_offset, page_number, phy_address;
	int pagemap;

	pagemap = open("/proc/self/pagemap", O_RDONLY);
	if (pagemap < 0) {
		printf("open");
		return;
	}

	page_offset = (((uint64_t)virtual_address) / page_size * page_length);
	if (lseek(pagemap, page_offset, SEEK_SET) != page_offset) {
		perror("lseek");
		close(pagemap);
		return;
	}

	page_number = 0;
	if (read(pagemap, &page_number, sizeof(page_number)) !=
	    sizeof(page_number)) {
		perror("read");
		close(pagemap);
		return;
	}
	page_number &= 0x7FFFFFFFFFFFFFULL;

	close(pagemap);

	phy_address = ((page_number << page_shift) +
		       (((uint64_t)virtual_address) % page_size));
	printf("%s: virt-addr[%p] ==>> phys-addr[%lx]\n", prefix,
	       virtual_address, phy_address);
}

int main()
{
	struct dma_heap_allocation_data heap_data_src = {
		.len = getpagesize() * 2,
		.fd_flags = O_RDWR | O_CLOEXEC,
	};
	struct dma_heap_allocation_data heap_data_dst = {
		.len = getpagesize() * 2,
		.fd_flags = O_RDWR | O_CLOEXEC,
	};
	void* src_addr = NULL;
	void* dst_addr = NULL;
	int dev = 0;

	int ret = map_dma_memory("/dev/dma_heap/linux,cma", &heap_data_src,
				 &src_addr);
	if (ret)
		goto exit;
	ret = map_dma_memory("/dev/dma_heap/linux,cma", &heap_data_dst,
			     &dst_addr);
	if (ret)
		goto exit;

	virt_2_phys("src", src_addr);
	virt_2_phys("dst", dst_addr);
    
    dmabuf_sync(heap_data_src.fd, DMA_BUF_SYNC_START);
	dmabuf_write(src_addr, heap_data_src.len);
	dmabuf_sync(heap_data_src.fd, DMA_BUF_SYNC_END);
		
	 __builtin___clear_cache(src_addr, src_addr + heap_data_src.len);
	
    dmabuf_sync(heap_data_dst.fd, DMA_BUF_SYNC_START);
	memset(dst_addr, 0, heap_data_dst.len);
    dmabuf_sync(heap_data_dst.fd, DMA_BUF_SYNC_END);

	 __builtin___clear_cache(dst_addr, dst_addr + heap_data_src.len);

	struct dma_copy_info ioctl_data = {
		.src_fd = heap_data_src.fd,
		.dst_fd = heap_data_dst.fd,
		.length = heap_data_dst.len,
		.is_dma_buff = true,
		.status = 0,
	};

	dev = open("/dev/dma_memcpy", O_WRONLY);
	if (dev == -1) {
		printf("Opening was not possible!\n");
		ret = -1;
		goto exit;
	}

	
	ret = ioctl(dev, GP_DMA_XFER, &ioctl_data);
	if (ret)
		printf("ioctl fail with return value %d and status %d\n",ret , ioctl_data.status);
	if(ioctl_data.status != 0)
		printf("Copy fail with status %d\n",  ioctl_data.status);
	if(ioctl_data.status == 0)
		printf("ioctl Copy was successfull \n");
    
    dmabuf_sync(heap_data_dst.fd, DMA_BUF_SYNC_START);
	dmabuf_sync(heap_data_dst.fd, DMA_BUF_SYNC_END);
	sleep(3);
	
	
	int rc =0;
	for(int i=0; i< heap_data_dst.len; i++) {
		if (((char*)src_addr)[i] != ((char*)dst_addr)[i]){
			printf("Copy Fail for i = %d dst_addr = %02x \n", i, ((char*)dst_addr)[i] );
			rc = -1;
			break;
		}
			
	}
	if (rc == 0)
		printf("Copy was successfull!\n");

	ret = 0;

exit:
	if (ret) {
		perror("Test failed");
	}
	if (src_addr)
		unmap_dma_memory(src_addr, &heap_data_src);

	if (dst_addr)
		unmap_dma_memory(dst_addr, &heap_data_dst);
	if (dev)
		close(dev);
	return ret;
}


// need to run before modprobe hailo15_gp_memcopy; mknod /dev/dma_memcpy c 64 0 