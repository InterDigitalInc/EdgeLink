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

public class AlertsReport {
    private Short alertId;
    private AlertStateE alertState;
    private AlertTypeE alertType;
    private Short alertInfo;


    public Short getAlertId() {
        return alertId;
    }

    public AlertStateE getAlertState() {
        return alertState;
    }

    public AlertTypeE getAlertType() {
        return alertType;
    }

    public Short getAlertInfo() {
        return alertInfo;
    }

    public void setAlertId(Short alertId) {
        if (alertId < 0) {
            throw new IllegalArgumentException("AlertsReport: alertId can not be negative valiue.");
        }
        this.alertId = alertId;
    }

    public void setAlertState(AlertStateE alertState) {
        if ((alertState.getValue() < 0) || alertState.getValue() > 1) {
            throw new IllegalArgumentException("AlertsReport: Invalid alertState.");
        }
        this.alertState = alertState;
    }

    public void setAlertType(AlertTypeE alertType) {
        if (alertType.getValue() != 0)  {
            throw new IllegalArgumentException("AlertsReport: Invalid alertType.");
        }
        this.alertType = alertType;
    }

    public void setAlertInfo(Short alertInfo) {
        if (alertInfo < 0) {
            throw new IllegalArgumentException("AlertsReport: alertInfo can not be negative valiue.");
        }
        this.alertInfo = alertInfo;
    }



    @Override
    public String toString() {
        return "AlertsReport [alertId=" + alertId + ", alertState=" + alertState + ", alertType=" + alertType + ", alertInfo="
                + alertInfo + "]";
    }
}
