cmake_minimum_required(VERSION 3.10)
project(libhailo-throttling)

# Set C++ standard to C++17
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Logging options
option(THROTTLING_LOG_CTRL "Enable throttling log control" OFF)
option(THROTTLING_LOG_OUTPUT "Logging output level" OFF)
option(THROTTLING_LOG_LEVEL "Logging level" OFF)

if(THROTTLING_LOG_CTRL)
    add_definitions(-DTHROTTLING_LOG_CTRL=${THROTTLING_LOG_CTRL})
endif()

if(THROTTLING_LOG_OUTPUT)
    add_definitions(-DTHROTTLING_LOG_OUTPUT=${THROTTLING_LOG_OUTPUT})
endif()

if(THROTTLING_LOG_LEVEL)
    add_definitions(-DTHROTTLING_LOG_LEVEL=${THROTTLING_LOG_LEVEL})
endif()

# Set version (passed from Yocto)
set(LIBRARY_VERSION "1.7.0")  # Default version (set in the recipe as PV)
set(LIBRARY_SOVERSION "1")    # This will be the '1' in libhailo-throttling.so.1

# Retrieve PV (passed from Yocto)
set(PV "1.7.0")  # This should be passed from Yocto (in the recipe)

# Check if LIBRARY_VERSION matches PV
if(NOT LIBRARY_VERSION STREQUAL PV)
    message(FATAL_ERROR "LIBRARY_VERSION (${LIBRARY_VERSION}) does not match PV (${PV}). Please ensure they are the same.")
endif()

# Include directories
include_directories(${CMAKE_SOURCE_DIR})

# Source files
file(GLOB SOURCES *.cpp)
file(GLOB HEADERS *.h)

# Define library output with version as suffix
set(LIBRARY_NAME "hailo-throttling")

# Create shared library with version and so version
add_library(${LIBRARY_NAME} SHARED ${SOURCES})

# Set the version and so version
set_target_properties(${LIBRARY_NAME} PROPERTIES
    VERSION ${LIBRARY_VERSION}
    SOVERSION ${LIBRARY_SOVERSION}
    OUTPUT_NAME ${LIBRARY_NAME}  # Ensures the library is named correctly
)

# Install the library with versioned names
install(TARGETS ${LIBRARY_NAME}
    LIBRARY DESTINATION lib
    COMPONENT library
)

# Install header files
install(FILES ${HEADERS}
    DESTINATION include
)

# Ensure the symlink is created properly
install(CODE "execute_process(COMMAND ln -sf libhailo_throttling.so.${LIBRARY_VERSION} ${CMAKE_INSTALL_PREFIX}/lib/libhailo_throttling.so)")
install(CODE "execute_process(COMMAND ln -sf libhailo_throttling.so.${LIBRARY_SOVERSION} ${CMAKE_INSTALL_PREFIX}/lib/libhailo_throttling.so.${LIBRARY_SOVERSION})")
