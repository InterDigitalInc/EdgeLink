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

public class StatsReport {
    private Short sliceId;
    private Long timestampStart;
    private Long timestampStop;
    private LinkStats linkStats;

    public Short getSliceId() {
        return sliceId;
    }

    public Long getTimestampStart() {
        return timestampStart;
    }

    public Long getTimestampStop() {
        return timestampStop;
    }

    public LinkStats getLinkStats() {
        return linkStats;
    }

    public void setSliceId(Short sliceId) {
        this.sliceId = sliceId;
    }

    public void setTimestampStart(Long timestampStart) {
        this.timestampStart = timestampStart;
    }

    public void setTimestampStop(Long timestampStop) {
        this.timestampStop = timestampStop;
    }

    public void setLinkStats(LinkStats linkStats) {
        this.linkStats = linkStats;
    }

    @Override
    public String toString() {
        return "StatsReport [sliceId=" + sliceId + ", timestampStart=" + timestampStart + ", timestampStop=" + timestampStop + ", linkStats="
                + linkStats + "]";
    }
}
