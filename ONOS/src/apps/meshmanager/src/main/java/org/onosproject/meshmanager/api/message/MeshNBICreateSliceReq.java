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

public class MeshNBICreateSliceReq extends MeshNBIRequest {

    private Long  nodeId;
    private Long  poaId;
    private Short sessionId;
    private Sla   sla;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        if (nodeId != null && nodeId < 0) {
            throw new IllegalArgumentException("CreateSliceInput: nodeId should be non-negative.");
        }
        this.nodeId = nodeId;
    }

    public Long getPoaId() {
        return poaId;
    }

    public void setPoaId(Long poaId) {
        if (poaId != null && poaId < 0) {
            throw new IllegalArgumentException("CreateSliceInput: poaId should be non-negative.");
        }
        this.poaId = poaId;
    }

    public Short getSessionId() {
        return sessionId;
    }

    public void setSessionId(Short sessionId) {
        if (sessionId != null && sessionId < 0) {
            throw new IllegalArgumentException("CreateSliceInput: sessionId should be non-negative.");
        }
        this.sessionId = sessionId;
    }

    public Sla getSla() {
        return sla;
    }

    public void setSla(Sla sla) {
        this.sla = sla;
    }

    @Override
    public String toString() {
        return "CreateSliceInput [nodeId=" + nodeId + ", poaId=" + poaId + ", sessionId=" + sessionId + "," + "sla="
                + sla + "]";
    }

    @Override
    public boolean validate() {
        PriorityConfig priorityConfig = null;
        StatsConfig statsConfig = null;
        BwReq bwReqDl = null;
        BwReq bwReqUl = null;

        if (sla != null) {
            bwReqDl = sla.getBwReqDl();
            bwReqUl = sla.getBwReqUl();
            priorityConfig = sla.getPriorityConfig();
            statsConfig = sla.getStatsConfig();
        }

        Integer bwReqDlCir = null;
        Integer bwReqDlCbs = null;
        Short bwReqDlEir = null;
        Integer bwReqDlEbs = null;
        Integer bwReqDlCommMaxLatency = null;
        Integer bwReqDlCommMaxJitter = null;
        Short bwReqDlCommMaxFlr = null;
        if (bwReqDl != null) {
            bwReqDlCir = bwReqDl.getCir();
            bwReqDlCbs = bwReqDl.getCbs();
            bwReqDlEir = bwReqDl.getEir();
            bwReqDlEbs = bwReqDl.getEbs();
            bwReqDlCommMaxLatency = bwReqDl.getCommMaxLatency();
            bwReqDlCommMaxJitter = bwReqDl.getCommMaxJitter();
            bwReqDlCommMaxFlr = bwReqDl.getCommMaxFlr();
        }

        Integer bwReqUlCir = null;
        Integer bwReqUlCbs = null;
        Short bwReqUlEir = null;
        Integer bwReqUlEbs = null;
        Integer bwReqUlCommMaxLatency = null;
        Integer bwReqUlCommMaxJitter = null;
        Short bwReqUlCommMaxFlr = null;
        if (bwReqUl != null) {
            bwReqUlCir = bwReqUl.getCir();
            bwReqUlCbs = bwReqUl.getCbs();
            bwReqUlEir = bwReqUl.getEir();
            bwReqUlEbs = bwReqUl.getEbs();
            bwReqUlCommMaxLatency = bwReqUl.getCommMaxLatency();
            bwReqUlCommMaxJitter = bwReqUl.getCommMaxJitter();
            bwReqUlCommMaxFlr = bwReqUl.getCommMaxFlr();
        }

        Short accumulationInterval = null;
        Short reportingInterval = null;
        Byte statsReporting = null;
        if (statsConfig != null) {
            accumulationInterval = statsConfig.getAccumulationInterval();
            reportingInterval = statsConfig.getReportingInterval();
            statsReporting = statsConfig.getStatsReporting();
        }

        Byte priority = null;
        if (priorityConfig != null) {
            priority = priorityConfig.getPriority();
        }

        if (nodeId == null || poaId == null || sessionId == null || sla == null
                || bwReqDl == null
                || (bwReqDlCir == null || bwReqDlCbs == null || bwReqDlEir == null || bwReqDlEbs == null || bwReqDlCommMaxLatency == null || bwReqDlCommMaxJitter == null || bwReqDlCommMaxFlr == null)
                || bwReqUl == null
                || (bwReqUlCir == null || bwReqUlCbs == null || bwReqUlEir == null || bwReqUlEbs == null || bwReqUlCommMaxLatency == null || bwReqUlCommMaxJitter == null || bwReqUlCommMaxFlr == null)
                || statsConfig == null
                || (accumulationInterval == null || reportingInterval == null || statsReporting == null)
                || priorityConfig == null
                || priority == null) {

            return false;
        }
        return true;
    }
}
