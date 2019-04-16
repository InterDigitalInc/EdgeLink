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

public class MeshNBIGetAlertsReq extends MeshNBIRequest {

    private Short sessionId;
    private Short type;
    private Short alertId;

    public Short getSessionId() {
        return sessionId;
    }

    public void setSessionId(Short sessionId) {
        if (sessionId != null && sessionId < 0) {
            throw new IllegalArgumentException("GetAlertsInput, sessionId can not be negative ");
        }
        this.sessionId = sessionId;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        if (type != null && (type < 0) || (type > 1)) {
            throw new IllegalArgumentException("GetAlertsInput: type can either be 0(GET All) or 1(GET slice specific) configuration");
        }
        this.type = type;
    }

    public Short getAlertId() {
        return alertId;
    }

    public void setAlertId(Short alertId) {
        if (alertId != null && alertId < 0) {
            throw new IllegalArgumentException("GetAlertsInput: alertId should be non-negative.");
        }
        this.alertId = alertId;
    }

    @Override
    public String toString() {
        return "GetAlertsInput [sessionId=" + sessionId + ", type=" + type + ", alertId="
                + alertId + "]";
    }

    @Override
    public boolean validate() {
        if (sessionId == null || type == null || (type == 1 && alertId == null)) {
            return false;
        }
        return true;
    }
}
