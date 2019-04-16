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
package org.onosproject.meshmanager.api;

import org.onosproject.meshmanager.api.message.MeshSBIMessage;
import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.Provider;

/**
 * Abstraction of a mesh provider.
 */
public interface MeshProvider extends Provider {

    /**
     * Disconnect the mesh switch device.
     * @param id switch identifier
     */
    void disconnectDevice(DeviceId id);

    /**
     * Send a message to the switch.
     * @param id switch identifier
     * @param msg message to send
     */
    void sendMessage(DeviceId id, MeshSBIMessage msg);

}
