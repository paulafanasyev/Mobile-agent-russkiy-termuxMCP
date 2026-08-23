cmake_minimum_required(VERSION 3.22.1)
project(mobile_agent_llama)

add_library(mobile_agent_llama SHARED
  mobile_agent_llama.cpp
)

target_compile_features(mobile_agent_llama PRIVATE cxx_std_17)

target_link_libraries(mobile_agent_llama
  android
  log
)

# llama.cpp is intentionally an external native dependency. The Gradle build
# can point LLAMA_CPP_DIR at a pinned checkout containing llama.cpp + ggml.
# Keeping it external avoids shipping source in the JS package and lets CI
# build per-ABI artifacts (arm64-v8a/armeabi-v7a/x86_64).
if(DEFINED LLAMA_CPP_DIR)
  add_subdirectory(${LLAMA_CPP_DIR} ${CMAKE_BINARY_DIR}/llama.cpp)
  target_compile_definitions(mobile_agent_llama PRIVATE MOBILE_AGENT_LLAMA_CPP=1)
  target_include_directories(mobile_agent_llama PRIVATE ${LLAMA_CPP_DIR}/include)
  target_link_libraries(mobile_agent_llama PRIVATE llama)
endif()
