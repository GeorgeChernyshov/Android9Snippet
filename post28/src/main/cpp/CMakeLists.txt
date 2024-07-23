cmake_minimum_required(VERSION 3.4.1)

add_library(basic SHARED neural_network.cpp network_connector.cpp)

target_link_libraries(basic neuralnetworks android log)