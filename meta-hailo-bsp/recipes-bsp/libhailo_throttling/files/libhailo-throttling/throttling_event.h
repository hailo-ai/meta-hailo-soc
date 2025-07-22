#ifndef THROTTLING_EVENT_H
#define THROTTLING_EVENT_H

#include <string>

enum class ThrottlingEventId {
    HEATING_TH0,
    HEATING_TH1,
    HEATING_TH2,
    HEATING_TH3,
    HEATING_TH4,
    HEATING_MAX,

    COOLING_TH0,
    COOLING_TH1,
    COOLING_TH2,
    COOLING_TH3,
    COOLING_TH4,
    COOLING_MAX,

    STOP_WATCH,
};

std::string ThrottlingEventToString(ThrottlingEventId ev);

#endif // THROTTLING_EVENT_H