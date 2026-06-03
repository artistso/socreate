# SoCreate Native Rendering Library
# Future home of GPU-accelerated rendering pipeline

cmake_minimum_required(VERSION 3.22.1)
project("socreate-native")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add native rendering library
add_library(
    socreate-native
    SHARED
    socreate_renderer.cpp
)

# Find Android libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv3)
find_library(egl-lib EGL)

# Link libraries
target_link_libraries(
    socreate-native
    ${log-lib}
    ${android-lib}
    ${gles-lib}
    ${egl-lib}
)
