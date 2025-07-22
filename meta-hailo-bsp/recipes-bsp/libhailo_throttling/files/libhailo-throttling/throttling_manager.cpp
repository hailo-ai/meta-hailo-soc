#include <cstdlib>
#include <unistd.h>
#include <memory>
#include <functional>
#include <mutex>
#include <condition_variable>
#include <chrono>
#include <cstdio>
#include <map>
#include <errno.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdint.h>
#include <string.h>
#include <time.h>
#include <linux/thermal.h>

#include "throttling_event_queue.h"
#include "throttling_manager.h"
#include "throttling_log.h"

std::string thermalTripTypeToString(enum thermal_trip_type type) {
    std::map<enum thermal_trip_type, std::string> tripTypeMap = {
        {THERMAL_TRIP_ACTIVE, "ACTIVE"},
        {THERMAL_TRIP_PASSIVE, "PASSIVE"},
        {THERMAL_TRIP_HOT, "HOT"},
        {THERMAL_TRIP_CRITICAL, "CRITICAL"}
    };
    auto it = tripTypeMap.find(type);
    return (it != tripTypeMap.end()) ? it->second : "???";
}

ThrottlingManager::ThrottlingManager() : eventQueue(NULL), running(false) {
    sem = sem_open(HAILO_THERMAL_ENGINE_SEM_PATH, 0);
    if (sem == SEM_FAILED) {
        perror("Failed to open semaphore");
        return;
    }
    throttlingStateObjMap = {
        {ThrottlingStateId::UNINIT, &ThrottlingStateUninit::getInstance()},
        {ThrottlingStateId::FULL_PERFORMANCE, &ThrottlingStateFullPerformance::getInstance()},
        {ThrottlingStateId::S0, &ThrottlingState0::getInstance()},
        {ThrottlingStateId::S1, &ThrottlingState1::getInstance()},
        {ThrottlingStateId::S2, &ThrottlingState2::getInstance()},
        {ThrottlingStateId::S3, &ThrottlingState3::getInstance()},
        {ThrottlingStateId::S4, &ThrottlingState4::getInstance()}
    };

    prevState = throttlingStateObjMap[ThrottlingStateId::UNINIT];
    currState = throttlingStateObjMap[ThrottlingStateId::UNINIT];
}

void ThrottlingManager::thermalEventDispatcherThreadFn()
{
    while (running) {
        std::pair<ThrottlingEventId, uint64_t> event = eventQueue->pop();
        thermalEventDispatcher(event.first, event.second);
    }
}

