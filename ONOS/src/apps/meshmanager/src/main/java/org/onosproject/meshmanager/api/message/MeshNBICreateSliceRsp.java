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
public class MeshNBICreateSliceRsp extends MeshNBIResponse {
    private ResultCodeE returnCode;
    private Short sliceId;

    /**
     *
     */
    public MeshNBICreateSliceRsp() {
    }

    /**
     *
     * @return
     */
    public ResultCodeE getReturnCode() {
        return returnCode;
    }

    /**
     *
     * @param returnCode
     */
    public void setReturnCode(ResultCodeE returnCode) {
        this.returnCode = returnCode;
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
        this.sliceId = sliceId;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "CreateSliceOutput [returnCode=" + returnCode + ", sliceId=" + sliceId + "]";
    }
}
