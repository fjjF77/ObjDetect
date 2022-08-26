project(yolov5ncnn)

cmake_minimum_required(VERSION 3.4.1)

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

add_library(yolov5ncnn SHARED yolov5ncnn_jni.cpp) #IMPORTED

#set_target_properties(yolov5ncnn
#	PROPERTIES IMPORTED_LOCATION
#	${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/libyolov5ncnn.so)

target_link_libraries(yolov5ncnn
    ncnn

    jnigraphics
)