void ThrottlingManager::thermalEventQueuingHandler(bool firstUpdate)
{
    struct hailo_tz *severed_tz = NULL;
    ThrottlingEventId ev_heating, ev_cooling;
    int i, severed_tz_id = 0;
    int trip_id = -1;
    int trip_dir;
    uint64_t trip_ts = -1;

    if (firstUpdate) {
        logger_default << "thermalEventQueuingHandler, firstUpdate = " << firstUpdate << std::endl;
    }

    if (thermalData->num_zones == 0) {
        logger_default << "no thermal zones exist" << std::endl;
        return;
    }

    if (firstUpdate) {
        setState(&ThrottlingStateFullPerformance::getInstance(), 0);
    }

    //
    // Find out which heating/cooling trip ID has been crossed.
    // - strategy:
    //   1) Find highest reported thermal zone temperature value.
    //   2) Find Min heating & Max Cooling trips __temperature__ values.
    //   3) Find highest possible crossed heating trip ID.
    //   4) Find lowest possible crossed cooling trip ID.
    //   5) Find which crossed trip happened or none happened.
    //   6) In case device just booted with temperatue < Minimal Heating trip value, return ...
    //   7) Queue either cooling or heating or both events.
    //

    // 1) Find highest reported thermal zone temperature value.
    severed_tz = &thermalData->tz[0];
    for (i = 1; i < thermalData->num_zones; i++) {
        struct hailo_tz *zone = &thermalData->tz[i];
        if (zone->temp > severed_tz->temp) {
            severed_tz = zone;
            severed_tz_id = i;
        }
    }

    // 2) Find Min heating & Max Cooling trips __temperature__ values.
    struct hailo_tz_trip *tt = &severed_tz->trip[0];
    // Set to INT32_MAX to indicate no heating trip has been crossed.
    int heatingTripMinVal = INT32_MAX;
    // Set to INT32_MIN to indicate no cooling trip has been crossed.
    int coolingTripMaxVal = INT32_MIN;
    for (i = 0; i < severed_tz->num_trips; i++) {
        int heatingTripVal = tt[i].temp;
        int coolingTripVal = tt[i].temp - tt[i].hyst;
        if (heatingTripMinVal > heatingTripVal) {
            heatingTripMinVal = heatingTripVal;
        }
        if (coolingTripMaxVal < coolingTripVal) {
            coolingTripMaxVal = coolingTripVal;
        }
    }

    //
    // 3) Find highest possible crossed heating trip ID.
    //
    // Set to INT32_MIN to indicate no heating trip has been crossed.
    int32_t maxCrossedHeatingTrip_val = INT32_MIN;
    // Set to -1 to indicate no heating trip has been crossed.
    int maxCrossedHeatingTrip_id = -1;
    // Set to 0 to indicate no heating trip has been crossed.
    uint64_t maxCrossedHeatingTrip_ts = 0;
    for (i = 0; i < severed_tz->num_trips; i++) {
        int heatingTripVal = tt[i].temp;
        if (severed_tz->temp > heatingTripVal && maxCrossedHeatingTrip_val < heatingTripVal) {
            maxCrossedHeatingTrip_val = heatingTripVal;
            maxCrossedHeatingTrip_id = tt[i].id;
            maxCrossedHeatingTrip_ts = tt[i].last_heating_ts;
        }
    }

    //
    // 4) Find lowest possible crossed cooling trip ID.
    //
    // Set to INT32_MAX to indicate no cooling trip has been crossed.
    int32_t minCrossedCoolingTrip_val = INT32_MAX;
    // Set to -1 to indicate no cooling trip has been crossed.
    int minCrossedCoolingTrip_id = -1;
    // Set to 0 to indicate no cooling trip has been crossed.
    uint64_t minCrossedCoolingTrip_ts = 0;
    for (i = 0; i < severed_tz->num_trips; i++) {
        int coolingTripVal = tt[i].temp - tt[i].hyst;
        if (severed_tz->temp <= coolingTripVal && minCrossedCoolingTrip_val >= coolingTripVal) {
            minCrossedCoolingTrip_val = coolingTripVal;
            minCrossedCoolingTrip_id = tt[i].id;
            minCrossedCoolingTrip_ts = tt[i].last_cooling_ts;
        }
    }

    // 5) Find which crossed trip happened or none happened.
    if (minCrossedCoolingTrip_id != -1 && maxCrossedHeatingTrip_id != -1) {
        // Either heating or cooling trip have been crossed.
        if (minCrossedCoolingTrip_ts > maxCrossedHeatingTrip_ts) {
            // Last crossed trip -> cooling.
            trip_id = minCrossedCoolingTrip_id;
            trip_dir = TRIP_DIR_DOWN;
            trip_ts = minCrossedCoolingTrip_ts;
        } else {
            // Last crossed trip -> heating.
            trip_id = maxCrossedHeatingTrip_id;
            trip_dir = TRIP_DIR_UP;
            trip_ts = maxCrossedHeatingTrip_ts;
        }
    } else if (minCrossedCoolingTrip_id != -1) {
        // Only Cooling trip has been crossed.
        trip_id = minCrossedCoolingTrip_id;
        trip_dir = TRIP_DIR_DOWN;
        trip_ts = minCrossedCoolingTrip_ts;
    } else if (maxCrossedHeatingTrip_id != -1) {
        // Heating trip that has been crossed last.
        trip_id = maxCrossedHeatingTrip_id;
        trip_dir = TRIP_DIR_UP;
        trip_ts = maxCrossedHeatingTrip_ts;
    } else {
        // No trip has been crossed.
        return;

    }

    // 6) In case device just booted with temperatue < Minimal Heating trip value, return ...
    if (trip_ts == 0 && severed_tz->temp < heatingTripMinVal) {
        logger_default << "Device booted with temperature " << severed_tz->temp << ", which is < MinHeatingTrip" << std::endl;
        return;
    }

    // 7) Queue either cooling or heating or both events.
    switch (trip_dir) {
        case TRIP_DIR_UP:
            ev_heating = static_cast<ThrottlingEventId>(static_cast<int>(ThrottlingEventId::HEATING_TH0) + trip_id);
            pushEvent(ev_heating, trip_ts);
            break;
        case TRIP_DIR_DOWN:
            if (firstUpdate) {
                // If the first update __and__ last occured trip is COOLING, then we push heating event of that trip as well.
                ev_heating = static_cast<ThrottlingEventId>(static_cast<int>(ThrottlingEventId::HEATING_TH0) + trip_id);
                pushEvent(ev_heating, trip_ts);
            }
            ev_cooling = static_cast<ThrottlingEventId>(static_cast<int>(ThrottlingEventId::COOLING_TH0) + trip_id);
            pushEvent(ev_cooling, trip_ts);
            break;
        case TRIP_DIR_INVALID:
        default:
            for (i = 0; i < thermalData->tz[severed_tz_id].num_trips; i++) {
                struct hailo_tz_trip *tt = &thermalData->tz[severed_tz_id].trip[i];
                if (severed_tz->temp > tt->temp) {
                    ev_heating = static_cast<ThrottlingEventId>(static_cast<int>(ThrottlingEventId::HEATING_TH0) + trip_id);
                    pushEvent(ev_heating, 0);
                }
            }
            break;
    }
}


