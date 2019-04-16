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

public class MeshNBIDeleteSliceReq extends MeshNBIRequest {
    private Short    sessionId;
    private Short    sliceId;

    public Short getSessionId() {
        return sessionId;
    }

    public void setSessionId(Short sessionId) {
        if (sessionId != null && sessionId < 0) {
            throw new IllegalArgumentException("DeleteSliceInput: sessionId should be non-negative.");
        }
        this.sessionId = sessionId;
    }

    public Short getSliceId() {
        return sliceId;
    }

    public void setSliceId(Short sliceId) {
        if (sliceId != null && sliceId < 0) {
            throw new IllegalArgumentException("DeleteSliceInput: sliceId should be non-negative.");
        }
        this.sliceId = sliceId;
    }

    @Override
    public String toString() {
        return "DeleteSliceInput [sessionId=" + sessionId + " ,sliceId="
                + sliceId + "]";
    }

    @Override
    public boolean validate() {
        if ((sessionId == null) || (sliceId == null)) {
            return false;
        }
        return true;
    }
}
