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

public class MeshNBIGetMaxAvailableBWReq extends MeshNBIRequest {

    private Short sessionId;
    private Long nodeId;

    public Short getSessionId() {
        return sessionId;
    }

    public void setSessionId(Short sessionId) {
        if (sessionId != null && sessionId < 0) {
            throw new IllegalArgumentException("GetMaxAvailableBandwidthInput: sessionId can not be negative");
        }
        this.sessionId = sessionId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        if (nodeId != null && nodeId < 0) {
            throw new IllegalArgumentException("GetMaxAvailableBandwidthInput: nodeId can not be negative");
        }
        this.nodeId = nodeId;
    }
    @Override
    public String toString() {
        return "GetMaxAvailableBandwidthInput [sessionid=" + sessionId + ", nodeid=" + nodeId + "]";
    }

    @Override
    public boolean validate() {
        if ((sessionId == null) || (nodeId == null)) {
            return false;
        }
        return true;
    }
}
