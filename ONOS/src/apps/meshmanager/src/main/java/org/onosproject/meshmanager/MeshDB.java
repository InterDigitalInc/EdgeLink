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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class MeshDB {
    private static final byte CONTROLLER_VERSION = 1;
    private static final String INTERNAL_CONTROLLER_VERSION = "MMW_V2_15_0_0";

    private final Logger logger = getLogger(getClass());

    private byte controllerVersion;
    private int meshFwdTblDlEntryIndex, meshFwdTblUlEntryIndex;
    private int accessNodeFwdTblDlEntryIndex, accessNodeFwdTblUlEntryIndex;
    private HashMap<Short, Integer> entryIdMap = new HashMap<>();
    private boolean[] entryIdArray = new boolean[MeshConstants.ENDING_NON_RESERVED_ACCESS_NODE_DL_ENTRY_ID + 1];

    private int lastResetId;
    private ConcurrentHashMap<Long, MeshNode> gwNodeMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, MeshNode> nodeIdMacNodeMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, AccessNode> nodeIdMacAccessNodeMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, MeshSector> sectorMacSectorMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Short, MeshNode> vlanIdNodeMap = new ConcurrentHashMap<>();

    private MeshManager meshManager;
    private List<MeshNode> meshNodeList = new ArrayList<>();
    private List<MeshNode> gwNodeList = new ArrayList<>();
    private List<SerializedMeshMessage> serializedAssocMsgList = new ArrayList<>();
    private List<SerializedMeshMessage> serializedFwdTblMsgList = new ArrayList<>();

    /**
     * Constructor
     * @param meshManager
     */
    public MeshDB(MeshManager meshManager) {
        this.meshManager = meshManager;
        this.lastResetId = -1;
        controllerVersion = CONTROLLER_VERSION;

        // Clear Maps
        nodeIdMacNodeMap.clear();
        nodeIdMacAccessNodeMap.clear();
        vlanIdNodeMap.clear();
        sectorMacSectorMap.clear();
        gwNodeMap.clear();

        // Clear gwNodeMacList (currently only one gwNodeMac, not a list)
        gwNodeList.clear();

        // Initialize entry ID Map & array
        entryIdMap.clear();
        meshFwdTblDlEntryIndex = MeshConstants.STARTING_NON_RESERVED_ENTRY_ID;
        meshFwdTblUlEntryIndex = MeshConstants.STARTING_RESERVED_GW_ENTRY_ID;
        accessNodeFwdTblDlEntryIndex = MeshConstants.STARTING_NON_RESERVED_ACCESS_NODE_DL_ENTRY_ID;
        accessNodeFwdTblUlEntryIndex = MeshConstants.STARTING_RESERVED_OTHER_ACCESS_NODE_UL_ENTRY_ID;
        for (int i = 0; i < entryIdArray.length; i++) {
            entryIdArray[i] = false;
        }
    }

    /**
     *
     * @return
     */
    public List<SerializedMeshMessage> getSerializedFwdTblMsgList() {
        return serializedFwdTblMsgList;
    }

    /**
     *
     * @param msg
     * @return
     */
    public boolean addSerializedFwdTblMsg(SerializedMeshMessage msg) {
        if (msg == null) {
            return false;
        }
        logger.trace("Adding to FWD TBL serialized message list: " + msg.toString());
        return serializedFwdTblMsgList.add(msg);
    }

    /**
     *
     * @param msg
     * @return
     */
    public boolean removeSerializedFwdTblMsg(SerializedMeshMessage msg) {
        return serializedFwdTblMsgList.remove(msg);
    }

    /**
     *
     * @return
     */
    public List<SerializedMeshMessage> getSerializedAssocMsgList() {
        return serializedAssocMsgList;
    }

    /**
     *
     * @param msg
     * @return
     */
    public boolean addSerializedAssocMsg(SerializedMeshMessage msg) {
        if (msg == null) {
            return false;
        }
        return serializedAssocMsgList.add(msg);
    }

    /**
     *
     * @param msg
     * @return
     */
    public boolean removeSerializedAssocMsg(SerializedMeshMessage msg) {
        return serializedAssocMsgList.remove(msg);
    }

    /**
     *
     * @param version
     * @return
     */
    public byte getLowestCommonVersion(byte version) {
        if (version <= controllerVersion) {
            return version;
        } else {
            return controllerVersion;
        }
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setNodeIdMacNodeMap(long key, MeshNode value) {
        if (value == null) {
            nodeIdMacNodeMap.remove(key);
        } else {
            nodeIdMacNodeMap.put(key, value);
        }
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setNodeIdMacAccessNodeMap(long key, AccessNode value) {
        if (value == null) {
            nodeIdMacAccessNodeMap.remove(key);
        } else {
            nodeIdMacAccessNodeMap.put(key, value);
        }
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setVlanIdNodeMap(short key, MeshNode value) {
        if (value == null) {
            vlanIdNodeMap.remove(key);
        } else {
            vlanIdNodeMap.put(key, value);
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public MeshNode getNodeIdMacNodeMap(long key) {
        return nodeIdMacNodeMap.get(key);
    }

    /**
     *
     * @param key
     * @return
     */
    public AccessNode getNodeIdMacAccessNodeMap(long key) {
        return nodeIdMacAccessNodeMap.get(key);
    }

    /**
     *
     * @return
     */
    public ConcurrentHashMap<Long, AccessNode> getNodeIdMacAccessNodeMap() {
        return nodeIdMacAccessNodeMap;
    }

    /**
     *
     * @param key
     * @return
     */
    public MeshNode getVlanIdNodeMap(short key) {
        return vlanIdNodeMap.get(key);
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setSectorMacSectorMap(long key, MeshSector value) {
        if (key != 0) {
            if (value == null) {
                sectorMacSectorMap.remove(key);
            } else {
                sectorMacSectorMap.put(key, value);
            }
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public MeshSector getSectorMacSectorMap(long key) {
        return sectorMacSectorMap.get(key);
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setGwNodeMap(long key, MeshNode value) {
        if (value == null) {
            gwNodeMap.remove(key);
        } else {
            gwNodeMap.put(key, value);
        }
    }

    /**
     *
     * @param gwNode
     */
    public void addGwNode(MeshNode gwNode) {
        if (!gwNodeList.contains(gwNode)) {
            gwNodeList.add(gwNode);
        }
    }

    /**
     *
     * @return
     */
    public MeshNode getGwNode() {
        // List of one only for now.. so return first item in the list if any
        MeshNode gwNode = null;
        if (!gwNodeList.isEmpty()) {
            gwNode = gwNodeList.get(0);
        }
        return gwNode;
    }

    /**
     *
     * @param resetId
     */
    public void setLastResetId(int resetId) {
        this.lastResetId = resetId;
    }

    /**
     *
     * @return
     */
    public int getLastResetId() {
        return lastResetId;
    }

    /**
     *
     * @return
     */
    public List<MeshNode> getMeshNodeList() {
        return meshNodeList;
    }

    /**
     *
     * @param meshNode
     */
    public void addMeshNode(MeshNode meshNode) {
        long nodeId = meshNode.getNodeId();
        long nodeMac = meshNode.getOwnBridgeMac();
        MeshNode gwNode = meshNode.getGwNode();

        // Add mesh node to DB
        meshNodeList.add(meshNode);

        // Add Node mappings
        setNodeIdMacNodeMap(nodeId, meshNode);
        setGwNodeMap(nodeId, gwNode);
        if (nodeId != nodeMac) {
            setNodeIdMacNodeMap(nodeMac, meshNode);
            setGwNodeMap(nodeMac, gwNode);
        }

        // Add VLAN ID mappings
        setVlanIdNodeMap(meshNode.getVlanIdDl(), meshNode);
        setVlanIdNodeMap(meshNode.getVlanIdUl(), meshNode);
    }

    /**
     * Remove mesh node & sector mappings & references
     * @param meshNode
     */
    public void removeMeshNode(MeshNode meshNode) {
        long nodeId = meshNode.getNodeId();
        long nodeMac = meshNode.getOwnBridgeMac();

        // Remove all mesh sectors from mesh node
        List<MeshSector> sectList = new ArrayList<>(meshNode.getMeshSectList());
        for (MeshSector meshSector : sectList) {
            removeMeshSector(meshNode, meshSector);
        }
        sectList.clear();

        // Remove VLAN ID mappings
        setVlanIdNodeMap(meshNode.getVlanIdDl(), null);
        setVlanIdNodeMap(meshNode.getVlanIdUl(), null);

        // Remove Node mappings
        if (nodeId != nodeMac) {
            setNodeIdMacNodeMap(nodeMac, null);
            setGwNodeMap(nodeMac, null);
        }
        setNodeIdMacNodeMap(nodeId, null);
        setGwNodeMap(nodeId, null);

        // Remove mesh node from DB
        meshNodeList.remove(meshNode);
    }

    /**
     *
     * @param meshNode
     * @param meshSector
     */
    public void addMeshSector(MeshNode meshNode, MeshSector meshSector) {
        // Add mesh sector to mesh node
        meshNode.addMeshSector(meshSector);

        // Add mesh sector mapping
        setSectorMacSectorMap(meshSector.getOwnSectMac(), meshSector);
    }

    /**
     *
     * @param meshNode
     * @param meshSector
     */
    public void removeMeshSector(MeshNode meshNode, MeshSector meshSector) {
        // Remove mesh sector mapping
        setSectorMacSectorMap(meshSector.getOwnSectMac(), null);

        // Remove mesh sector from mesh node
        meshNode.removeMeshSector(meshSector);
    }

    /**
     *
     * @param vlanId
     * @return
     */
    public long getBridgeMacFromVlan(short vlanId) {
        MeshNode meshNode = getVlanIdNodeMap(vlanId);
        if (meshNode == null) {
            return 0;
        }
        return meshNode.getOwnBridgeMac();
    }

    /**
     * Release BW reserved for provided Mesh/Access node MAC
     * @param mac Mesh or Access node MAC
     */
    public void releaseBW(long mac) {
        List<Short> vlanIdList = new ArrayList<>();
        logger.trace("releasing the bandwidth related to node: " + HexString.toHexString(mac));

        // Retrieve VLAN IDs for provided MAC
        MeshNode meshNode = getNodeIdMacNodeMap(mac);
        if (meshNode != null) {
            vlanIdList.add(meshNode.getVlanIdUl());
            vlanIdList.add(meshNode.getVlanIdDl());
        } else {
            AccessNode accessNode = getNodeIdMacAccessNodeMap(mac);
            if (accessNode != null) {
                vlanIdList.add(accessNode.getVlanIdUl());
                vlanIdList.add(accessNode.getVlanIdDl());
            } else {
                logger.error("Unable to find Mesh or Access node for " +
                                     HexString.toHexString(mac));
                return;
            }
        }

        // Release BW on all nodes & sectors for provided VLAN IDs
        for (MeshNode node : meshNodeList) {
            logger.trace("releasing the bandwidth on node " +
                                 HexString.toHexString(node.getOwnBridgeMac()));
            for (MeshSector sector : node.getMeshSectList()) {
                // checking !STA and not PCP since a sector that was PCP that
                // came down is not PCP anymore but has entries that need to
                // be cleared up
                if (sector.getSectorPersonality() != MeshSector.MESH_PERSONALITY_STA_MODE) {
                    sector.releaseBW(vlanIdList);
                }
            }
        }
    }

    /**
     * Release BW reserved for provided VLAN IDs
     * @param vlanIdList List of VLAN IDs to release BW
     */
    public void releaseBW(List<Short> vlanIdList) {
        for (MeshNode node : meshNodeList) {
            for (MeshSector sector : node.getMeshSectList()) {
                sector.releaseBW(vlanIdList);
            }
        }
    }

    /**
     * Release reserved BW for all VLAN IDs passing through provided mesh link
     * @param link
     */
    public void releaseBW(MeshLink link) {
        List<Short> vlanIdList = new ArrayList<>();

        // Get all VLAN IDs passing through link
        vlanIdList.addAll(link.getPrimReqSlaMap(MeshRoutingManager.STA_TO_PCP).keySet());
        vlanIdList.addAll(link.getPrimReqSlaMap(MeshRoutingManager.PCP_TO_STA).keySet());
        vlanIdList.addAll(link.getAltReqSlaMap(MeshRoutingManager.STA_TO_PCP).keySet());
        vlanIdList.addAll(link.getAltReqSlaMap(MeshRoutingManager.PCP_TO_STA).keySet());

        // Release BW for all VLAN IDs
        releaseBW(vlanIdList);
    }

    /**
     *
     * @param id
     */
    public void releaseEntryId(short id) {
        Integer entryId = entryIdMap.remove(id);
        if (entryId != null) {
            entryIdArray[entryId] = false;
        }
    }

    /**
     *
     * @param id
     * @param isRouteToGw
     * @param isData
     * @return
     */
    public short getEntryId(short id, boolean isRouteToGw, boolean isData) {
        Integer entryId;

        if (entryIdMap.containsKey(id)) {
            entryId = entryIdMap.get(id);
        } else {
            if (isRouteToGw) {
                if (isData) {
                    // UL DATA
                    entryId = accessNodeFwdTblUlEntryIndex;
                    accessNodeFwdTblUlEntryIndex = getNextAvailableEntryIndex(
                            MeshConstants.STARTING_UPLINK_DATA_ENTRY_ID,
                            MeshConstants.ENDING_UPLINK_DATA_ENTRY_ID,
                            accessNodeFwdTblUlEntryIndex);
                } else {
                    // UL SIGNALING
                    entryId = meshFwdTblUlEntryIndex;
                    meshFwdTblUlEntryIndex = getNextAvailableEntryIndex(
                            MeshConstants.STARTING_UPLINK_SIGNALING_ENTRY_ID,
                            MeshConstants.ENDING_UPLINK_SIGNALING_ENTRY_ID,
                            meshFwdTblUlEntryIndex);
                }
            } else {
                if (isData) {
                    // DL DATA
                    entryId = accessNodeFwdTblDlEntryIndex;
                    accessNodeFwdTblDlEntryIndex = getNextAvailableEntryIndex(
                            MeshConstants.STARTING_DOWNLINK_DATA_ENTRY_ID,
                            MeshConstants.ENDING_DOWNLINK_DATA_ENTRY_ID,
                            accessNodeFwdTblDlEntryIndex);
                } else {
                    // DL SIGNALING
                    entryId = meshFwdTblDlEntryIndex;
                    meshFwdTblDlEntryIndex = getNextAvailableEntryIndex(
                            MeshConstants.STARTING_DOWNLINK_SIGNALING_ENTRY_ID,
                            MeshConstants.ENDING_DOWNLINK_SIGNALING_ENTRY_ID,
                            meshFwdTblDlEntryIndex);
                }
            }
            entryIdMap.put(id, entryId);
        }

        entryIdArray[entryId] = true;
        return (short)entryId.intValue();
    }

    /***********
     * This function dumps all the database information regarding the topology and the
     * different mapping objects used throughout. It uses the different levels the logger object
     * is using. It prints based on the logger level (i.e. if logger is set at TRACE level
     * but the parameter passed on is DEBUG, it won't print anything).
     *
     * TRACE/DEBUG : print all the node/sector/fwdRules DB + all the mappings
     * INFO/WARNING/ERROR : prints only the mappings and the number of nodes in the DB
     * @param debugLevel
     * values: TRACE/DEBUG/INFO/WARNING/ERROR
     */
    public void dumpDB(byte debugLevel) {
        String myString = null;

        if (logger.isDebugEnabled()) {
            for (MeshNode node : getMeshNodeList()) {
                if (myString == null) {
                    myString = node.toString(1);
                } else {
                    myString = myString + node.toString(1);
                }
            }
        }
        switch (debugLevel) {
            case MeshConstants.TRACE:
            case MeshConstants.DEBUG:
                if (logger.isDebugEnabled()) { //includes logger.TRACE
                    logger.debug("dumpDB (" + getMeshNodeList().size() + " nodes):\n" + myString);
                    logger.debug("\n\nGlobal Mappings:" +
                                         "\n\tgwNodeMap:" + printGwNodeMap() +
                                         "\n\tnodeIdMacNodeMap: " + printNodeIdMacNodeMap() +
                                         "\n\tsectorMacSectorMap: " + printSectorMacSectorMap() +
                                         "\n\tnodeIdMacAccessNodeMap: " + printNodeIdMacAccessNodeMap() +
                                         "\n\tvlanIdNodeMap: " + printVlanIdNodeMap() +
                                         "\n\tserializedMsg: " + printSerializedFwdTblMsgList());
                    //MeshRouting DB is printed at TRACE level only
                    meshManager.getMeshRoutingManager().toString(MeshConstants.TRACE);
                    //getMeshRoutingManager().printGraph(MeshConstants.TRACE);
                    logger.debug(meshManager.getMeshSliceManager().toString(MeshConstants.TRACE));
                }
                break;
            case MeshConstants.INFO:
            case MeshConstants.WARNING:
            case MeshConstants.ERROR:
                logger.debug("dumpDB (" + getMeshNodeList().size() + " nodes):\n");
                logger.debug("\n\nGlobal Mappings:" +
                                     "\n\tgwNodeMap:" + printGwNodeMap() +
                                     "\n\tnodeIdMacNodeMap: " + printNodeIdMacNodeMap() +
                                     "\n\tsectorMacSectorMap: " + printSectorMacSectorMap() +
                                     "\n\tnodeIdMacAccessNodeMap: " + printNodeIdMacAccessNodeMap() +
                                     "\n\tvlanIdNodeMap: " + printVlanIdNodeMap() +
                                     "\n\tserializedMsg: " + printSerializedFwdTblMsgList());
                break;
            default:
                break;
        }
    }

    /**
     *
     * @return
     */
    public String printDB() {
        String myString = "";
        String myFullString;

        for (MeshNode node : getMeshNodeList()) {
            myString += node.toString(1);
        }

        myFullString = "printDB (" + getMeshNodeList().size() + " nodes):\n" + myString;
        myFullString += "\n\nGlobal Mappings:" +
                "\n\tgwNodeMap:" + printGwNodeMap() +
                "\n\tnodeIdMacNodeMap: " + printNodeIdMacNodeMap() +
                "\n\tsectorMacSectorMap: " + printSectorMacSectorMap() +
                "\n\tnodeIdMacAccessNodeMap: " + printNodeIdMacAccessNodeMap() +
                "\n\tvlanIdNodeMap: " + printVlanIdNodeMap() +
                "\n\tserializedMsg: " + printSerializedFwdTblMsgList();
        //MeshRouting DB is printed at TRACE level only
        //getMeshRoutingManager().printGraph(MeshConstants.TRACE);
        //logger.debug(getMeshSliceManager().toString(MeshConstants.TRACE));
        return myFullString;
    }

    /**
     *
     * @return
     */
    private String printSerializedFwdTblMsgList() {
        String myString = "";
        for (SerializedMeshMessage msg : serializedFwdTblMsgList) {
            myString += "\n*****\n";
            myString += msg.toString();
            myString += "\n";
        }
        myString += "\n";
        return myString;
    }

    /**
     *
     * @return
     */
    private String printVlanIdNodeMap() {
        String myString = "";
        for (Entry<Short, MeshNode>entry : vlanIdNodeMap.entrySet()) {
            myString += "\n\tvlan[" + entry.getKey() + "] meshNodeMac[" +
                    HexString.toHexString(entry.getValue().getOwnBridgeMac()) + "]";
        }
        myString += "\n";
        return myString;
    }

    /**
     *
     * @return
     */
    private String printNodeIdMacNodeMap() {
        String myString = "";
        for (Entry<Long, MeshNode> entry : nodeIdMacNodeMap.entrySet()) {
            myString += "\n\tkey[" + HexString.toHexString(entry.getKey()) + "] nodeMac[" +
                    HexString.toHexString(entry.getValue().getOwnBridgeMac()) + "]";
        }
        myString += "\n";
        return myString;
    }

    /**
     *
     * @return
     */
    private String printNodeIdMacAccessNodeMap() {
        String myString = "";
        for (Entry<Long, AccessNode> entry : nodeIdMacAccessNodeMap.entrySet()) {
            myString += "\n\tkey[" + HexString.toHexString(entry.getKey()) + "] accessNodeMac[" +
                    HexString.toHexString(entry.getValue().getOwnMac()) + "]";
        }
        myString += "\n";
        return myString;
    }

    /**
     *
     * @return
     */
    private String printSectorMacSectorMap() {
        String myString = "";
        for(Entry<Long, MeshSector> entry : sectorMacSectorMap.entrySet()) {
            myString += "\n\tkey[" + HexString.toHexString(entry.getKey()) + "] sectorMac[" +
                    HexString.toHexString(entry.getValue().getOwnSectMac()) + "]";
        }
        myString += "\n";
        return myString;
    }

    /**
     *
     * @return
     */
    private String printGwNodeMap() {
        String myString = "";
        for (Entry<Long, MeshNode> entry : gwNodeMap.entrySet()) {
            myString += "\n\tkey[" + HexString.toHexString(entry.getKey()) + "] gwNodeMac[" +
                    HexString.toHexString(entry.getValue().getOwnBridgeMac()) + "]";
        }
        myString += "\n";
        return myString;
    }

    /**
     *
     * @param lowerLimit
     * @param upperLimit
     * @param entryIndex
     * @return
     */
    private int getNextAvailableEntryIndex(int lowerLimit, int upperLimit, int entryIndex) {
        int firstPassEntryIndex = entryIndex;
        do {
            // TODO -- handle wrapping properly
            if (upperLimit == entryIndex) {
                entryIndex = lowerLimit;
            } else {
                entryIndex++;
            }
            if (entryIndex == firstPassEntryIndex) {
                entryIndex = -1;
                break;
            }
        } while (entryIdArray[entryIndex]);
        return entryIndex;
    }

}
