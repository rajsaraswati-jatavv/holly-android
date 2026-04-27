cmake_minimum_required(VERSION 3.22.1)

project("holly")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add llama.cpp library
add_library(llama SHARED
    llama.cpp
)

# Add include directories
include_directories(
    ${CMAKE_SOURCE_DIR}/include
)

# Find required packages
find_package(log REQUIRED)

# Link libraries
target_link_libraries(llama
    android
    log
    ${CMAKE_SOURCE_DIR}/jniLibs/${ANDROID_ABI}/libllama.so
)

# Build the JNI wrapper
add_library(holly-jni SHARED
    holly_jni.cpp
)

target_link_libraries(holly-jni
    llama
    android
    log
)
