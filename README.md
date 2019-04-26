# EdgeLink

InterDigital’s EdgeLink (mmW Mesh Transport) is a wireless 5G solution that can deliver high capacity links with very low latency.  It is capable of multiplexing packetized Fronthaul and Backhaul traffic on a unified network under the control of Software Defined Networking (SDN) mesh controller over long range mmW links using high gain fixed antennas installed with Edgelink nodes .  The system can be installed indoor or outdoor to provide network connectivity over an unlicensed 60 GHz data link.

EdgeLink requires the presence of nodes linked in a mesh network, a mesh controller (based on ONOS), and an orchestrator.

## Getting started
* Clone the EdgeLink repo<br>
  `git clone https://github.com/InterDigitalInc/EdgeLink.git`<br>
  This step would create the *EdgeLink* directory and all the installation steps assumes this directory as the base directory.<br>
* [Setup runtime environment(Ubuntu/Java)](setenv.md)
* [Install and Configure mesh controller](ONOS/onos_install.md)
* [Install and Configure orchestrator](orchestrator/orchestrator_install.md)
* [Start mesh controller](ONOS/onos_exe.md)
* [Start orchestrator](orchestrator/orchestrator_exe.md)

## Licensing
Mesh controller and orchestrator are published under [InterDigital Labs Public License](LICENSE.md)<br>
ONOS is published under [Apache 2.0 License](LICENSE-ONOS.md)
