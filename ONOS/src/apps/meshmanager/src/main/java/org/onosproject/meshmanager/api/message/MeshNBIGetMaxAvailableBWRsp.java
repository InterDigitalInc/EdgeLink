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

public class MeshNBIGetMaxAvailableBWRsp extends MeshNBIResponse {

    private ResultCodeE returnCode;
    private Short maxBwUl;
    private Short maxBwDl;

    public ResultCodeE getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(ResultCodeE returnCode) {
        this.returnCode = returnCode;
    }

    public Short getMaxBwUl() {
        return maxBwUl;
    }

    public void setMaxBwUl(Short maxBwUl) {
        this.maxBwUl = maxBwUl;
    }

    public Short getMaxBwDl() {
        return maxBwDl;
    }

    public void setMaxBwDl(Short maxBwDl) {
        this.maxBwDl = maxBwDl;
    }

    @Override
    public String toString() {
        return "GetMaxAvailableBandwidthOutput [returnCode=" + returnCode + " maxBwUl= " + maxBwUl + " maxBwDl= " + maxBwDl +" ]";
    }
}
