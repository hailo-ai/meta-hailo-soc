#ifndef THROTTLING_LOG_H
#define THROTTLING_LOG_H

#include <iostream>
#include <fstream>
#include <sstream>
#include <streambuf>
#include <syslog.h>
#include <mutex>

// Log levels
enum class LogLevel {
    EMERG,
    ALERT,
    CRIT,
    ERR,
    WARNING,
    NOTICE,
    INFO,
    DEBUG,
};

// Log output types
enum class LogOutput {
    STDOUT,
    STDERR,
    SYSLOG,
};

// Custom stream buffer for logging
class LogStreamBuf : public std::streambuf {
public:
    LogStreamBuf(LogOutput output, LogLevel logLvl);
    ~LogStreamBuf();

protected:
    int overflow(int c) override;

private:
    LogOutput output_;
    LogLevel logLvl;
    std::mutex mutex_;
    std::string buffer_;

    int logLevelToInt(LogLevel level);
    std::string logLevelToString(LogLevel level);
    void writeLog(const std::string& message);
};

// Logger class using std::ostream
class Logger : public std::ostream {
public:
    Logger(LogOutput output, LogLevel logLvl);

private:
    LogStreamBuf buf_;
};

extern Logger logger_stderr;
extern Logger logger_stdout;
extern Logger logger_syslog;
extern Logger logger_default;

#endif // THROTTLING_LOG_H
