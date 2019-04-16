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

import org.onlab.util.HexString;
import org.onosproject.meshmanager.api.message.MeshConstants;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class AccessNode {
    protected static final long MESH_SESSION_STATS_BIT0 = 0x01;
    protected static final long MESH_STATS_BIT1 = 0x02;
    protected static final long MESH_STATS_BIT2 = 0x04;
    protected static final long MESH_STATS_BIT3 = 0x08;
    protected static final long MESH_STATS_BIT4 = 0x10;
    protected static final long MESH_STATS_BIT5 = 0x20;
    protected static final long MESH_STATS_BIT6 = 0x40;
    protected static final long MESH_STATS_BIT7 = 0x80;
    protected static final long MESH_STATS_BIT8 = 0x100;

    private final Logger logger = getLogger(getClass());

    private short id;
    private long ownMac;
    private long parentMeshNodeMac;
    private MeshNode parentMeshNode;
    private VlanInfo vlanInfo;
    private boolean slaMet;
    private short[] nbHopsAway = new short[2]; //uplink, downlink

    // Set to true if a stat update needs to be generated
    // (i.e. the orchestrator will be notified of that change)
    private boolean statsUpdateNeeded;

    // Represent 64 flags that could be set (1 bit = 1 different stats represented by a flag)
    private long statsUpdateFlags;


    /**
     *
     * @param accessNodeMac
     * @param parentMeshNodeMac
     * @param parentMeshNode
     * @param vlanInfo
     */
    public AccessNode(
            long accessNodeMac,
            long parentMeshNodeMac,
            MeshNode parentMeshNode,
            VlanInfo vlanInfo) {
        this.ownMac = accessNodeMac;
        this.parentMeshNodeMac = parentMeshNodeMac;
        this.parentMeshNode = parentMeshNode;
        this.vlanInfo = vlanInfo;
        id = vlanInfo.getUniqueId(); //using the same as uniqueId in the TC right now
        slaMet = false;
        nbHopsAway[MeshConstants.UPLINK] = 0; //nb of hops away on the primary path
        nbHopsAway[MeshConstants.DOWNLINK] = 0; //nb of hops away on the primary path
        statsUpdateFlags = 0;
    }

    /**
     *
     * @return
     */
    public long getOwnMac() {
        return ownMac;
    }

    /**
     *
     * @return
     */
    public long getParentMeshNodeMac() {
        return parentMeshNodeMac;
    }

    /**
     *
     * @return
     */
    public short getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public long getStatsUpdateFlags() {
        return statsUpdateFlags;
    }

    /**
     *
     * @param bit
     */
    public void setStatsUpdateFlags(long bit) {
        statsUpdateFlags |= bit;
    }

    /**
     *
     */
    public void clearStatsUpdateFlags() {
        statsUpdateFlags = 0;
    }

    /**
     *
     * @return
     */
    public MeshNode getParentMeshNode() {
        return parentMeshNode;
    }

    /**
     *
     * @param node
     */
    public void setParentMeshNode(MeshNode node) {
        parentMeshNode = node;
    }

    /**
     *
     * @return
     */
    public VlanInfo getVlanInfo() {
       return vlanInfo;
    }

    /**
     *
     * @param vlanInfo
     */
    public void setVlanInfo(VlanInfo vlanInfo) {
        this.vlanInfo = vlanInfo;
    }

    /**
     *
     * @return
     */
    public short getVlanIdUl() {
        return vlanInfo.getVlanIdUl();
    }

    /**
     *
     * @return
     */
    public short getVlanIdDl() {
        return vlanInfo.getVlanIdDl();
    }

    /**
     *
     * @return
     */
    public byte getPcpUl() {
        return vlanInfo.getPcpUl();
    }

    /**
     *
     * @return
     */
    public byte getPcpDl() {
        return vlanInfo.getPcpDl();
    }

    /**
     *
     * @return
     */
    public int getReqSlaDl() {
        return vlanInfo.getReqSlaDl();
    }

    /**
     *
     * @return
     */
    public int getReqSlaUl() {
        return vlanInfo.getReqSlaUl();
    }

    /**
     *
     * @return
     */
    public short getCbsUl() {
        return vlanInfo.getCbsUl();
    }

    /**
     *
     * @return
     */
    public short getCbsDl() {
        return vlanInfo.getCbsDl();
    }

    /**
     *
     * @return
     */
    public short getUniqueId() {
        return vlanInfo.getUniqueId();
    }

    /**
     *
     * @param nbHops
     * @param direction
     */
    public void setNbHopsAway(short nbHops, short direction) {
        logger.trace("Mesh Node of Access Node: " + HexString.toHexString(ownMac) +
                             " is " + nbHops + " hop(s) away in " + direction + " direction");
        nbHopsAway[direction] = nbHops;
    }

    /**
     *
     * @param direction
     * @return
     */
    public short getNbHopsAway(short direction) {
        return nbHopsAway[direction];
    }

    /**
     *
     * @param slaMet
     */
    public void setSlaMet(boolean slaMet) {
        this.slaMet = slaMet;
    }

    /**
     *
     * @return
     */
    public boolean isSlaMet() {
        return slaMet;
    }

    /**
     *
     * @param pcpUl
     * @param pcpDl
     * @param reqSlaUl
     * @param reqSlaDl
     * @param cbsUl
     * @param cbsDl
     */
    public void updateReqSla(byte pcpUl, byte pcpDl, int reqSlaUl, int reqSlaDl, short cbsUl, short cbsDl) {
        vlanInfo.setPcp(MeshConstants.UPLINK, pcpUl);
        vlanInfo.setPcp(MeshConstants.DOWNLINK, pcpDl);
        vlanInfo.setReqSla(MeshConstants.UPLINK, reqSlaUl);
        vlanInfo.setReqSla(MeshConstants.DOWNLINK, reqSlaDl);
        vlanInfo.setCbs(MeshConstants.UPLINK, cbsUl);
        vlanInfo.setCbs(MeshConstants.DOWNLINK, cbsDl);
    }

    /**
     *
     * @return
     */
    public String toString() {
        String str = "\n\t***** ACCESS NODE *****" +
                "\n\tuniqueId=" + vlanInfo.getUniqueId() +
                "\n\townMac=" + HexString.toHexString(ownMac) +
                "\n\tparentMac(planned)=" + HexString.toHexString(parentMeshNodeMac);

        if (parentMeshNode != null) {
            str += "\n\tparentMac=" + HexString.toHexString(parentMeshNode.getOwnBridgeMac());
        } else {
            str += "\n\tparentMac=not assigned yet";
        }

        str += "\n\tvlan/pcp[UL,DL]=" + vlanInfo.getVlanIdUl() + "/" + vlanInfo.getPcpUl() +
                "," + vlanInfo.getVlanIdUl() + "/" + vlanInfo.getPcpDl() +
                "\n\treqSla[UL,DL]=" + vlanInfo.getReqSlaUl() + "," + vlanInfo.getReqSlaDl() +
                "\n\tcbs[UL,DL]=" + vlanInfo.getCbsUl() + "," + vlanInfo.getCbsDl() +
                "\n\tslaMet=" + slaMet + "\n";
        return str;
    }

}

