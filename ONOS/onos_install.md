# Mesh controller

## Goal
- Guidance on installing and configuring the mesh controller

## Pre-requisites
- [Setup runtime environment(Ubuntu/Java)](../setenv.md)

## ONOS

ONOS version we use:
- ONOS 1.9.0

How we do it:

Run a script to build, install ONOS, install the mesh controller apps, configure the mesh controller.
```
./scripts/setup_onos.sh
```

## Nodes reachability 

Routing tables mush be modified to ensure nodes reachability (GW and NonGW)<br>
Manually update the file setup_routes.sh with relevant IP addresses<br>
>*route add -host \<IP of GW node\> dev \<interface to reach the GW\>*<br>
>*route add -net \<IP subnet of NonGW nodes\>\/\<NonGW network mask\> dev <interface to reach the GW> gw \<IP of the GW node\>*
```
vi scripts/setup_routes.sh
```

Manually copy the file to init.d directory to make the changes persistent based on the IP route changes<br>
```
cp scripts/setup_routes.sh /etc/init.d/
```

Run the script to setup the routes
```
./scripts/setup_routes.sh
```

