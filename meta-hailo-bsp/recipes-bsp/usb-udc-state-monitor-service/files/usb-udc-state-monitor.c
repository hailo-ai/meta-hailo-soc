#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <poll.h>
#include <signal.h>
#include <syslog.h>

#define DEFAULT_UDC_STATE_PATH "/sys/class/udc/280000.cdns-usb3/state"
#define MAX_STATE_LEN 64

static volatile sig_atomic_t running = 1;

static void sig_handler(int sig)
{
	(void)sig;
	running = 0;
}

static int read_state(int fd, char *buf, size_t len)
{
	ssize_t n;

	if (lseek(fd, 0, SEEK_SET) < 0)
		return -1;

	n = read(fd, buf, len - 1);
	if (n <= 0)
		return -1;

	/* Strip trailing newline */
	if (buf[n - 1] == '\n')
		n--;
	buf[n] = '\0';

	return 0;
}

int main(int argc, char *argv[])
{
	const char *path = (argc > 1) ? argv[1] : DEFAULT_UDC_STATE_PATH;
	char prev_state[MAX_STATE_LEN];
	char curr_state[MAX_STATE_LEN];
	struct pollfd pfd;
	int fd;

	signal(SIGTERM, sig_handler);
	signal(SIGINT, sig_handler);

	openlog("usb-udc-state", LOG_PID, LOG_DAEMON);

	fd = open(path, O_RDONLY);
	if (fd < 0) {
		syslog(LOG_ERR, "Failed to open %s", path);
		closelog();
		return EXIT_FAILURE;
	}

	if (read_state(fd, prev_state, sizeof(prev_state)) < 0) {
		syslog(LOG_ERR, "Failed to read initial state from %s", path);
		close(fd);
		closelog();
		return EXIT_FAILURE;
	}

	syslog(LOG_INFO, "Monitoring UDC state (initial: %s)", prev_state);

	pfd.fd = fd;
	pfd.events = POLLPRI | POLLERR;

	while (running) {
		int ret = poll(&pfd, 1, 1000);

		if (ret < 0) {
			if (running)
				syslog(LOG_ERR, "poll() failed");
			break;
		}

		if (read_state(fd, curr_state, sizeof(curr_state)) < 0)
			continue;

		if (strcmp(curr_state, prev_state) != 0) {
			syslog(LOG_INFO, "UDC state changed: %s -> %s",
			       prev_state, curr_state);
			strcpy(prev_state, curr_state);
		}
	}

	syslog(LOG_INFO, "Stopped monitoring UDC state");
	close(fd);
	closelog();

	return EXIT_SUCCESS;
}