void ThrottlingManager::thermalEventQueuingThreadFn()
{
    static bool firstUpdate = true;
    logger_default << "Thermal event queuing started" << std::endl;
    while (running) {
        if (!firstUpdate) {
            sem_wait(sem);
        }
        thermalEventQueuingHandler(firstUpdate);
        firstUpdate = false;
    }
    firstUpdate = true;
    logger_default << "Thermal event queuing stopped" << std::endl;
}

void ThrottlingManager::thermalEventDispatcher(ThrottlingEventId event, uint64_t ts)
{
    if (currState) {
        logger_default << "EV: handle event (" << ThrottlingEventToString(event) << "), current state(" << *currState << ")" << std::endl;
        if (event == ThrottlingEventId::STOP_WATCH) {
            return;
        }
        currState->handle(*this, event, ts);
    }
}

void ThrottlingManager::thermalDataDump(void)
{
    int i, z;

    for (z = 0; z < thermalData->num_zones; z++) {
        struct hailo_tz *tz = &thermalData->tz[z];
        if (tz->id == -1) {
            continue;
        }
        
        logger_default << "tz[" << tz->id << "]: " << tz->name << ", temperature: " << tz->temp << " °C" << std::endl;
        logger_default << "\t- governer " << tz->governor << std::endl;
        logger_default << "\t- trips: " << tz->num_trips << std::endl;
        for (i = 0; i < tz->num_trips; i++) {
            struct hailo_tz_trip *tt = &tz->trip[i];
            if (tt->id == -1) {
                continue;
            }
            logger_default << "\t\t- trip[" << tt->id << "]: type " 
                           << thermalTripTypeToString(static_cast<enum thermal_trip_type>(tt->type)) 
                           << ", temp " << tt->temp 
                           << ", hyst " << tt->hyst 
                           << ": last_cooling_ts[" << tt->last_cooling_ts << "] msec, last_heating_ts[" << tt->last_heating_ts << "] msec" << std::endl;
        }
    }
}

void ThrottlingManager::setState(ThrottlingState *nextState, uint64_t ts)
{
    std::string nextStateStr = (!nextState) ? "uninit" : nextState->toString();
    std::string currStateStr = (!currState) ? "uninit" : currState->toString();
    ThrottlingStateId currStateId = getCurrStateId();

    logger_default << "Set state: [" << currStateStr << " => " << nextStateStr << "]" << std::endl;

    if (!nextState)
        return;

    if (currStateId == ThrottlingStateId::UNINIT || currStateId != nextState->getStateId()) {
        if (currStateId != ThrottlingStateId::UNINIT) {
            currState->onExitCallbacks(*this);
            currState->setExitTimestamp(ts);
        }
        // Set new state as current state.
        prevState = currState;
        currState = nextState;

        currState->setEnterTimestamp(ts);
        currState->onEnterCallbacks(*this);
    }
}

void ThrottlingManager::pushEvent(ThrottlingEventId event, uint64_t ts)
{
    eventQueue->push(event, ts);
}


ThrottlingManager::~ThrottlingManager()
{
    stopWatch();
    sem_close(sem);
}

bool ThrottlingManager::isRunning()
{
    return running;
}

int ThrottlingManager::startWatch()
{
    logger_default << "Start watching ..." << std::endl;

    eventQueue = new ThermalEventQueue();
    if (!eventQueue) {
        perror("Failed to create event queue");
        return -ENOMEM;
    }

    int fd = open(HAILO_THERMAL_ENGINE_DATA_PATH, O_RDONLY, 0444);
    if (fd < 0) {
        perror("Failed to open file");
        return fd;
    }

    struct stat sb;
    if (fstat(fd, &sb) == -1) {
        perror("fstat");
        close(fd);
        return errno;
    }

    thermalDataSize = sb.st_size;
    if (thermalDataSize == 0) {
        fprintf(stderr, "File is empty.\n");
        close(fd);
        return -EIO;
    }        
    
    thermalData = (struct hailo_thermal_data *)mmap(NULL, thermalDataSize, PROT_READ, MAP_SHARED, fd, 0);
    if (thermalData == MAP_FAILED) {
        perror("Failed to mmap");
        return errno;
    }
    close(fd);

    thermalDataDump();

    running = true;
    thermalEventDispatcherThread = std::thread(&ThrottlingManager::thermalEventDispatcherThreadFn, this);
    thermalEventQueuingThread = std::thread(&ThrottlingManager::thermalEventQueuingThreadFn, this);

    return 0;
}


