#!/bin/sh

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

#route add -host <IP of GW node> dev <interface to reach the GW>
#route add -net <IP subnet of NonGW nodes>/<NonGW network mask> dev <interface to reach the GW> gw <IP of the GW node>

sudo route add -host 192.168.200.10 dev eth0
sudo route add -net 192.168.150.0/24 dev eth0 gw 192.168.200.10

