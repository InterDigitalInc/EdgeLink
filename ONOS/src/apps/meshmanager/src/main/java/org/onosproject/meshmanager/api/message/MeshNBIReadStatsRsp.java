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

public class MeshNBIReadStatsRsp extends MeshNBIResponse {

    private ResultCodeE       returnCode;
    private Short             numSlice;
    private List<StatsReport> statsReportList;

    public ResultCodeE getReturnCode() {
        return returnCode;
    }

    public Short getNumSlice() {
        return numSlice;
    }

    public List<StatsReport> getStatsReportList() {
        return statsReportList;
    }

    public void setNumSlice(Short numSlice) {
        this.numSlice = numSlice;
    }

    public void setReturnCode(ResultCodeE returnCode) {
        this.returnCode = returnCode;
    }

    public void setStatsReportList(List<StatsReport> statsReportList) {
        this.statsReportList = statsReportList;
    }

    public MeshNBIReadStatsRsp (ResultCodeE returnCode, Short numSlice,
                                List<StatsReport> statsReportList) {
        this.returnCode = returnCode;
        this.numSlice = numSlice;
        this.statsReportList = statsReportList;
    }

    @Override
    public String toString() {
        return "ReadStatisticsOutput [returnCode=" + returnCode + ", numSlice=" + numSlice + ", statsReport="
                + statsReportList + "]";
    }
}
