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

# Get full path to script directory
SCRIPT=$(readlink -f "$0")
BASEDIR=$(dirname "$SCRIPT")

#preparation of downloading the tar file
sudo groupadd tomcat
sudo useradd -s /bin/false -g tomcat -d /opt/tomcat tomcat
cd /tmp
wget https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.39/bin/apache-tomcat-8.5.39.tar.gz
sudo mkdir /opt/tomcat

#installation of tomcat server
sudo tar xzvf apache-tomcat-8.5.39.tar.gz -C /opt/tomcat --strip-components=1
cd /opt/tomcat
sudo chgrp -R tomcat /opt/tomcat
sudo chmod -R g+r conf
sudo chmod g+x conf
sudo chown -R tomcat webapps/ work/ temp/ logs/

#setting of java to configure for the tomcat server
java="$(sudo update-java-alternatives -l)"
str_array=($java)
javaVar=${str_array[2]}
cp $BASEDIR/../orchestrator/config/tomcat.service.default $BASEDIR/../orchestrator/config/tomcat.service
javaVarStr="s|<javaVar>|$javaVar|g"
sed -i $javaVarStr $BASEDIR/../orchestrator/config/tomcat.service
sudo cp $BASEDIR/../orchestrator/config/tomcat.service /etc/systemd/system/
sudo sed -i 's/8080/8888/g' conf/server.xml

#starting services
sudo systemctl daemon-reload
sudo systemctl start tomcat

#back to initial state
cd $BASEDIR

