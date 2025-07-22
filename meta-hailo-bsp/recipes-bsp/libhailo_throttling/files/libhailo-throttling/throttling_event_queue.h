#ifndef THROTTLING_EVENT_QUEUE_H
#define THROTTLING_EVENT_QUEUE_H

#include <queue>
#include <mutex>
#include <condition_variable>
#include <utility>
#include <stdint.h>

#include "throttling_event.h"

// Event Queue with thread safety
class ThermalEventQueue {
private:
    std::queue<std::pair<ThrottlingEventId, uint64_t>> queue;
    std::mutex mtx;
    std::condition_variable cv;

public:
    void push(ThrottlingEventId id, uint64_t ts);
    std::pair<ThrottlingEventId, uint64_t> pop();
};

#endif // THROTTLING_EVENT_QUEUE_H