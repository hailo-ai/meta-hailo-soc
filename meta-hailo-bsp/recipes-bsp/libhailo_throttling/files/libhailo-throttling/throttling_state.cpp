#include <map>
#include "throttling_manager.h"
#include "throttling_state.h"
#include "throttling_log.h"


// Function to convert enum class ThrottlingStateId to string
std::string ThrottlingStateToString(ThrottlingStateId id) {
    std::map<ThrottlingStateId, std::string> stateMap = {
        {ThrottlingStateId::UNINIT, "UNINIT"},
        {ThrottlingStateId::FULL_PERFORMANCE, "FULL_PERFORMANCE"},
        {ThrottlingStateId::S0, "S0"},
        {ThrottlingStateId::S1, "S1"},
        {ThrottlingStateId::S2, "S2"},
        {ThrottlingStateId::S3, "S3"},
        {ThrottlingStateId::S4, "S4"}
    };

    auto it = stateMap.find(id);
    return (it != stateMap.end()) ? it->second : "UNKNOWN";
}

/*!
 * @brief Overload the << operator
 */
std::ostream& operator<<(std::ostream& os, const ThrottlingState& state)
{
    return os << ThrottlingStateToString(state.getStateId());
}


//
// Throttling State base class implementations
//

/*!
 * @brief Throttling State Constructor
 */
ThrottlingState::ThrottlingState(ThrottlingStateId state) : stateId(state) {}


/*!
 * @brief Throttling State Destructor
 */
ThrottlingState::~ThrottlingState() {}


/*!
 * @brief Convert Throttling state to string
 */
std::string ThrottlingState::toString() const
{
    return ThrottlingStateToString(stateId);
}


/*!
 * @brief Overload the != operator
 */
bool ThrottlingState::operator!=(const ThrottlingState& other) const {
    return (stateId != other.stateId);
}


/*!
 * @brief Overload the == operator
 */
bool ThrottlingState::operator==(const ThrottlingState& other) const {
    return (stateId == other.stateId);
}


/*!
 * @brief Execute Enter/Exit Callbacks
 */
void ThrottlingState::onEnterCallbacks(ThrottlingManager& ctx)
{
    for (const auto& cb : enterCallbacks) {
        cb(ctx);
    }
}
void ThrottlingState::onExitCallbacks(ThrottlingManager& ctx)
{
    for (const auto& cb : exitCallbacks) {
        cb(ctx);
    }
}


/*!
 * @brief Get Throttling State Id
 */
ThrottlingStateId ThrottlingState::getStateId() const
{ 
    return stateId;
}


/*!
 * @brief Get Enter/Exit Timestamp
 */
uint64_t ThrottlingState::getEnterTimestamp(void) const
{
    return enter_ts;
}
uint64_t ThrottlingState::getExitTimestamp(void) const
{
    return exit_ts;
}


/*!
 * @brief Set Enter/Exit Timestamp
 */
void ThrottlingState::setEnterTimestamp(uint64_t ts)
{
    enter_ts = ts;
}
void ThrottlingState::setExitTimestamp(uint64_t ts)
{
    exit_ts = ts;
}


/*!
 * @brief Register user callback upon Enter/Exit this state
 */
void ThrottlingState::register_enterCb(const Callback &cb)
{
    enterCallbacks.push_back(cb);
    logger_stdout << "Registering enterCB for state[" << toString() << "], registerd CBs: " <<  enterCallbacks.size() << std::endl;
}
void ThrottlingState::register_exitCb(const Callback &cb)
{
    exitCallbacks.push_back(cb);
    logger_stdout << "Registering exitCB for state[" << toString() << "], registerd CBs: " <<  enterCallbacks.size() << std::endl;
}


/*!
 * @brief Unregister user callback upon Enter/Exit this state
 */
void ThrottlingState::unregister_enterCb(const Callback &cb)
{
    enterCallbacks.erase(
        std::remove_if(enterCallbacks.begin(), enterCallbacks.end(),
                        [&](const Callback& storedCb) {
                            return storedCb.target<void(int)>() == cb.target<void(int)>();
                        }),
        enterCallbacks.end());
    logger_stdout << "Unegistering enterCB for state[" << toString() << "], registerd CBs: " <<  enterCallbacks.size() << std::endl;
}
void ThrottlingState::unregister_exitCb(const Callback &cb)
{
    exitCallbacks.erase(
        std::remove_if(exitCallbacks.begin(), exitCallbacks.end(),
                        [&](const Callback& storedCb) {
                            return storedCb.target<void(int)>() == cb.target<void(int)>();
                        }),
        exitCallbacks.end());
    logger_stdout << "Unegistering exitCB for state[" << toString() << "], registerd CBs: " <<  enterCallbacks.size() << std::endl;
}


