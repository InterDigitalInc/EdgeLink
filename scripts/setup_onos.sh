#!/bin/bash

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
sleep 30
/opt/onos/onos-1.9.0/bin/onos-service stop
rm output.txt
cd $BASEDIR
echo $BASEDIR
cp ../ONOS/config/FlowRuleManager.config /opt/onos/onos-1.9.0/apache-karaf-3.0.8/data/cache/bundle6/data/config/org/onosproject/net/flow/impl/FlowRuleManager.config
cp ../ONOS/config/MeshManager.config /opt/onos/onos-1.9.0/apache-karaf-3.0.8/data/cache/bundle6/data/config/org/onosproject/meshmanager/MeshManager.config

cd $BASEDIR
reset
