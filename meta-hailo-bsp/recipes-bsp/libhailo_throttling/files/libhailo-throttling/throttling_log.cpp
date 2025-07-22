#include <map>
#include "throttling_log.h"


#ifdef DEFAULT_LOG_LEVEL
    #define DEFAULT_LOG_LEVEL (THROTTLING_LOG_LEVEL)
#else
    #define DEFAULT_LOG_LEVEL (LOG_INFO) // 0-EMERG, 1-ALERT, 2-CRIT, 3-ERR, 4-WARNING, 5-NOTICE, 6-INFO, 7-DEBUG
#endif

#ifdef DEFAULT_LOG_OUTPUT
    #define DEFAULT_LOG_OUTPUT (THROTTLING_LOG_OUTPUT)
#else
    #define DEFAULT_LOG_OUTPUT (0) // 0-STDOUT, 1-STDERR, 2-SYSLOG
#endif


LogOutput intToLogOutput(int output)
{
    std::map<int, LogOutput> outputMap = {
        {0, LogOutput::STDOUT},
        {1, LogOutput::STDERR},
        {2, LogOutput::SYSLOG}
    };
    auto it = outputMap.find(output);
    return (it != outputMap.end()) ? it->second : LogOutput::STDOUT;
}

LogLevel intToLogLevel(int level) 
{
    std::map<int, LogLevel> levelMap = {
        {0, LogLevel::EMERG},
        {1, LogLevel::ALERT},
        {2, LogLevel::CRIT},
        {3, LogLevel::ERR},
        {4, LogLevel::WARNING},
        {5, LogLevel::NOTICE},
        {6, LogLevel::INFO},
        {7, LogLevel::DEBUG}
    };
    auto it = levelMap.find(level);
    return (it != levelMap.end()) ? it->second : LogLevel::INFO;
};


LogLevel defaultLogLevel = intToLogLevel(DEFAULT_LOG_LEVEL);
LogOutput defaultLogOutput = intToLogOutput(DEFAULT_LOG_OUTPUT);

// Custom stream buffer for logging
LogStreamBuf::LogStreamBuf(LogOutput output, LogLevel logLvl) : output_(output), logLvl(logLvl)
{
    if (output_ == LogOutput::SYSLOG) {
        openlog("throttling-mgr", LOG_CONS | LOG_PID | LOG_NDELAY, LOG_USER);
        setlogmask(LOG_UPTO(logLevelToInt(logLvl)));
    }
}

LogStreamBuf::~LogStreamBuf()
{
    if (output_ == LogOutput::SYSLOG) {
        closelog();
    }
}

int LogStreamBuf::overflow(int c)
{
    if (c != EOF) {
        std::lock_guard<std::mutex> lock(mutex_);
        buffer_ += static_cast<char>(c);

        // Flush the buffer on newline
        if (c == '\n') {
            writeLog(buffer_);
            buffer_.clear();
        }
    }
    return c;
}

int LogStreamBuf::logLevelToInt(LogLevel level) 
{
    std::map<LogLevel, int> levelMap = {
        {LogLevel::EMERG, LOG_EMERG},
        {LogLevel::ALERT, LOG_ALERT},
        {LogLevel::CRIT, LOG_CRIT},
        {LogLevel::ERR, LOG_ERR},
        {LogLevel::WARNING, LOG_WARNING},
        {LogLevel::NOTICE, LOG_NOTICE},
        {LogLevel::INFO, LOG_INFO},
        {LogLevel::DEBUG, LOG_DEBUG}
    };
    auto it = levelMap.find(level);
    return (it != levelMap.end()) ? it->second : LOG_INFO;
};

std::string LogStreamBuf::logLevelToString(LogLevel level)
{
    std::map<LogLevel, std::string> levelMap = {
        {LogLevel::EMERG, "EMERG"},
        {LogLevel::ALERT, "ALERT"},
        {LogLevel::CRIT, "CRIT"},
        {LogLevel::ERR, "ERROR"},
        {LogLevel::WARNING, "WARNING"},
        {LogLevel::NOTICE, "NOTICE"},
        {LogLevel::INFO, "INFO"},
        {LogLevel::DEBUG, "DEBUG"}
    }; 
    auto it = levelMap.find(level);
    return (it != levelMap.end()) ? it->second : "UNKNOWN";
};

void LogStreamBuf::writeLog([[maybe_unused]] const std::string& message)
{
#ifdef THROTTLING_LOG_CTRL  
    switch (output_) {
        case LogOutput::STDOUT:
            std::cout << "[" + logLevelToString(logLvl) +"] " + message;
            break;
        case LogOutput::STDERR:
            std::cerr << "[" + logLevelToString(logLvl) +"] " + message;
            break;
        case LogOutput::SYSLOG:
            syslog(logLevelToInt(logLvl), "%s", message.c_str());
            break;
    }
#endif    
}

// Logger class using std::ostream
Logger::Logger(LogOutput output, LogLevel logLvl) : std::ostream(&buf_), buf_(output, logLvl) {}

// Declare global loggers
Logger logger_stderr(LogOutput::STDERR, defaultLogLevel);
Logger logger_stdout(LogOutput::STDOUT, defaultLogLevel);
Logger logger_syslog(LogOutput::SYSLOG, defaultLogLevel);
Logger logger_default(defaultLogOutput, defaultLogLevel);

