#ifndef THROTTLING_STATE_H
#define THROTTLING_STATE_H

#include <vector>
#include <functional>
#include <stdint.h>
#include "throttling_manager_types.h"
#include "throttling_event.h"

struct ThrottlingManager;
enum class ThrottlingEventId;

//
// Throttling State Base Class
//
class ThrottlingState
{
    friend class ThrottlingManager;
private:
    ThrottlingStateId stateId;
    uint64_t enter_ts;
    uint64_t exit_ts;

    /*!
     * @brief Enter/Exit Callbacks vector
     * 
     * @note Callbacks are user-defined functions that will be executed
     *       upon entering/exiting this state.
     */
    std::vector<Callback> enterCallbacks;
    std::vector<Callback> exitCallbacks;

    /*!
     * @brief Execute Enter/Exit Callbacks
     *
     * @param ctx: Throttling context
     * @return void
     */
    void onEnterCallbacks(ThrottlingManager &ctx);
    void onExitCallbacks(ThrottlingManager &ctx);

    /*!
     * @brief Handle event on this Throttling state
     *
     * @param context: Throttling context
     * @param event: Thermal event id
     * @param ts: Timestamp of the event occurrence
     * @return void
     */
    virtual void handle(ThrottlingManager &context, ThrottlingEventId &event, uint64_t ts) = 0;

    /*!
     * @brief Set Enter/Exit Timestamp
     *
     * @param ts: Timestamp
     * @return void
     */
    void setEnterTimestamp(uint64_t ts);
    void setExitTimestamp(uint64_t ts);

public:
    ThrottlingState(ThrottlingStateId state);
    virtual ~ThrottlingState();

    /*!
     * @brief Get Throttling State Id
     *
     * @return ThrottlingStateId
     */
    ThrottlingStateId getStateId(void) const;

    /*!
     * @brief Get Enter/Exit Timestamp
     *
     * @return uint64_t
     */
    uint64_t getEnterTimestamp(void) const;
    uint64_t getExitTimestamp(void) const;

    /*!
     * @brief Register user callback upon Enter/Exit this state
     *
     * @param cb: Callback function
     * @return void
     */
    void register_enterCb(const Callback &cb);
    void register_exitCb(const Callback &cb);

    /*!
     * @brief Unregister user callback upon Enter/Exit this state
     *
     * @param cb: Callback function
     * @return void
     */
    void unregister_enterCb(const Callback &cb);
    void unregister_exitCb(const Callback &cb);

    /*!
     * @brief Overload the !=, == operators
     *
     * @param other: Throttling state
     * @return bool
     */
    bool operator!=(const ThrottlingState& other) const;
    bool operator==(const ThrottlingState& other) const;

    /*!
     * @brief Convert Throttling state to string
     */
    std::string toString() const;

    /*!
     * @brief Overload the << operator
     *
     * @param os: Output stream
     * @param state: Throttling state
     */
    friend std::ostream& operator<<(std::ostream& os, const ThrottlingState& state);
};



//
// Throttling State Derived Classes
//

/*!
 * @brief Throttling State: uninit (defined as Singleton)
 * 
 * @note This state is the default state where the system is running at full performance.
 */
class ThrottlingStateUninit : public ThrottlingState, public Singleton<ThrottlingStateUninit> {
    friend class Singleton<ThrottlingStateUninit>;
private:
ThrottlingStateUninit() : ThrottlingState(ThrottlingStateId::UNINIT) {}
    void handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts) {};
};

/*!
 * @brief Throttling State: Full Performance (defined as Singleton)
 * 
 * @note This state is the default state where the system is running at full performance.
 */
class ThrottlingStateFullPerformance : public ThrottlingState, public Singleton<ThrottlingStateFullPerformance> {
    friend class Singleton<ThrottlingStateFullPerformance>;
private:
    ThrottlingStateFullPerformance() : ThrottlingState(ThrottlingStateId::FULL_PERFORMANCE) {}
    void handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts) override;
};

/*!
 * @brief Throttling State: S0 (defined as Singleton)
 */
class ThrottlingState0 : public ThrottlingState, public Singleton<ThrottlingState0> {
    friend class Singleton<ThrottlingState0>;
private:
    ThrottlingState0() : ThrottlingState(ThrottlingStateId::S0) {}
    void handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts) override;
};

/*!
 * @brief Throttling State: S1 (defined as Singleton)
 */
class ThrottlingState1 : public ThrottlingState, public Singleton<ThrottlingState1> {
    friend class Singleton<ThrottlingState1>;
private:
    ThrottlingState1() : ThrottlingState(ThrottlingStateId::S1) {}
    void handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts) override;
};

/*!
 * @brief Throttling State: S2 (defined as Singleton)
 */
class ThrottlingState2 : public ThrottlingState, public Singleton<ThrottlingState2> {
    friend class Singleton<ThrottlingState2>;
private:
    ThrottlingState2() : ThrottlingState(ThrottlingStateId::S2) {}
    void handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts) override;
};

/*!
 * @brief Throttling State: S3 (defined as Singleton)
 */
class ThrottlingState3 : public ThrottlingState, public Singleton<ThrottlingState3> {
    friend class Singleton<ThrottlingState3>;
private:
    ThrottlingState3() : ThrottlingState(ThrottlingStateId::S3) {}
    void handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts) override;
};

/*!
 * @brief Throttling State: S4 (defined as Singleton)
 */
class ThrottlingState4 : public ThrottlingState, public Singleton<ThrottlingState4> {
    friend class Singleton<ThrottlingState4>;
private:
    ThrottlingState4() : ThrottlingState(ThrottlingStateId::S4) {}
    void handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts) override;
};

#endif // THROTTLING_STATE_H