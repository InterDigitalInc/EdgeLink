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

public class MeshNBICustomerDeregistrationRsp extends MeshNBIResponse {

    private ResultCodeE returnCode;

    public MeshNBICustomerDeregistrationRsp(ResultCodeE returnCode) {
        this.returnCode = returnCode;
    }

    public ResultCodeE getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(ResultCodeE returnCode) {
        this.returnCode = returnCode;
    }
    @Override
    public String toString() {
        return "CustomerDeregistrationOutput [returnCode=" + returnCode + "]";
    }
}
