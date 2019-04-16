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

/**
 *
 */
public class MeshNBIGetSliceReq extends MeshNBIRequest {
    private Short sessionId;
    private Short type;
    private Short sliceId;

    /**
     *
     * @return
     */
    public Short getSessionId() {
        return sessionId;
    }

    /**
     *
     * @param sessionId
     */
    public void setSessionId(Short sessionId) {
        if (sessionId != null && sessionId < 0) {
            throw new IllegalArgumentException("GetSliceInput: sessionId should be non-negative.");
        }
        this.sessionId = sessionId;
    }

    /**
     *
     * @return
     */
    public Short getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(Short type) {
        if (type != null && (type < 0) || (type > 1)) {
            throw new IllegalArgumentException("GetSliceInput: type can either be 0(GET All) " +
                                                       "or 1(GET slice specific) configuration");
        }
        this.type = type;
    }

    /**
     *
     * @return
     */
    public Short getSliceId() {
        return sliceId;
    }

    /**
     *
     * @param sliceId
     */
    public void setSliceId(Short sliceId) {
        if (sliceId != null && sliceId < 0) {
            throw new IllegalArgumentException("GetSliceInput: sliceId should be non-negative.");
        }
        this.sliceId = sliceId;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "GetSliceInput [sessionId=" + sessionId + " ,type=" + type + " ,sliceId"
                + sliceId + "]";
    }

    /**
     *
     * @return
     */
    @Override
    public boolean validate() {
        if (sessionId == null || type == null || (type == 1 && sliceId == null)) {
            return false;
        }
        return true;
    }
}
