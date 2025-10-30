#include <chrono>
#include <ctime>
#include <cstdio>
#include <string>
#include <unistd.h>
#include <getopt.h>
#include <stdint.h>
#include "throttling_manager.h"

#define DEFAULT_MAX_COOLING_DURATION_MSEC 5000ULL

bool enteringFullPerfState_timer_started = false;
bool denoise_ctr = false;
bool stream_ctr = true;
uint64_t max_cooling_duration_msec = DEFAULT_MAX_COOLING_DURATION_MSEC;
bool batch_mode = false;
int batch_duration_sec;

std::string ctrl_info(ThrottlingManager &mgr) {
    char buffer[128];
    std::snprintf(buffer, sizeof(buffer), 
        "state[curr(%s), prev(%s)]: stream[%s], denoise[%s]", 
        mgr.currStateToString().c_str(), 
        mgr.prevStateToString().c_str(), 
        stream_ctr ? "on" : "off", 
        denoise_ctr ? "on" : "off");
    return std::string(buffer);
}

static uint64_t get_monotonic_time_msec(void)
{
	struct timespec ts;

	// CLOCK_MONOTONIC is not affected by system time changes
	if (clock_gettime(CLOCK_MONOTONIC_RAW, &ts) == -1) {
		perror("clock_gettime");
		return -1;
	}

	return (uint64_t)ts.tv_sec * 1000LL + ts.tv_nsec / 1000000LL;
}

// Callback function definition
void onTimerExpire(ThrottlingManager &mgr)
{
    std::cout << "  - CB[timer]::_start_: " << ctrl_info(mgr) << std::endl;
    if (mgr.getCurrStateId() == ThrottlingStateId::FULL_PERFORMANCE) {
        denoise_ctr = true;
        enteringFullPerfState_timer_started = false;
        std::cout << "  - CB[timer]: enabling denoise" << std::endl;
    }
    std::cout << "  - CB[timer]::__end__: " << ctrl_info(mgr) << std::endl;
}

// Timer function that runs asynchronously
void startTimer(int duration_msec, Callback callback, ThrottlingManager &mgr) {
    std::thread([duration_msec, callback, &mgr]() {
        // Wait for the duration
        std::this_thread::sleep_for(std::chrono::milliseconds(duration_msec));
        // Invoke the callback
        callback(mgr);
    }).detach(); // Detach the thread to run independently
}

// callback entring to FULL_PPERFORMANCE state
void do_enterCb_fullPerf(ThrottlingManager &mgr)
{
    ThrottlingStateId prevState = mgr.getPrevStateId();

    std::cout << "  - CB[enter]::_start_: " << ctrl_info(mgr) << std::endl;
    if (prevState == ThrottlingStateId::UNINIT) {
        denoise_ctr = true;
    } else {
        if (denoise_ctr == false && !enteringFullPerfState_timer_started) {
            uint64_t enter_ts = mgr.getStateEnterTimestamp(ThrottlingStateId::FULL_PERFORMANCE);
            uint64_t curr_ts = get_monotonic_time_msec();
            uint64_t diff_ts = curr_ts - enter_ts;
            if (diff_ts >= max_cooling_duration_msec) {
                denoise_ctr = true;
            } else {
                uint64_t remain_ts = max_cooling_duration_msec - diff_ts;
                enteringFullPerfState_timer_started = true;
                std::cout << "  - CB[enter]: - timestamp msec:" << std::endl;
                std::cout << "  - CB[enter]:   - enter " << enter_ts << " msec" << std::endl;
                std::cout << "  - CB[enter]:   - curr  " << curr_ts  << " msec" << std::endl;
                std::cout << "  - CB[enter]:   - diff  " << diff_ts  << " msec" << std::endl;
                std::cout << "  - CB[enter]: - Enabling denoise in " << remain_ts << " msec" << std::endl;
                startTimer(remain_ts, onTimerExpire, mgr);
            }   
        }
    }

    std::cout << "  - CB[enter]::_end__: " << ctrl_info(mgr) << std::endl;
}

// callback entring to S0 state
void do_enterCb_s0(ThrottlingManager &mgr)
{
    std::cout << "  - CB[enter]::_start_: " << ctrl_info(mgr) << std::endl;
    if (mgr.getPrevStateId() == ThrottlingStateId::FULL_PERFORMANCE) {
        denoise_ctr = false;
    }
    std::cout << "  - CB[enter]::_end_: " << ctrl_info(mgr) << std::endl;
}

// callback entring to S1 state
void do_enterCb_s1(ThrottlingManager &mgr)
{
    std::cout << "  - CB[enter]::_start_: " << ctrl_info(mgr) << std::endl;
    std::cout << "  - CB[enter]::_end__: " << ctrl_info(mgr) << std::endl;
}

