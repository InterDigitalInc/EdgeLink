#!/bin/bash

#  ~ =============================================================================
#  ~
#  ~                     Copyright (c) 2019
#  ~                 InterDigital Communications, Inc.
#  ~
#  ~                      All rights reserved.
#  ~
#  ~  Licensed under the terms and conditions provided in InterDigital Labs Public
#  ~  License v.1 (the “License”). You may not use this file except in compliance
#  ~  with the License. Unless required by applicable law or agreed to in writing,
#  ~  software distributed under the License is distributed on as “AS IS” BASIS,
#  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
#  ~  the License for the specific language governing permissions and limitations
#  ~  under the License.
#  ~
#  ~ =============================================================================

sudo apt-get install software-properties-common -y && \
sudo add-apt-repository ppa:webupd8team/java -y && \
sudo apt-get update && \
sudo apt-get install default-jdk -y && \
sudo apt-get install openjfx -y

