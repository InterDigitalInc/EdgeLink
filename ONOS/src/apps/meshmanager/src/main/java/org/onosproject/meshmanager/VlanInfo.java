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

package org.onosproject.meshmanager;

import org.onosproject.meshmanager.api.message.MeshConstants;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class VlanInfo {
    private final Logger logger = getLogger(getClass());

    private short vlanIdUl;
    private short vlanIdDl;
    private byte pcpUl;
    private byte pcpDl;
    private int reqSlaUl;
    private int reqSlaDl;
    private short cbsUl;
    private short cbsDl;
    private short uniqueId;

    /**
     *
     * @param vlanIdUl
     * @param pcpUl
     * @param vlanIdDl
     * @param pcpDl
     * @param reqSlaUl
     * @param reqSlaDl
     * @param cbsUl
     * @param cbsDl
     * @param uniqueId
     */
    public VlanInfo(short vlanIdUl, byte pcpUl, short vlanIdDl, byte pcpDl,
                    int reqSlaUl, int reqSlaDl, short cbsUl, short cbsDl, short uniqueId) {
        this.vlanIdUl = vlanIdUl;
        this.pcpUl = pcpUl;
        this.vlanIdDl = vlanIdDl;
        this.pcpDl = pcpDl;
        this.reqSlaUl = reqSlaUl;
        this.reqSlaDl = reqSlaDl;
        this.cbsUl = cbsUl;
        this.cbsDl = cbsDl;
        this.uniqueId = uniqueId;
    }

    /**
     * Copy constructor
     * @param vlanInfo
     */
    public VlanInfo(VlanInfo vlanInfo) {
        vlanIdUl = vlanInfo.vlanIdUl;
        pcpUl = vlanInfo.pcpUl;
        vlanIdDl = vlanInfo.vlanIdDl;
        pcpDl = vlanInfo.pcpDl;
        reqSlaUl = vlanInfo.reqSlaUl;
        reqSlaDl = vlanInfo.reqSlaDl;
        cbsUl = vlanInfo.cbsUl;
        cbsDl = vlanInfo.cbsDl;
        uniqueId = vlanInfo.uniqueId;
    }

    /**
     *
     * @return
     */
    public short getVlanIdUl() {
        return vlanIdUl;
    }

    /**
     *
     * @param vlanId
     */
    public void setVlanIdUl(short vlanId) {
        vlanIdUl = vlanId;
    }

    /**
     *
     * @return
     */
    public short getVlanIdDl() {
        return vlanIdDl;
    }

    /**
     *
     * @param vlanId
     */
    public void setVlanIdDl(short vlanId) {
        vlanIdDl = vlanId;
    }

    /**
     *
     * @param direction
     * @return
     */
    public short getVlanId(short direction) {
        if (direction == MeshConstants.DOWNLINK) {
            return vlanIdDl;
        } else {
            return vlanIdUl;
        }
    }

    /**
     *
     * @return
     */
    public byte getPcpUl() {
        return pcpUl;
    }

    /**
     *
     * @return
     */
    public byte getPcpDl() {
        return pcpDl;
    }

    /**
     *
     * @param direction
     * @return
     */
    public byte getPcp(short direction) {
        if (direction == MeshConstants.DOWNLINK) {
            return pcpDl;
        } else {
            return pcpUl;
        }
    }

    /**
     *
     * @param direction
     * @param pcp
     */
    public void setPcp(short direction, byte pcp) {
        if (direction == MeshConstants.DOWNLINK) {
            pcpDl = pcp;
        } else {
            pcpUl = pcp;
        }
    }

    /**
     *
     * @return
     */
    public int getReqSlaUl() {
        return reqSlaUl;
    }

    /**
     *
     * @return
     */
    public int getReqSlaDl() {
        return reqSlaDl;
    }

    /**
     *
     * @param direction
     * @return
     */
    public int getReqSla(short direction) {
        if (direction == MeshConstants.DOWNLINK) {
            return reqSlaDl;
        } else {
            return reqSlaUl;
        }
    }

    /**
     *
     * @param direction
     * @param reqSla
     */
    public void setReqSla(short direction, int reqSla) {
        if (direction == MeshConstants.DOWNLINK) {
            reqSlaDl = reqSla;
        } else {
            reqSlaUl = reqSla;
        }
    }

    /**
     *
     * @return
     */
    public short getCbsUl() {
        return cbsUl;
    }

    /**
     *
     * @return
     */
    public short getCbsDl() {
        return cbsDl;
    }

    /**
     *
     * @param direction
     * @param cbs
     */
    public void setCbs(short direction, short cbs) {
        if (direction == MeshConstants.DOWNLINK) {
            cbsDl = cbs;
        } else {
            cbsUl = cbs;
        }
    }

    /**
     *
     * @return
     */
    public short getUniqueId() {
        return uniqueId;
    }

    /**
     *
     * @param value
     */
    public void setUniqueId(short value) {
        uniqueId = value;
    }

    /**
     *
     * @return
     */
    public String toString() {
        String str =
            "\n******\nVlanInfo" +
            "\n\tvlan Id (UL-DL)=" + vlanIdUl + "/" + pcpUl + " - " + vlanIdDl + "/" + pcpDl +
            "\n\treqSla (UL-DL)=" + reqSlaUl + " - " + reqSlaDl +
            "\n\tunique Id=" + uniqueId;
        return str;
    }
}

