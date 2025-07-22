#ifndef THROTTLING_MANAGER_TYPES_H
#define THROTTLING_MANAGER_TYPES_H

#ifdef __cplusplus
#include <functional>
#endif

#include <hailo-thermal-engine.h>

#ifdef __cplusplus
class ThrottlingManager;

using Callback = std::function<void(ThrottlingManager &)>;

// Generic Singleton base class template
template <typename T>
class Singleton {
protected:
    Singleton() = default;
    ~Singleton() = default;
    
    Singleton(const Singleton&) = delete;
    Singleton& operator=(const Singleton&) = delete;
    Singleton(Singleton&&) = delete;
    Singleton& operator=(Singleton&&) = delete;

public:
    static T& getInstance() {
        static T instance;
        return instance;
    }
};

enum class ThrottlingStateId
{
    UNINIT = -1,
    FULL_PERFORMANCE,
    S0,
    S1,
    S2,
    S3,
    S4,
};

#endif

#endif // THROTTLING_MANAGER_TYPES_H