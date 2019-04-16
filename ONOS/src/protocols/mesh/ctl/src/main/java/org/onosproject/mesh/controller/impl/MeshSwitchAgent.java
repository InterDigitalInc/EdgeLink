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
package org.onosproject.mesh.controller.impl;

import org.onosproject.meshmanager.api.message.MeshSBIMessage;


/**
 * Responsible for keeping track of the current set of switches
 * connected to the system. As well as whether they are in Master
 * role or not.
 */
public interface MeshSwitchAgent {

    /**
     * Add a switch that has just connected to the system.
     * @param sw the switch to add
     * @param sw the actual switch object.
     * @return true if added, false otherwise.
     */
    boolean addSwitch(InternalMeshSwitch sw);

    /**
     * Clear all state in controller switch maps for a switch that has
     * disconnected from the local controller. Also release control for
     * that switch from the global repository. Notify switch listeners.
     * @param sw the switch to remove.
     */
    void removeSwitch(InternalMeshSwitch sw);

    /**
     * Process a message coming from a switch.
     * @param sw the switch the message came on.
     * @param msg the message to process
     */
    void processMessage(InternalMeshSwitch sw, MeshSBIMessage msg);

}