//
// Throttling State derived classes implementations
//

/*!
 * @brief Full Performance Handle event implementation
 *
 * @param context: Throttling context
 * @param event: Thermal event id
 * @param ts: Timestamp of the event occurrence
 * @return void
 */
void ThrottlingStateFullPerformance::handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts)
{
    static std::vector<std::pair<ThrottlingEventId, ThrottlingState*>> heatingPairs = {
        { ThrottlingEventId::HEATING_TH0, &ThrottlingState0::getInstance() },
        { ThrottlingEventId::HEATING_TH1, &ThrottlingState1::getInstance() },
        { ThrottlingEventId::HEATING_TH2, &ThrottlingState2::getInstance() },
        { ThrottlingEventId::HEATING_TH3, &ThrottlingState3::getInstance() },
        { ThrottlingEventId::HEATING_TH4, &ThrottlingState4::getInstance() }
    };

    if (ThrottlingEventId::HEATING_TH0 <= event && event <= ThrottlingEventId::HEATING_TH4) {
        for (const auto& p : heatingPairs) {
            if (event >= p.first) {
                ctx.setState(p.second, ts);
            }
        }
    }
}


/*!
 * @brief S0 Handle event implementation
 *
 * @param context: Throttling context
 * @param event: Thermal event id
 * @param ts: Timestamp of the event occurrence
 * @return void
 */
void ThrottlingState0::handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts)
{
    static std::vector<std::pair<ThrottlingEventId, ThrottlingState*>> heatingPairs = {
        { ThrottlingEventId::HEATING_TH1, &ThrottlingState1::getInstance() },
        { ThrottlingEventId::HEATING_TH2, &ThrottlingState2::getInstance() },
        { ThrottlingEventId::HEATING_TH3, &ThrottlingState3::getInstance() },
        { ThrottlingEventId::HEATING_TH4, &ThrottlingState4::getInstance() }
    };

    static std::vector<std::pair<ThrottlingEventId, ThrottlingState*>> coolingPairs = {
        { ThrottlingEventId::COOLING_TH0, &ThrottlingStateFullPerformance::getInstance() },
    };

    if (ThrottlingEventId::HEATING_TH0 <= event && event <= ThrottlingEventId::HEATING_TH4) {
        for (const auto& p : heatingPairs) {
            if (event >= p.first) {
                ctx.setState(p.second, ts);
            }
        }
    }

    if (event == ThrottlingEventId::COOLING_TH0) {
        for (const auto& p : coolingPairs) {
            if (event <= p.first) {
                ctx.setState(p.second, ts);
            }
        }
    }
}


/*!
 * @brief S1 Handle event implementation
 *
 * @param context: Throttling context
 * @param event: Thermal event id
 * @param ts: Timestamp of the event occurrence
 * @return void
 */
void ThrottlingState1::handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts)
{
    static std::vector<std::pair<ThrottlingEventId, ThrottlingState*>> heatingPairs = {
        { ThrottlingEventId::HEATING_TH2, &ThrottlingState2::getInstance() },
        { ThrottlingEventId::HEATING_TH3, &ThrottlingState3::getInstance() },
        { ThrottlingEventId::HEATING_TH4, &ThrottlingState4::getInstance() }
    };

    static std::vector<std::pair<ThrottlingEventId, ThrottlingState*>> coolingPairs = {
        { ThrottlingEventId::COOLING_TH1, &ThrottlingState0::getInstance() },
        { ThrottlingEventId::COOLING_TH0, &ThrottlingStateFullPerformance::getInstance() },
    };

    if (ThrottlingEventId::HEATING_TH2 <= event && event <= ThrottlingEventId::HEATING_TH4) {
        for (const auto& p : heatingPairs) {
            if (event >= p.first) {
                ctx.setState(p.second, ts);
            }
        }
    }

    if (ThrottlingEventId::COOLING_TH0 <= event&& event <= ThrottlingEventId::COOLING_TH1) {
        for (const auto& p : coolingPairs) {
            if (event <= p.first) {
                ctx.setState(p.second, ts);
            }
        }
    }
}


