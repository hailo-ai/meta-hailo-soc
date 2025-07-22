cmake_minimum_required(VERSION 3.10)
project(hailo-throttling-app)

# Set C++ standard
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Define compile-time macros
add_definitions(-DLOG_ALERT=1)

# Include directories
include_directories(${CMAKE_SOURCE_DIR})

# Source files
set(SOURCES main.cpp)

# Link against libhailo-throttling
add_executable(hailo-simple-throttling-app ${SOURCES})
target_link_libraries(hailo-simple-throttling-app PRIVATE hailo-throttling pthread)

# Installation rules
install(TARGETS hailo-simple-throttling-app
    RUNTIME DESTINATION bin
)
