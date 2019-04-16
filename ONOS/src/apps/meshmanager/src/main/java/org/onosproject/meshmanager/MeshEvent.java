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

import org.onosproject.event.AbstractEvent;
//import org.onosproject.meshmanager.MeshNode;
import org.onosproject.net.Link;

/**
 * Describes infrastructure mesh event.
 */
public class MeshEvent extends AbstractEvent<MeshEvent.Type, Link> {
//public class MeshEvent extends AbstractEvent<MeshEvent.Type, MeshNode> {

    /**
     * Type of mesh events.
     */
    public enum Type {
        /**
         * Signifies that stats have changed
         */
        MESH_STATS_CHANGED,

        /**
         * Signifies that a mesh alert was raised
         */
        MESH_ALERT_RAISED,

       /**
         * Signifies that a mesh alert got cleared
         */
        MESH_ALERT_CLEARED,


        /**
         * Signifies that a link has been removed.
         */
        LINK_REMOVED
    }

    /**
     * Creates an event of a given type and for the specified mesh node and the
     * current time.
     *
     * @param type mesh event type
     * @param mesh event mesh subject
     */
//    public MeshEvent(Type type, MeshNode node) {
    public MeshEvent(Type type, Link node) {
          super(type, node);
    }

    /**
     * Creates an event of a given type and for the specified mesh node and time.
     *
     * @param type mesh event type
     * @param mesh event mesh subject
     * @param time occurrence time
     */
//    public MeshEvent(Type type, MeshNode node, long time) {
    public MeshEvent(Type type, Link node, long time) {
         super(type, node, time);
    }

}

