#!/bin/sh

#route add -host <IP of GW node> dev <interface to reach the GW>
#route add -net <IP subnet of NonGW nodes>/<NonGW network mask> dev <interface to reach the GW> gw <IP of the GW node>

route add -host 192.168.200.10 dev eth0
route add -net 192.168.150.0/24 dev eth0 gw 192.168.200.10

