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
public class Sla {
    private BwReq bwReqDl;
    private BwReq bwReqUl;
    private PriorityConfig priorityConfig;
    private StatsConfig statsConfig;

    /**
     *
     * @return
     */
    public BwReq getBwReqDl() {
        return bwReqDl;
    }

    /**
     *
     * @param bwReqDl
     */
    public void setBwReqDl(BwReq bwReqDl) {
        this.bwReqDl = bwReqDl;
    }

    /**
     *
     * @return
     */
    public BwReq getBwReqUl() {
        return bwReqUl;
    }

    /**
     *
     * @param bwReqUl
     */
    public void setBwReqUl(BwReq bwReqUl) {
        this.bwReqUl = bwReqUl;
    }

    /**
     *
     * @return
     */
    public PriorityConfig getPriorityConfig() {
        return priorityConfig;
    }

    /**
     *
     * @param priorityConfig
     */
    public void setPriorityConfig(PriorityConfig priorityConfig) {
        this.priorityConfig = priorityConfig;
    }

    /**
     *
     * @return
     */
    public StatsConfig getStatsConfig() {
        return statsConfig;
    }

    /**
     *
     * @param statsConfig
     */
    public void setStatsConfig(StatsConfig statsConfig) {
        this.statsConfig = statsConfig;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "Sla [" +
                "bwReqDl=" + bwReqDl +
                ", bwReqUl=" + bwReqUl +
                ", priorityConfig=" + priorityConfig +
                ", statsConfig=" + statsConfig + "]";
    }
}
