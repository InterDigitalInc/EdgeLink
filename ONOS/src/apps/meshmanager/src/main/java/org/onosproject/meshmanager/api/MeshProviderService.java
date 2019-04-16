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
import org.onosproject.net.provider.ProviderService;

/**
 * Service through which mesh providers can inject information to the
 * Mesh Manager.
 */
public interface MeshProviderService extends ProviderService<MeshProvider> {

    /**
     * New switch device connected.
     * @param id switch identifier
     */
    void deviceConnected(DeviceId id);

    /**
     * Switch device disconnected.
     * @param id switch identifier
     */
    void deviceDisconnected(DeviceId id);

    /**
     * Handle a message from the switch.
     * @param id switch identifier
     * @param msg received message
     */
    void handleMessage(DeviceId id, MeshSBIMessage msg);
}
