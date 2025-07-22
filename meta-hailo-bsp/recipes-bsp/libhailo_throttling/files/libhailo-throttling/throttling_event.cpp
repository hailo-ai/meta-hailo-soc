
#include <map>
#include "throttling_event.h"

std::string ThrottlingEventToString(ThrottlingEventId ev) {
    static const std::map<ThrottlingEventId, std::string> eventMap = {
        {ThrottlingEventId::HEATING_TH0, "HEATING_TH0"},
        {ThrottlingEventId::HEATING_TH1, "HEATING_TH1"},
        {ThrottlingEventId::HEATING_TH2, "HEATING_TH2"},
        {ThrottlingEventId::HEATING_TH3, "HEATING_TH3"},
        {ThrottlingEventId::HEATING_TH4, "HEATING_TH4"},
        {ThrottlingEventId::COOLING_TH0, "COOLING_TH0"},
        {ThrottlingEventId::COOLING_TH1, "COOLING_TH1"},
        {ThrottlingEventId::COOLING_TH2, "COOLING_TH2"},
        {ThrottlingEventId::COOLING_TH3, "COOLING_TH3"},
        {ThrottlingEventId::COOLING_TH4, "COOLING_TH4"},
        {ThrottlingEventId::STOP_WATCH, "STOP_WATCH"}
    };

    auto it = eventMap.find(ev);
    return (it != eventMap.end()) ? it->second : "THROTTLING_EV_?";
}
