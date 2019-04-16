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
public class BwReq {
    private Integer cir;
    private Integer cbs;
    private Short eir;
    private Integer ebs;
    private Integer commMaxLatency;
    private Integer commMaxJitter;
    private Short commMaxFlr;

    /**
     *
     * @return
     */
    public Integer getCbs() {
        return cbs;
    }

    /**
     *
     * @param cbs
     */
    public void setCbs(Integer cbs) {
        if (cbs != null && cbs < 0) {
            throw new IllegalArgumentException("BwReq: cbs should be non-negative.");
        }
        this.cbs = cbs;
    }

    /**
     *
     * @return
     */
    public Integer getCir() {
        return cir;
    }

    /**
     *
     * @param cir
     */
    public void setCir(Integer cir) {
        if (cir != null && (cir < -1 || cir > 65535)) {
            throw new IllegalArgumentException("BwReq: cir range should be within -1 and 65535.");
        }
        this.cir = cir;
    }

    /**
     *
     * @return
     */
    public Short getCommMaxFlr() {
        return commMaxFlr;
    }

    /**
     *
     * @param commMaxFlr
     */
    public void setCommMaxFlr(Short commMaxFlr) {
        if (commMaxFlr != null && commMaxFlr < 0) {
            throw new IllegalArgumentException("BwReq: commMaxFlr should be non-negative.");
        }
        this.commMaxFlr = commMaxFlr;
    }

    /**
     *
     * @return
     */
    public Integer getCommMaxJitter() {
        return commMaxJitter;
    }

    /**
     *
     * @param commMaxJitter
     */
    public void setCommMaxJitter(Integer commMaxJitter) {
        if (commMaxJitter != null && commMaxJitter < 0) {
            throw new IllegalArgumentException("BwReq: commMaxJitter should be non-negative.");
        }
        this.commMaxJitter = commMaxJitter;
    }

    /**
     *
     * @return
     */
    public Integer getCommMaxLatency() {
        return commMaxLatency;
    }

    /**
     *
     * @param commMaxLatency
     */
    public void setCommMaxLatency(Integer commMaxLatency) {
        if (commMaxLatency != null && commMaxLatency < 0) {
            throw new IllegalArgumentException("BwReq: commMaxLatency should be non-negative.");
        }
        this.commMaxLatency = commMaxLatency;
    }

    /**
     *
     * @return
     */
    public Integer getEbs() {
        return ebs;
    }

    /**
     *
     * @param ebs
     */
    public void setEbs(Integer ebs) {
        if (ebs != null && ebs < 0) {
            throw new IllegalArgumentException("BwReq: ebs should be non-negative.");
        }
        this.ebs = ebs;
    }

    /**
     *
     * @return
     */
    public Short getEir() {
        return eir;
    }

    /**
     *
     * @param eir
     */
    public void setEir(Short eir) {
        if (eir != null && eir < 0) {
            throw new IllegalArgumentException("BwReq: eir should be non-negative.");
        }
        this.eir = eir;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "BwReq [" +
                "cbs=" + cbs +
                ", cir=" + cir +
                ", commMaxFlr=" + commMaxFlr +
                ", commMaxJitter=" + commMaxJitter +
                ", commMaxLatency=" + commMaxLatency +
                ", ebs=" + ebs +
                ", eir=" + eir + "]";
    }
}
