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

public class LinkStats {
    private double throughputUl;
    private double throughputDl;
    private Long numDataBytesUl;
    private Long numDataBytesDl;
    private Long numGoodPktsUl;
    private Long numGoodPktsDl;
    private Long numBadPktsUl;
    private Long numBadPktsDl;
    private Long numPktsDroppedUl;
    private Long numPktsDroppedDl;
    private Integer latencyAvg;
    private Integer latencyMin;
    private Integer latencyMax;
    private Integer jitter;
    private Integer frameLossRatio;

    // Initialize all values as we are not able to fill all the stat fields in response
    public LinkStats() {
        this.throughputUl = 0.0d;
        this.throughputDl = 0.0d;
        this.numDataBytesUl = 0L;
        this.numDataBytesDl = 0L;
        this.numGoodPktsUl = 0L;
        this.numGoodPktsDl = 0L;
        this.numBadPktsUl = 0L;
        this.numBadPktsDl = 0L;
        this.numPktsDroppedUl = 0L;
        this.numPktsDroppedDl = 0L;
        this.latencyAvg = 0;
        this.latencyMin = 0;
        this.latencyMax = 0;
        this.jitter = 0;
        this.frameLossRatio = 0;
    }

    public Integer getFrameLossRatio() {
        return frameLossRatio;
    }

    public Integer getJitter() {
        return jitter;
    }

    public Integer getLatencyAvg() {
        return latencyAvg;
    }

    public Integer getLatencyMax() {
        return latencyMax;
    }

    public Integer getLatencyMin() {
        return latencyMin;
    }

    public Long getNumBadPktsDl() {
        return numBadPktsDl;
    }

    public Long getNumBadPktsUl() {
        return numBadPktsUl;
    }

    public Long getNumGoodPktsDl() {
        return numGoodPktsDl;
    }

    public Long getNumGoodPktsUl() {
        return numGoodPktsUl;
    }

    public Long getNumPktsDroppedDl() {
        return numPktsDroppedDl;
    }

    public Long getNumPktsDroppedUl() {
        return numPktsDroppedUl;
    }

    public Long getNumDataBytesDl() {
        return numDataBytesDl;
    }

    public Long getNumDataBytesUl() {
        return numDataBytesUl;
    }

    public double getThroughputDl() {
        return throughputDl;
    }

    public double getThroughputUl() {
        return throughputUl;
    }

    public void setFrameLossRatio(Integer frameLossRatio) {
        this.frameLossRatio = frameLossRatio;
    }

    public void setJitter(Integer jitter) {
        this.jitter = jitter;
    }

    public void setLatencyAvg(Integer latencyAvg) {
        if (latencyAvg < 0){
            throw new IllegalArgumentException("LinkStats: latencyAvg can not be negative value");
        }
        this.latencyAvg = latencyAvg;
    }

    public void setLatencyMax(Integer latencyMax) {
        if (latencyMax < 0) {
            throw new IllegalArgumentException("LinkStats: latencyMax can not be negative value");
        }
        this.latencyMax = latencyMax;
    }

    public void setLatencyMin(Integer latencyMin) {
        if (latencyMin < 0) {
            throw new IllegalArgumentException("LinkStats: latencyMin can not be negative value");
        }
        this.latencyMin = latencyMin;
    }

    public void setNumBadPktsDl(Long numBadPktsDl) {
        if (numBadPktsDl < 0) {
            throw new IllegalArgumentException("LinkStats: numBadPktsDl can not be negative value");
        }
        this.numBadPktsDl = numBadPktsDl;
    }

    public void setNumBadPktsUl(Long numBadPktsUl) {
        if (numBadPktsUl < 0) {
            throw new IllegalArgumentException("LinkStats: numBadPktsUl can not be negative value");
        }
        this.numBadPktsUl = numBadPktsUl;
    }

    public void setNumDataBytesDl(Long numDataBytesDl) {
        if (numDataBytesDl < 0) {
            throw new IllegalArgumentException("LinkStats: numDataBytesDl can not be negative value");
        }
        this.numDataBytesDl = numDataBytesDl;
    }

    public void setNumDataBytesUl(Long numDataBytesUl) {
        if (numDataBytesUl < 0) {
            throw new IllegalArgumentException("LinkStats: numDataBytesUl can not be negative value");
        }
        this.numDataBytesUl = numDataBytesUl;
    }

    public void setNumGoodPktsDl(Long numGoodPktsDl) {
        if (numGoodPktsDl < 0) {
            throw new IllegalArgumentException("LinkStats: numGoodPktsDl can not be negative value");
        }
        this.numGoodPktsDl = numGoodPktsDl;
    }

    public void setNumGoodPktsUl(Long numGoodPktsUl) {
        if (numGoodPktsUl < 0) {
            throw new IllegalArgumentException("LinkStats: numGoodPktsUl can not be negative value");
        }
        this.numGoodPktsUl = numGoodPktsUl;
    }

    public void setNumPktsDroppedDl(Long numPktsDroppedDl) {
        if (numPktsDroppedDl < 0) {
            throw new IllegalArgumentException("LinkStats: numPktsDroppedDl can not be negative value");
        }
        this.numPktsDroppedDl = numPktsDroppedDl;
    }

    public void setNumPktsDroppedUl(Long numPktsDroppedUl) {
        if (numPktsDroppedUl < 0) {
            throw new IllegalArgumentException("LinkStats: numPktsDroppedUl can not be negative value");
        }
        this.numPktsDroppedUl = numPktsDroppedUl;
    }

    public void setThroughputDl(double throughputDl) {
        if (throughputDl < 0) {
            throw new IllegalArgumentException("LinkStats: throughputDl can not be negative value");
        }
        this.throughputDl = throughputDl;
    }

    public void setThroughputUl(double throughputUl) {
        if (throughputUl < 0) {
            throw new IllegalArgumentException("LinkStats: throughputUl can not be negative value");
        }
        this.throughputUl = throughputUl;
    }

    @Override
    public String toString() {
        return  "linkStats [throughputUl="  +   throughputUl+ ", throughputDl=" + throughputDl
                + ", numDataBytesUl=" + numDataBytesUl+ ", numDataBytesDl=" + numDataBytesDl
                + ", numGoodPktsUl=" + numGoodPktsUl+ ", numGoodPktsDl=" + numGoodPktsDl
                + ", numBadPktsUl=" + numBadPktsUl+ ", numBadPktsDl=" + numBadPktsDl
                + ", numPktsDroppedUl=" +  numPktsDroppedUl+ ", numPktsDroppedDl="
                +  numPktsDroppedDl+ ", latencyAvg=" + latencyAvg+ ", latencyMin="
                + latencyMin+ ", latencyMax=" + latencyMax+ ", jitter=" + jitter
                + ", frameLossRatio=" + frameLossRatio+ "]";
    }
}
