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
package org.onosproject.meshmanager;

import org.onosproject.meshmanager.api.message.MeshMessage;
import org.onosproject.meshmanager.api.message.MeshNBIRequest;
import org.onosproject.meshmanager.api.message.MeshNBIResponse;

/**
 * Service interface exported by MESH application.
 */
public interface MeshService {

    /**
     * Process a NBI Request message
     * @param req NBI Request message
     * @return NBI Response message
     */
    MeshNBIResponse processNBIRequest(MeshNBIRequest req);

    /**
     * Adds a Mesh message to the message queue
     * @return boolean success or not
     */
    boolean addToMsgQ(MeshMessage msg);

    /**
     *
     * @param nbiNotificationListener
     */
    void registerNbiNotificationListener(NbiNotificationListener nbiNotificationListener);

    /**
     *
     */
    void unregisterNbiNotificationListener();


    // Debug getter methods

    /**
     * Retrieve the Mesh database
     * @return MeshDB object
     */
    MeshDB getMeshDB();

    /**
     * Retrieve the Mesh Slice Manager
     * @return MeshSliceManager object
     */
    MeshSliceManager getMeshSliceManager();

    /**
     * Retrieve the Mesh Routing Manager
     * @return MeshRoutingManager object
     */
    MeshRoutingManager getMeshRoutingManager();

}
