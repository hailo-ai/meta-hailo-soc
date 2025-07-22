#include <iostream>
#include "throttling_event_queue.h"
#include "throttling_log.h"

void ThermalEventQueue::push(ThrottlingEventId id, uint64_t ts) {
    std::pair<ThrottlingEventId, uint64_t> event = {id, ts};
    std::lock_guard<std::mutex> lock(mtx);
    queue.push(event);
    logger_default << "EV: pushed " << ThrottlingEventToString(id) << " @ " << ts << " msec" << std::endl;
    cv.notify_one();
}

std::pair<ThrottlingEventId, uint64_t> ThermalEventQueue::pop() {
    std::unique_lock<std::mutex> lock(mtx);
    cv.wait(lock, [this] { return !queue.empty(); }); // Wait for an event
    std::pair<ThrottlingEventId, uint64_t> event = queue.front();
    logger_default << "EV: poped " << ThrottlingEventToString(event.first) << " @ " << event.second << " msec" << std::endl;
    queue.pop();
    return event;
}