/*!
 * @brief S2 Handle event implementation
 *
 * @param context: Throttling context
 * @param event: Thermal event id
 * @param ts: Timestamp of the event occurrence
 * @return void
 */
void ThrottlingState2::handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts)
{
    static std::vector<std::pair<ThrottlingEventId, ThrottlingState*>> heatingPairs = {
        { ThrottlingEventId::HEATING_TH3, &ThrottlingState3::getInstance() },
        { ThrottlingEventId::HEATING_TH4, &ThrottlingState4::getInstance() }
    };

    static std::vector<std::pair<ThrottlingEventId, ThrottlingState*>> coolingPairs = {
        { ThrottlingEventId::COOLING_TH2, &ThrottlingState1::getInstance() },
        { ThrottlingEventId::COOLING_TH1, &ThrottlingState0::getInstance() },
        { ThrottlingEventId::COOLING_TH0, &ThrottlingStateFullPerformance::getInstance() },
    };

    if (ThrottlingEventId::HEATING_TH3 <= event && event <= ThrottlingEventId::HEATING_TH4) {
        for (const auto& p : heatingPairs) {
            if (event >= p.first) {
                ctx.setState(p.second, ts);
            }
        }
    }

    if (ThrottlingEventId::COOLING_TH0 <= event&& event <= ThrottlingEventId::COOLING_TH2) {
        for (const auto& p : coolingPairs) {
            if (event <= p.first) {
                ctx.setState(p.second, ts);
            }
        }
    }
}


/*!
 * @brief S3 Handle event implementation
 *
 * @param context: Throttling context
 * @param event: Thermal event id
 * @param ts: Timestamp of the event occurrence
 * @return void
 */
void ThrottlingState3::handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts)
{
    static std::vector<std::pair<ThrottlingEventId, ThrottlingState*>> heatingPairs = {
        { ThrottlingEventId::HEATING_TH4, &ThrottlingState4::getInstance() }
    };

    static std::vector<std::pair<ThrottlingEventId, ThrottlingState*>> coolingPairs = {
        { ThrottlingEventId::COOLING_TH3, &ThrottlingState2::getInstance() },
        { ThrottlingEventId::COOLING_TH2, &ThrottlingState1::getInstance() },
        { ThrottlingEventId::COOLING_TH1, &ThrottlingState0::getInstance() },
        { ThrottlingEventId::COOLING_TH0, &ThrottlingStateFullPerformance::getInstance() },
    };

    if (ThrottlingEventId::HEATING_TH4 == event) {
        for (const auto& p : heatingPairs) {
            if (event >= p.first) {
                ctx.setState(p.second, ts);
            }
        }
    }

    if (ThrottlingEventId::COOLING_TH0 <= event&& event <= ThrottlingEventId::COOLING_TH3) {
        for (const auto& p : coolingPairs) {
            if (event <= p.first) {
                ctx.setState(p.second, ts);
            }
        }
    }
}


/*!
 * @brief S4 Handle event implementation
 *
 * @param context: Throttling context
 * @param event: Thermal event id
 * @param ts: Timestamp of the event occurrence
 * @return void
 */
void ThrottlingState4::handle(ThrottlingManager &ctx, ThrottlingEventId &event, uint64_t ts)
{
    static std::vector<std::pair<ThrottlingEventId, ThrottlingState*>> coolingPairs = {
        { ThrottlingEventId::COOLING_TH4, &ThrottlingState3::getInstance() },
        { ThrottlingEventId::COOLING_TH3, &ThrottlingState2::getInstance() },
        { ThrottlingEventId::COOLING_TH2, &ThrottlingState1::getInstance() },
        { ThrottlingEventId::COOLING_TH1, &ThrottlingState0::getInstance() },
        { ThrottlingEventId::COOLING_TH0, &ThrottlingStateFullPerformance::getInstance() },
    };

    if (ThrottlingEventId::COOLING_TH0 <= event&& event <= ThrottlingEventId::COOLING_TH4) {
        for (const auto& p : coolingPairs) {
            if (event <= p.first) {
                ctx.setState(p.second, ts);
            }
        }
    }
}
