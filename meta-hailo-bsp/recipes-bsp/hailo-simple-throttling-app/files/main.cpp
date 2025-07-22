#include <chrono>
#include <ctime>
#include <cstdio>
#include <string>
#include "throttling_manager.h"
bool enteringFullPerfState_timer_started = false;
bool denoise_ctr = false;
bool stream_ctr = true;

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
            if (diff_ts >= 5000ULL) {
                denoise_ctr = true;
            } else {
                uint64_t remain_ts = 5000ULL - diff_ts;
                enteringFullPerfState_timer_started = true;
                std::cout << "  - CB[enter]: enter_ts " << enter_ts << " msec" << std::endl;
                std::cout << "  - CB[enter]: curr_ts " << curr_ts << " msec" << std::endl;
                std::cout << "  - CB[enter]: diff_ts " << diff_ts << " msec" << std::endl;
                std::cout << "  - CB[enter]: enabling denoise in " << remain_ts << " msec" << std::endl;
                startTimer(remain_ts, onTimerExpire, mgr);
            }   
        }
    }

    std::cout << "  - CB[enter]::_end_: " << ctrl_info(mgr) << std::endl;
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
    std::cout << "  - CB[enter]::_end_: " << ctrl_info(mgr) << std::endl;
}

// callback entring to S2 state
void do_enterCb_s2(ThrottlingManager &mgr)
{
    std::cout << "  - CB[enter]::_start_: " << ctrl_info(mgr) << std::endl;
    std::cout << "  - CB[enter]::_end_: " << ctrl_info(mgr) << std::endl;
}

// callback entring to S3 state
void do_enterCb_s3(ThrottlingManager &mgr)
{
    std::cout << "  - CB[enter]::_start_: " << ctrl_info(mgr) << std::endl;
    if (mgr.getPrevStateId() == ThrottlingStateId::S4) {
        stream_ctr = true;
    }
    std::cout << "  - CB[enter]::_end_: " << ctrl_info(mgr) << std::endl;
}

// callback entring to S4 state
void do_enterCb_s4(ThrottlingManager &mgr)
{
    std::cout << "  - CB[enter]::_start_: " << ctrl_info(mgr) << std::endl;
    stream_ctr = false;
    std::cout << "  - CB[enter]::_end_: " << ctrl_info(mgr) << std::endl;
}

int main()
{
    ThrottlingManager &mgr = ThrottlingManager::getInstance();

	mgr.register_enterCb(ThrottlingStateId::FULL_PERFORMANCE, do_enterCb_fullPerf);
	mgr.register_enterCb(ThrottlingStateId::S0, do_enterCb_s0);
	mgr.register_enterCb(ThrottlingStateId::S1, do_enterCb_s1);
	mgr.register_enterCb(ThrottlingStateId::S2, do_enterCb_s2);
	mgr.register_enterCb(ThrottlingStateId::S3, do_enterCb_s3);
	mgr.register_enterCb(ThrottlingStateId::S4, do_enterCb_s4);

    printf("Press Enter to start ThrottlingManager...: ");
    while (getchar() != '\n');
    mgr.startWatch();

    printf("\n\nStart heating & colling your device...\n\n");

    printf("Press Enter to stop ThrottlingManager...: ");
    while (getchar() != '\n');
    mgr.stopWatch();

    return 0;
}
