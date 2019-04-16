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

public class MeshNBICustomerRegistrationRsp extends MeshNBIResponse {

    private ResultCodeE returnCode;
    private Short sessionDuration;
    private Short sessionId;
    private Byte apiVersion;

    public MeshNBICustomerRegistrationRsp(Byte apiVersion,
                                          ResultCodeE returnCode,
                                          Short sessionDuration,
                                          Short sessionId) {
        this.apiVersion = apiVersion;
        this.returnCode = returnCode;
        this.sessionDuration = sessionDuration;
        this.sessionId = sessionId;
    }

    public ResultCodeE getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(ResultCodeE returnCode) {
        this.returnCode = returnCode;
    }

    public Short getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(Short sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public Short getSessionId() {
        return sessionId;
    }

    public void setSessionId(Short sessionId) {
        this.sessionId = sessionId;
    }

    public Byte getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(Byte apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public String toString() {
        return "CustomerRegistrationOutput [returnCode=" + returnCode + ", sessionDuration=" + sessionDuration
                + ", sessionId=" + sessionId + ", apiVersion=" + apiVersion + "]";
    }
}
