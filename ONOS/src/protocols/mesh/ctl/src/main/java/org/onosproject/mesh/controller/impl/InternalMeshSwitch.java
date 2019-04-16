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

import org.onosproject.mesh.controller.MeshDpid;
import org.onosproject.mesh.controller.MeshSwitch;
import org.onosproject.meshmanager.api.message.MeshSBIMessage;
import org.onosproject.net.driver.HandlerBehaviour;

/**
 * Represents the driver side of a Mesh switch.
 * This interface should never be exposed to consumers.
 */
public interface InternalMeshSwitch extends MeshSwitch, HandlerBehaviour {

    /**
     * Announce to the Mesh agent that this switch has connected.
     * @return true if successful, false if duplicate switch.
     */
    boolean add();

    /**
     * Remove this switch from the Mesh agent.
     */
    void remove();

    /**
     * Gets the Mesh DataPath ID of the switch.
     *
     * @return the mesh switch dpid
     */
    MeshDpid getDpid();

    /**
     * Checks if the switch is still connected.
     *
     * @return whether the switch is still connected
     */
    boolean isConnected();

    /**
     * Handle a message from the switch.
     * @param msg the message to handle
     */
    void handleMessage(MeshSBIMessage msg);
}