// callback entring to S2 state
void do_enterCb_s2(ThrottlingManager &mgr)
{
    std::cout << "  - CB[enter]::_start_: " << ctrl_info(mgr) << std::endl;
    std::cout << "  - CB[enter]::_end__: " << ctrl_info(mgr) << std::endl;
}

// callback entring to S3 state
void do_enterCb_s3(ThrottlingManager &mgr)
{
    std::cout << "  - CB[enter]::_start_: " << ctrl_info(mgr) << std::endl;
    if (mgr.getPrevStateId() == ThrottlingStateId::S4) {
        stream_ctr = true;
    }
    std::cout << "  - CB[enter]::_end__: " << ctrl_info(mgr) << std::endl;
}

// callback entring to S4 state
void do_enterCb_s4(ThrottlingManager &mgr)
{
    std::cout << "  - CB[enter]::_start_: " << ctrl_info(mgr) << std::endl;
    stream_ctr = false;
    std::cout << "  - CB[enter]::_end__: " << ctrl_info(mgr) << std::endl;
}

// Global variable to hold the throttling mode
ThrottlingMode throttling_mode = ThrottlingMode::AUTO;

int parseArgs(int argc, char *argv[])
{
    static struct option long_options[] = {
        {"cooling", required_argument, nullptr, 'c'},
        {"mode", required_argument, nullptr, 'm'},
        {"batch", required_argument, nullptr, 'b'},
        {"help", no_argument, nullptr, 'h'},
        {nullptr, 0, nullptr, 0}
    };

    int opt;
    int option_index = 0;

    while ((opt = getopt_long(argc, argv, "m:c:b:h", long_options, &option_index)) != -1) {
        switch (opt) {
            case 'm': {
                std::string arg(optarg);
                if (arg == "auto") {
                    throttling_mode = ThrottlingMode::AUTO;
                } else if (arg == "manual") {
                    throttling_mode = ThrottlingMode::MANUAL;
                } else {
                    std::cerr << "Invalid mode: " << arg << "\n";
                    return -1;
                }
                break;
            }
            case 'c': {
                size_t pos;
                std::string arg(optarg);
                uint64_t duration = std::stoull(arg, &pos);
                if (pos != arg.size() || duration < 5) {
                    std::cerr << "Invalid cooling duration: " << arg << "\n";
                    return -1;
                }
                max_cooling_duration_msec = duration * 1000ULL;
                break;
            }
            case 'b': {
                size_t pos;
                std::string arg(optarg);
                uint64_t duration = std::stoull(arg, &pos);
                if (pos != arg.size() || duration < 5) {
                    std::cerr << "Invalid batch duration: " << arg << "\n";
                    return -1;
                }
                batch_mode = true;
                batch_duration_sec = duration;
                break;
            }
            case 'h':
                std::cout << argv[0] << " [options]\n"
                          << "Options:\n"
                          << "  -c, --cooling <duration sec> : max cooling duration for allowing denoise.\n"
                          << "                                 duration > 5 sec.\n"
                          << "  -m, --mode <auto|manual>     : Set throttling mode (default: auto).\n"
                          << "  -b, --batch <duration sec>   : non-interactive mode.\n"
                          << "                                 duration > 0 sec.\n"
                          << "  -h, --help                   : Show this help message.\n";
                exit(0); // exit early after help
            default:
                return -1;
        }
    }

    return 0;
}


int main(int argc, char *argv[])
{
    // Parse command line arguments
    if (parseArgs(argc, argv) != 0) {
        printf("Failed to parse options and arguments\n");
        printf("Use --help for usage\n");
        return EXIT_FAILURE;
    }

    printf("Running %s in %s mode\n", argv[0], batch_mode ? "batch" : "interactive");
    printf("- mode: %s\n", throttling_mode == ThrottlingMode::AUTO ? "AUTO" : "MANUAL");
    printf("- cooling: %lu msec\n", max_cooling_duration_msec);
    printf("Press Enter to start ThrottlingManager > ");

    if (!batch_mode) 
        while (getchar() != '\n');

    ThrottlingManager &mgr = ThrottlingManager::getInstance();
    mgr.setThrottlingMode(throttling_mode);

	mgr.register_enterCb(ThrottlingStateId::FULL_PERFORMANCE, do_enterCb_fullPerf);
	mgr.register_enterCb(ThrottlingStateId::S0, do_enterCb_s0);
	mgr.register_enterCb(ThrottlingStateId::S1, do_enterCb_s1);
	mgr.register_enterCb(ThrottlingStateId::S2, do_enterCb_s2);
	mgr.register_enterCb(ThrottlingStateId::S3, do_enterCb_s3);
	mgr.register_enterCb(ThrottlingStateId::S4, do_enterCb_s4);

    printf("\n\nStart heating & cooling your device >\n\n");
    mgr.startWatch();
    if (!batch_mode) { 
        printf("Press Enter to stop ThrottlingManager > ");
        while (getchar() != '\n');
    } else
        sleep(batch_duration_sec);
    mgr.stopWatch();

    return 0;
}
