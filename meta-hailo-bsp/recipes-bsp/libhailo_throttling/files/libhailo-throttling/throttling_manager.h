#ifndef THROTTLING_MANAGER_H
#define THROTTLING_MANAGER_H

#include <iostream>
#include <string>
#include <cstddef>
#include <thread>
#include <atomic>
#include <map>
#include <semaphore.h>
#include <mutex>
#include "throttling_manager_types.h"
#include "throttling_event.h"
#include "throttling_state.h"

class ThermalEventQueue;
class ThrottlingManager;

class ThrottlingManager : public Singleton<ThrottlingManager> {
    friend class Singleton<ThrottlingManager>;
    friend class ThrottlingStateFullPerformance;
    friend class ThrottlingState0;
    friend class ThrottlingState1;
    friend class ThrottlingState2;
    friend class ThrottlingState3;
    friend class ThrottlingState4;
private:
    struct hailo_thermal_data *thermalData;
    size_t thermalDataSize;
    ThrottlingState *currState;
    ThrottlingState *prevState;
	ThermalEventQueue *eventQueue;
	std::atomic<bool>  running;
	std::thread thermalEventDispatcherThread;
	std::thread thermalEventQueuingThread;
	std::mutex watchCtrlMutex;
	std::map<ThrottlingStateId, ThrottlingState*> throttlingStateObjMap;
    sem_t *sem;

    ThrottlingManager();

	/*!
	 * @brief Thermal Event dispatching includes:
	 *        1. thermalEventDispatcherThreadFn: pop event from eventQueue
	 *        2. thermalEventDispatcher: dispatch event to current throttling state.
	 */
    void thermalEventDispatcherThreadFn();
    void thermalEventDispatcher(ThrottlingEventId event, uint64_t ts);

	/*!
	 * @brief Thermal Event Listener from thermal-event service includes:
	 *        1. thermalEventQueuingThreadFn: Waits for new thermal event occurence.
	 *        2. thermalEventQueuingHandler: Eenqueue new thermal event into internal jitter buffer.
	 */
    void thermalEventQueuingThreadFn();
    void thermalEventQueuingHandler(bool firstUpdate = true);

	/*!
	 * @brief Dump Thermal Data
	 */
    void thermalDataDump(void);

	/*!
	 * @brief Set throttling state
	 * 
	 * @param state: Throttling state to set
	 * @param ts: Timestamp when the state is set
	 */
	void setState(ThrottlingState *state, uint64_t ts);

	/*!
	 * @brief push <event, timestamp> into internal queue.
	 * 
	 * @param event: Thermal event id
	 * @param ts: Timestamp when the thermal event happend
	 */
	void pushEvent(ThrottlingEventId event, uint64_t ts);

public:
    ~ThrottlingManager();
	static constexpr const char* SYSFS_THROTTLING_MODE_PATH = "/sys/devices/soc0/throttling_mode/";

	bool isRunning();
	/*!
	 * @brief StartStop managing throttling state.
	 * 
	 * @return 0 on success, otherwise errno
	 */
    int startWatch();
    void stopWatch();

	/*!
	 * @brief Get current/previous state id
	 * 
	 * @return ThrottlingStateId
	 */
    ThrottlingStateId getCurrStateId();
    ThrottlingStateId getPrevStateId();

	/*!
	 * @brief Get current/previous state as string
	 * 
	 * @return ThrottlingStateId
	 */
	std::string currStateToString();
    std::string prevStateToString();

	/*!
	 * @brief Get current/previous state enter/exit CLOCK_MONOTONIC_RAW timestamp in msec
	 * 
	 * @return	uint64_t
	 */
	uint64_t getStateEnterTimestamp(ThrottlingStateId state);
    uint64_t getStateExitTimestamp(ThrottlingStateId state);

    /*!
	 * @brief Register user callback upon entering/exiting the specify state
     *
     * @param id: Throttling State id.
     * @param cb: Callback function.
     * @return 0 on success, otherwise errno
     */
    int register_enterCb(ThrottlingStateId id, const Callback &cb);
    int register_exitCb(ThrottlingStateId id, const Callback &cb);

    /*!
	 * @brief Unegister user callback upon entering/exiting the specify state
     *
     * @param id: Throttling State id.
     * @param cb: Callback function.
     * @return 0 on success, otherwise errno
     */
    int unregister_enterCb(ThrottlingStateId id, const Callback &cb);
    int unregister_exitCb(ThrottlingStateId id, const Callback &cb);

    /*!
	 * @brief Set/Get throttling mode
     *
     * @param mode: ThrottlingMode::AUTO/MANUAL.
     * @return 0 on success, otherwise errno
     */
    int setThrottlingMode(ThrottlingMode mode);
    int getThrottlingMode(ThrottlingMode &mode);
};

#endif // THROTTLING_MANAGER_H