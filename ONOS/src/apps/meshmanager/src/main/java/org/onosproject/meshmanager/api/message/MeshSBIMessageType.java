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

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public enum MeshSBIMessageType {
    NC_HELLO(1),
    NC_CONFIG_SETUP(2),
    NC_CONFIG_CONFIRM(3),
    NC_REPORT_INDICATION(4),
    NC_RESET(5),
    NC_CONNECT(6),
    NC_CONNECT_ACK(7),
    NC_SOCKET_DOWN(8);

    public int value;
    public static final Map<Integer, MeshSBIMessageType> lookup = new HashMap<>();

    /**
     *
     * @param value
     */
    MeshSBIMessageType(int value) {
        this.value = value;
    }

    /**
     *
     * @return
     */
    public int getValue() {
        return value;
    }

    /**
     *
     */
    static {
        // Create reverse lookup hash map
        for (MeshSBIMessageType type : MeshSBIMessageType.values()) {
            lookup.put(type.getValue(), type);
        }
    }
}


