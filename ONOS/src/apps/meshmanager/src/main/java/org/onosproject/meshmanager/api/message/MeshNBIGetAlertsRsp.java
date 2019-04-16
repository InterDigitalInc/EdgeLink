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

import java.util.List;

/**
 *
 */
public class MeshNBIGetAlertsRsp extends MeshNBIResponse {

    private ResultCodeE returnCode;
    private Short numAlert;
    private List<AlertsReport> alertsReportList;

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
    public Short getNumAlert() {
        return numAlert;
    }

    /**
     *
     * @param numAlert
     */
    public void setNumAlert(Short numAlert) {
        this.numAlert = numAlert;
    }

    /**
     *
     * @return
     */
    public List<AlertsReport> getAlertsReportList() {
        return alertsReportList;
    }

    /**
     *
     * @param alertsReportList
     */
    public void setAlertIdList(List<AlertsReport> alertsReportList) {
        this.alertsReportList = alertsReportList;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "GetAlertsOutput [returnCode=" + returnCode + ", numAlert=" + numAlert + ", alertsReportList="
                + alertsReportList + "]";
    }

}
