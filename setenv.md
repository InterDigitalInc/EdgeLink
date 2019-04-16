# Runtime Environment Setup
- Guidance on runtime pre-requisites installation

## Overview
EdgeLink requires the following pre-requisites
- [Ubuntu](#ubuntu)
- [Java](#Java)


## Ubuntu
There are many installation guides out there; we use [this one](https://tutorials.ubuntu.com/tutorial/tutorial-install-ubuntu-desktop#0)

Version we use:
- 16.04 LTS

- Requirements
>- Account with sudo access
>- Download size about 150MB, installed size about 500MB
>- As part of the OS setup, disable the network management system and configure a static IP address in the /etc/network/interfaces to use as the main controller IP address used to reach the GW node.
>- Make sure the IP routing tables do not get overwritten by a network management system

## Java

Java Version we use:
- Java8

How we do it:

Run a script to automatically install the oracle java version 8.
```
./scripts/setup_java.sh
