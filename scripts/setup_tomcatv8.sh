#!/bin/bash

# Get full path to script directory
SCRIPT=$(readlink -f "$0")
BASEDIR=$(dirname "$SCRIPT")

#preparation of downloading the tar file
sudo groupadd tomcat
sudo useradd -s /bin/false -g tomcat -d /opt/tomcat tomcat
cd /tmp
wget http://apache.mirrors.ionfish.org/tomcat/tomcat-8/v8.5.40/bin/apache-tomcat-8.5.40.tar.gz
sudo mkdir /opt/tomcat

#installation of tomcat server
sudo tar xzvf apache-tomcat-8*tar.gz -C /opt/tomcat --strip-components=1
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

