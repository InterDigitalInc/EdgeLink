#!/bin/bash

# Get full path to script directory
SCRIPT=$(readlink -f "$0")
BASEDIR=$(dirname "$SCRIPT")

#installation of orchestrator
sudo chmod -Rf 777 /opt/tomcat/webapps/
mkdir /opt/tomcat/webapps/orchestrator
cd $BASEDIR
cd ..
cp -Rf orchestrator/src/* /opt/tomcat/webapps/orchestrator/

#back to initial state
cd $BASEDIR

