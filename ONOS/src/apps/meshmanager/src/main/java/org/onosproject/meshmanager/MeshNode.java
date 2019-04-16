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
import org.onosproject.meshmanager.api.message.CapabilityInfo;
import org.onosproject.meshmanager.api.message.MeshConstants;
import org.onosproject.meshmanager.api.message.MeshSBIMessage;
import org.onosproject.net.DeviceId;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class MeshNode {
    private final Logger logger = getLogger(getClass());

    private long ownBridgeMac;
    private int ownBridgeIPAddress;
    private long nodeId;
    private byte swVersion;
    private byte nbSectors;
    private byte nbConnectedEdges;

    private short signalingPortIndex;
    private short dataPortIndex;

    private byte nodeRole;

    private VlanInfo vlanInfo;
    private int reqSlaUl;
    private int reqSlaDl;

    private short[] nbHopsAway = new short[2]; //uplink, downlink

    public static final byte UNDEFINED = 0;
    public static final byte DISCONNECTED = 1;
    public static final byte CONNECTING = 2;
    public static final byte CONNECTED_NOT_CONFIRMED = 3;
    public static final byte CONNECTED_CONFIRMED_SINGLE_SECTOR = 4;
    public static final byte CONNECTED_CONFIRMED_COMPLETE = 5;
    private byte status;

    private DeviceId meshDeviceId;
    private DeviceId associatedDeviceId;
    private MeshNode gwNode;
    private Integer unsolicitedTid;
    private MeshSector tmpAssocMeshSect = null;

    private List<MeshSector> meshSectList = new ArrayList<>();
    private List<MeshFwdTblEntry> meshFwdTblEntryList = new ArrayList<>();
    private List<AccessNode> accessNodeList = new ArrayList<>();
    private HashMap<Integer, Byte> tidMap = new HashMap<>();

    /**
     * Constructor
     * @param nodeMac
     * @param gwNode
     * @param ipAddr
     * @param nodeId
     * @param vlanInfo
     * @param swVersion
     * @param nbSectors
     * @param sPortIndex
     * @param dPortIndex
     * @param status
     * @param deviceId
     * @param associatedDeviceId
     */
    public MeshNode(
            long nodeMac,
            MeshNode gwNode,
            int ipAddr,
            long nodeId,
            VlanInfo vlanInfo,
            byte swVersion,
            byte nbSectors,
            short sPortIndex,
            short dPortIndex,
            byte status,
            DeviceId deviceId,
            DeviceId associatedDeviceId) {
        this.ownBridgeMac = nodeMac;
        this.gwNode = gwNode;
        this.ownBridgeIPAddress = ipAddr;
        this.nodeId = nodeId;
        this.vlanInfo = vlanInfo;
        this.swVersion = swVersion;
        this.nbSectors = nbSectors;
        this.signalingPortIndex = sPortIndex;
        this.dataPortIndex = dPortIndex;
        this.nbConnectedEdges = 0;
        this.nbHopsAway[MeshConstants.UPLINK] = 0; //nb of hops away on the primary path
        this.nbHopsAway[MeshConstants.DOWNLINK] = 0; //nb of hops away on the primary path
        this.status = status;
        this.meshDeviceId = deviceId;
        this.associatedDeviceId = associatedDeviceId;
        this.unsolicitedTid = 0;

        reqSlaUl = 0;
        reqSlaDl = 0;
    }

    /**
     *
     * @return
     */
    public MeshNode getGwNode() {
        return gwNode;
    }

    /**
     *
     * @return
     */
    public DeviceId getMeshDeviceId() {
        return meshDeviceId;
    }

    /**
     *
     * @param deviceId
     */
    public void setMeshDeviceId(DeviceId deviceId) {
        meshDeviceId = deviceId;
    }

    /**
     *
     * @return
     */
    public DeviceId getAssociatedDeviceId() {
        return associatedDeviceId;
    }

    /**
     *
     * @param deviceId
     */
    public void setAssociatedDeviceId(DeviceId deviceId) {
        associatedDeviceId = deviceId;
    }

    /**
     * Number of hops away from the GW on the primary path
     * Used during association period to find out if we are one hop away
     * @param nbHops
     * @param direction
     */
    public void setNbHopsAway(short nbHops, short direction) {
        nbHopsAway[direction] =nbHops;
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
     * @return
     */
    public boolean isNbHopsAwayTooFar() {
        if (nbHopsAway[0] > MeshRoutingManager.MAX_HOPS ||
                nbHopsAway[1] > MeshRoutingManager.MAX_HOPS) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public long getOwnBridgeMac() {
        return ownBridgeMac;
    }

    /**
     *
     * @return
     */
    public short getSignalingPortIndex() {
        return signalingPortIndex;
    }

    /**
     *
     * @param index
     */
    public void setSignalingPortIndex(short index) {
        signalingPortIndex = index;
    }

    /**
     *
     * @return
     */
    public short getDataPortIndex() {
        return dataPortIndex;
    }

    /**
     *
     * @param index
     */
    public void setDataPortIndex(short index) {
        dataPortIndex = index;
    }

    /**
     *
     * @param tid
     * @return
     */
    public Byte addTid(int tid, byte type) {
        logger.debug("Adding tid: " + Integer.toHexString(tid) + " to node: " +
                             HexString.toHexString(nodeId));
        return tidMap.put(tid, type);
    }

    /**
     *
     * @param tid
     * @return
     */
    public Byte removeTid(int tid) {
        logger.debug("Removing tid: " + Integer.toHexString(tid) + " from node: " +
                             HexString.toHexString(nodeId));
        return tidMap.remove(tid);
    }

    /**
     *
     * @return
     */
    public String getAllTidString() {
        String allValues = " ";
        for (int tid : tidMap.keySet()) {
            allValues += Integer.toHexString(tid);
            allValues += " ";
        }
        return allValues;
    }

    /**
     *
     * @return
     */
    public long getNodeId() {
        return nodeId;
    }

    /**
     *
     * @return
     */
    public byte getNodeStatus() {
        return status;
    }

    /**
     *
     * @param status
     */
    public void setNodeStatus(byte status) {
        logger.debug("NODE: " + HexString.toHexString(nodeId) +
                             " STATUS: " + getStatusString(this.status) +
                             " --> " + getStatusString(status));
        this.status = status;
    }

    /**
     *
     * @return
     */
    public boolean isConnected() {
        return ((status == CONNECTED_NOT_CONFIRMED) ||
                (status == CONNECTED_CONFIRMED_COMPLETE) ||
                (status == CONNECTED_CONFIRMED_SINGLE_SECTOR));
    }

    /**
     *
     * @return
     */
    public boolean isConfirmed() {
        return ((status == CONNECTED_CONFIRMED_COMPLETE) ||
                (status == CONNECTED_CONFIRMED_SINGLE_SECTOR));
    }

    /**
     *
     * @return
     */
    public boolean isComplete() {
        return (status == CONNECTED_CONFIRMED_COMPLETE);
    }

    /**
     *
     * @return
     */
    public boolean isSectorsConfirmed() {
        for (MeshSector sector : meshSectList) {
            if (!sector.isConfirmed()) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return
     */
    public int getOwnBridgeIPAddress() {
        return ownBridgeIPAddress;
    }

    /**
     *
     * @param version
     */
    public void setVersion(byte version) {
        this.swVersion = version;
    }

    /**
     *
     * @return
     */
    public byte getVersion() {
        return swVersion;
    }

    /**
     *
     * @param nbSectors
     */
    public void setNbSectors(byte nbSectors) {
        this.nbSectors = nbSectors;
    }

    /**
     *
     * @return
     */
    public byte getNbSectors() {
        return nbSectors;
    }

    /**
     *
     * @return Node location
     */
    public String getNodeLocation() {
        // Sending stubbed response for now
        return "latitude=00.00000, longitude=00.00000, altitude=0.0";
    }

    /**
     *
     * @return
     */
    public List<MeshFwdTblEntry> getMeshFwdTblEntryList() {
        return meshFwdTblEntryList;
    }

    /**
     *
     * @param entry
     */
    public void addMeshFwdTblEntry(MeshFwdTblEntry entry) {
        logger.debug("Adding node: " + HexString.toHexString(ownBridgeMac) +
                             " entry: " + entry.toString());
        meshFwdTblEntryList.add(entry);
    }

    /**
     *
     * @param entry
     */
    public void delMeshFwdTblEntry(MeshFwdTblEntry entry) {
        logger.debug("Removing node: " + HexString.toHexString(ownBridgeMac) +
                             " entry: " + entry.toString());
        meshFwdTblEntryList.remove(entry);
    }

    /**
     *
     * @param vlanId
     * @return
     */
    public MeshFwdTblEntry getMeshFwdTblEntryByVlanId(short vlanId) {
        for (MeshFwdTblEntry entry : meshFwdTblEntryList) {
            if (entry.getVlanId() == vlanId) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Retrieve temporary association mesh sector.
     * @return Temporary association mesh sector
     */
    public MeshSector getTmpAssocMeshSect() {
        return tmpAssocMeshSect;
    }

    /**
     *
     * @param sector
     */
    public void setTmpAssocMeshSect(MeshSector sector) {
        tmpAssocMeshSect = sector;
    }

    /**
     *
     * @return
     */
    public List<MeshSector> getMeshSectList() {
        return meshSectList;
    }

    /**
     * Add a mesh sector
     * @param sector
     */
    public void addMeshSector(MeshSector sector) {
        meshSectList.add(sector);
    }

    /**
     * Add a mesh sector
     * @param sector
     */
    public void removeMeshSector(MeshSector sector) {
        meshSectList.remove(sector);
    }

    /**
     *
     * @param cap
     */
    public void addCapabilityInfo(CapabilityInfo cap) {
        this.nodeRole = cap.getNodeAttributes();
    }

    /**
     *
     * @return
     */
    public boolean isGwNodeRole() {
        return ((nodeRole & CapabilityInfo.NODEROLE_MASK) == CapabilityInfo.GW_NODEROLE);
    }

    /**
     * Returns a summary of the node data
     */
    public String toString() {

        String allTidString = getAllTidString();

        String str = "\n***** MESH NODE *****" +
                "\nownMac=" + HexString.toHexString(ownBridgeMac) +
                "\nownIp=" + HexString.toHexString(ownBridgeIPAddress) +
                "\nnodeId=" + HexString.toHexString(nodeId) +
                "\nvlanId/pcp=" + vlanInfo.getVlanIdUl() + "/" + vlanInfo.getPcpUl() +
                "," + vlanInfo.getVlanIdDl()  + "/" + vlanInfo.getPcpDl() +
                "\nversion=" + swVersion +
                "\nstatus=" + getStatusString(status) + " : " + status +
                "\nnbSectors=" + nbSectors +
                "\nnbHopsAwayOnPrimaryPath=" + nbHopsAway[MeshConstants.UPLINK] +
                ", " + nbHopsAway[MeshConstants.DOWNLINK] +
                "\nnbConnectedEdges=" + nbConnectedEdges +
                "\nsectorListSize=" + meshSectList.size() +
                "\nmeshFwdTblEntryListSize=" + meshFwdTblEntryList.size() +
                "\ncfgSetupTidList=" + allTidString +
                "\nreqSla=" + reqSlaUl + "," + reqSlaDl;

        if (meshDeviceId != null) {
            str += "\nmeshDeviceId=" + meshDeviceId.toString();
        } else {
            str += "\nmeshDeviceId=null";
        }

        if (associatedDeviceId != null) {
            str += "\nassociatedDeviceId=" + associatedDeviceId.toString();
        } else {
            str += "\nassociatedDeviceId=null";
        }

        if (gwNode != null) {
            str += "\ngwNode=" + HexString.toHexString(gwNode.getOwnBridgeMac());
        } else {
            str += "\ngwNode=null";
        }

        if (!accessNodeList.isEmpty()) {
            str += "\naccessNodeList:";
            for (AccessNode accessNode : accessNodeList) {
                str += accessNode.toString();
            }
        } else {
            str += "\naccessNodeList=<empty>";
        }

        str += "\n";
        return str;
    }

    /**
     * Returns a summary of the node data and its sublists
     */
    public String toString(int printAll) {
        String myString = toString();

        if (printAll == 1) {
            for (MeshSector sector : meshSectList) {
                myString += "\nmeshSectorList:";
                myString += sector.toString(printAll);
            }
            for (MeshFwdTblEntry entry : meshFwdTblEntryList) {
                myString += "\nmeshFwdTblEntryList:";
                myString += entry.toString();
            }
        }
        return myString;
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
     * @param status
     * @return
     */
    public String getStatusString(byte status) {
        String string = null;
        switch(status) {
            case UNDEFINED:
                string = "UNDEFINED";
                break;
            case CONNECTING:
                string = "CONNECTING";
                break;
            case CONNECTED_NOT_CONFIRMED:
                string = "CONNECTED_NOT_CONFIRMED";
                break;
            case CONNECTED_CONFIRMED_SINGLE_SECTOR:
                string = "CONNECTED_CONFIRMED_SINGLE_SECTOR";
                break;
            case CONNECTED_CONFIRMED_COMPLETE:
                string = "CONNECTED_CONFIRMED_COMPLETE";
                break;
            case DISCONNECTED:
                string = "DISCONNECTED";
                break;
            default:
        }
        return string;
    }

    /**
     *
     * @return
     */
    public List<AccessNode> getAccessNodeList() {
        return accessNodeList;
    }

    /**
     *
     * @param accessNode
     */
    public void removeAccessNode(AccessNode accessNode) {
        accessNode.setParentMeshNode(null);
        accessNodeList.remove(accessNode);
    }

    /**
     *
     * @param accessNode
     */
    public void addAccessNode(AccessNode accessNode) {
        logger.info("Adding Access Node: " + HexString.toHexString(accessNode.getOwnMac()) +
                            " in Mesh Node: " + HexString.toHexString(getOwnBridgeMac()));
        accessNodeList.add(accessNode);
        accessNode.setParentMeshNode(this);
    }

    /**
     * Increment by one and return the new transaction ID
     * @return new transaction ID
     */
    public Integer getNewTid() {
        unsolicitedTid++;
        return (unsolicitedTid | MeshSBIMessage.NC_TID_MASK);
    }

}

