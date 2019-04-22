#!/bin/bash

sudo apt-get install software-properties-common -y && \
sudo add-apt-repository ppa:webupd8team/java -y && \
sudo apt-get update && \
sudo apt-get install default-jdk -y && \
sudo apt-get install openjfx -y

