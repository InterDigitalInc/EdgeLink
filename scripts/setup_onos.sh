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

echo $BASEDIR

#Build command
cd ONOS/src
tools/build/onos-buck build onos --show-output

#Copy build output to target directory
sudo mkdir /opt/onos
sudo chmod -Rf 777 /opt/onos
cp buck-out/gen/tools/package/onos-package/onos.tar.gz /opt/onos

#Extract build output
cd /opt/onos
tar xvzf onos.tar.gz

#Configure SYS_APPS in ./bin/onos-service
sed -i "s/^SYS_APPS=.*/SYS_APPS=drivers,meshmanager,meshcli,meshnbi,mesh,openflow-base/" /opt/onos/onos-1.9.0/bin/onos-service

#starting the service for the first time to create the config files out of the binaries
echo -e "\e[31mWait while the system is being set up for ONOS, that should take about 30 seconds. \e[33mDO NOT FORCE EXIT."
/opt/onos/onos-1.9.0/bin/onos-service start > output.txt &

#waiting for everything to be created and up

#wait for first file to exist
while !(test -f /opt/onos/onos-1.9.0/apache-karaf-3.0.8/data/cache/bundle6/data/config/org/onosproject/net/flow/impl/FlowRuleManager.config)
do
  echo -ne "."
  sleep 1
done

#wait for second file to exist
while !(test -f /opt/onos/onos-1.9.0/apache-karaf-3.0.8/data/cache/bundle6/data/config/org/onosproject/meshmanager/MeshManager.config)
do
  echo -ne ".."
  sleep 1
done

/opt/onos/onos-1.9.0/bin/onos-service stop
rm output.txt
cd $BASEDIR
echo $BASEDIR
cp ../ONOS/config/FlowRuleManager.config /opt/onos/onos-1.9.0/apache-karaf-3.0.8/data/cache/bundle6/data/config/org/onosproject/net/flow/impl/FlowRuleManager.config
cp ../ONOS/config/MeshManager.config /opt/onos/onos-1.9.0/apache-karaf-3.0.8/data/cache/bundle6/data/config/org/onosproject/meshmanager/MeshManager.config

cd $BASEDIR
reset
