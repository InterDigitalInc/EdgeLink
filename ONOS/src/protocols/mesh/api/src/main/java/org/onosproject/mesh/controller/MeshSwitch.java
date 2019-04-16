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

import org.onosproject.meshmanager.api.message.MeshSBIMessage;

/**
 * Represents to provider facing side of a switch.
 */
public interface MeshSwitch {

    /**
     * Disconnects the switch by closing the TCP connection. Results in a call
     * to the channel handler's channelDisconnected method for cleanup
     */
    void disconnect();

    /**
     * Writes the message to the driver.
     * <p>
     * Note: Messages may be silently dropped/lost due to IOExceptions or
     * role. If this is a concern, then a caller should use barriers.
     * </p>
     *
     * @param msg the message to write
     */
    void sendMsg(MeshSBIMessage msg);
}