void ThrottlingManager::stopWatch()
{
    std::lock_guard<std::mutex> lock(watchCtrlMutex); 
    if (!running) {
        return;
    }

    logger_default << "Stop watching ..." << std::endl;
    // Mark Listener & Dispatcher to stop.
    running = false;
    // release the semaphore to unblock the listener.
    sem_post(sem);
    // Push STOP_WATCH event to unblock the dispatcher.
    pushEvent(ThrottlingEventId::STOP_WATCH, 0);

    if (thermalEventDispatcherThread.joinable()) {
        thermalEventDispatcherThread.join();
    }

    if (thermalEventQueuingThread.joinable()) {
        thermalEventQueuingThread.join();
    }

    delete eventQueue;

    munmap(thermalData, thermalDataSize);
    thermalData = nullptr;
}


ThrottlingStateId ThrottlingManager::getCurrStateId()
{ 
    return currState->getStateId();
}

ThrottlingStateId ThrottlingManager::getPrevStateId()
{
    return prevState->getStateId();
}

std::string ThrottlingManager::currStateToString()
{ 
    return currState->toString();
}

std::string ThrottlingManager::prevStateToString()
{
    return prevState->toString();
}

uint64_t ThrottlingManager::getStateEnterTimestamp(ThrottlingStateId state)
{
    switch (state) {
        case ThrottlingStateId::FULL_PERFORMANCE: return ThrottlingStateFullPerformance::getInstance().getEnterTimestamp();
        case ThrottlingStateId::S0: return ThrottlingState0::getInstance().getEnterTimestamp();
        case ThrottlingStateId::S1: return ThrottlingState1::getInstance().getEnterTimestamp();
        case ThrottlingStateId::S2: return ThrottlingState2::getInstance().getEnterTimestamp();
        case ThrottlingStateId::S3: return ThrottlingState3::getInstance().getEnterTimestamp();
        case ThrottlingStateId::S4: return ThrottlingState4::getInstance().getEnterTimestamp();
        default: return 0;
    }
}

uint64_t ThrottlingManager::getStateExitTimestamp(ThrottlingStateId state)
{
    switch (state) {
        case ThrottlingStateId::FULL_PERFORMANCE: return ThrottlingStateFullPerformance::getInstance().getExitTimestamp();
        case ThrottlingStateId::S0: return ThrottlingState0::getInstance().getExitTimestamp();
        case ThrottlingStateId::S1: return ThrottlingState1::getInstance().getExitTimestamp();
        case ThrottlingStateId::S2: return ThrottlingState2::getInstance().getExitTimestamp();
        case ThrottlingStateId::S3: return ThrottlingState3::getInstance().getExitTimestamp();
        case ThrottlingStateId::S4: return ThrottlingState4::getInstance().getExitTimestamp();
        default: return 0;
    }
}

/*!
 * @brief Register user callback upon entering the specify state
 */
int ThrottlingManager::register_enterCb(ThrottlingStateId id, const Callback &cb)
{
    ThrottlingState *state = throttlingStateObjMap[id];
    if (state) {
        state->register_enterCb(cb);
    } else {
        return -EINVAL;
    }
    return 0;
}

/*!
 * @brief Register user callback upon exiting the specify state
 */
int ThrottlingManager::register_exitCb(ThrottlingStateId id, const Callback &cb)
{
    ThrottlingState *state = throttlingStateObjMap[id];
    if (state) {
        state->register_exitCb(cb);
    } else {
        return -EINVAL;
    }
    return 0;
}

/*!
 * @brief Unregister user callback upon entering the specify state
 */
int ThrottlingManager::unregister_enterCb(ThrottlingStateId id, const Callback &cb)
{
    ThrottlingState *state = throttlingStateObjMap[id];
    if (state) {
        state->unregister_enterCb(cb);
    } else {
        return -EINVAL;
    }
    return 0;
}

/*!
 * @brief Unregister user callback upon exiting the specify state
 */
int ThrottlingManager::unregister_exitCb(ThrottlingStateId id, const Callback &cb)
{
    ThrottlingState *state = throttlingStateObjMap[id];
    if (state) {
        state->unregister_exitCb(cb);
    } else {
        return -EINVAL;
    }
    return 0;
}
