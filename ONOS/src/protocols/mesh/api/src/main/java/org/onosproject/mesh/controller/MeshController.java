/* =============================================================================
**
**                     Copyright (c) 2019
**                 InterDigital Communications, Inc.
**
**                      All rights reserved.
**
**  Licensed under the terms and conditions provided in InterDigital Labs Public
**  License v.1 (the “License”). You may not use this file except in compliance 
**  with the License. Unless required by applicable law or agreed to in writing,
**  software distributed under the License is distributed on as “AS IS” BASIS,
**  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
**  the License for the specific language governing permissions and limitations
**  under the License.
**
** =============================================================================
*/
package org.onosproject.mesh.controller;

/**
 * Abstraction of an OpenFlow controller. Serves as a one stop
 * shop for obtaining OpenFlow devices and (un)register listeners
 * on OpenFlow events
 */
public interface MeshController {

    /**
     * Returns the actual switch for the given Mesh Dpid.
     * @param dpid Mesh switch ID
     * @return the interface to this switch
     */
    MeshSwitch getSwitch(MeshDpid dpid);

    /**
     * Register a listener for meta events that occur mesh devices.
     * @param listener the listener to notify
     */
    void addListener(MeshListener listener);

    /**
     * Unregister a listener.
     *
     * @param listener the listener to unregister
     */
    void removeListener(MeshListener listener);
}
