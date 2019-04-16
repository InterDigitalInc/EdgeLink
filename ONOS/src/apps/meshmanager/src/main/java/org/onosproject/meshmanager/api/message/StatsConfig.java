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
public class StatsConfig {
    private Short accumulationInterval;
    private Short reportingInterval;
    private Byte statsReporting;

    /**
     *
     * @return
     */
    public Short getAccumulationInterval() {
        return accumulationInterval;
    }

    /**
     *
     * @param accumulationInterval
     */
    public void setAccumulationInterval(Short accumulationInterval) {
        if (accumulationInterval != null && accumulationInterval < 0) {
            throw new IllegalArgumentException("StatsConfig: accumulationInterval should be non-negative.");
        }
        this.accumulationInterval = accumulationInterval;
    }

    /**
     *
     * @return
     */
    public Short getReportingInterval() {
        return reportingInterval;
    }

    /**
     *
     * @param reportingInterval
     */
    public void setReportingInterval(Short reportingInterval) {
        if (reportingInterval != null && reportingInterval < 0) {
            throw new IllegalArgumentException("StatsConfig: reportingInterval should be non-negative.");
        }
        this.reportingInterval = reportingInterval;
    }

    /**
     *
     * @return
     */
    public Byte getStatsReporting() {
        return statsReporting;
    }

    /**
     *
     * @param statsReporting
     */
    public void setStatsReporting(Byte statsReporting) {
        if (statsReporting != null && !((statsReporting == 0) || (statsReporting == 1))) {
            throw new IllegalArgumentException("StatsConfig: statsReporting should be either 0 or 1.");
        }
        this.statsReporting = statsReporting;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "StatsConfig [" +
                "accumulationInterval=" + accumulationInterval +
                ", reportingInterval=" + reportingInterval +
                ", statsReporting=" + statsReporting + "]";
    }

}
