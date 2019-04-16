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
package org.onosproject.meshmanager.api.message;

import org.onosproject.net.DeviceId;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Base class for all Mesh Device Events.
 */
public abstract class MeshDeviceEvent extends MeshMessage {
    DeviceId deviceId;
    private final Logger log = getLogger(getClass());

    /**
     * Set the device where/to this belong is from/or going to
     * @return
     */
    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Get the device where/to this belong is from/or going to
     * @return
     */
    public DeviceId getDeviceId() {
        return deviceId;
    }

}
