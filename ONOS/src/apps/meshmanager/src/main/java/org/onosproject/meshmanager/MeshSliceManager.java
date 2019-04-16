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
import org.onosproject.meshmanager.api.message.MeshNCConfigSetup;
import org.onosproject.meshmanager.api.message.Sla;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class MeshSliceManager {
    private final Logger logger = getLogger(getClass());

    private MeshRoutingManager meshRoutingManager;
    private MeshDB meshDatabase;
    private HashMap<Long, List<AccessNode>> pendingAccessNodeListMap = new HashMap<>();


    /**
     * Constructor
     * @param meshRoutingManager
     * @param meshDatabase
     */
    public MeshSliceManager(MeshRoutingManager meshRoutingManager, MeshDB meshDatabase) {
        setConstructorPointers(meshRoutingManager, meshDatabase);
    }

    /**
     * Constructor
     * @param meshRoutingManager
     * @param meshDatabase
     */
    public void setConstructorPointers(MeshRoutingManager meshRoutingManager, MeshDB meshDatabase) {
        this.meshRoutingManager = meshRoutingManager;
        this.meshDatabase = meshDatabase;
    }

    /**
     *
     * @param mac
     * @param accessNodeList
     */
    public void addPendingAccessNodes(long mac, List<AccessNode> accessNodeList) {
        List<AccessNode> pendingAccessNodeList;

        // Make sure access node list is not empty
        if (accessNodeList.isEmpty()) {
            return;
        }

        // Retrieve Access Node list for provided mac. If none, create a new empty list.
        pendingAccessNodeList = pendingAccessNodeListMap.get(mac);
        if (pendingAccessNodeList == null) {
            pendingAccessNodeList = new ArrayList<>();
            pendingAccessNodeListMap.put(mac, pendingAccessNodeList);
        }

        // Add access nodes to pending list
        for (AccessNode accessNode : accessNodeList) {
            if (!pendingAccessNodeList.contains(accessNode)) {
                logger.trace("Adding accessNode: " + HexString.toHexString(accessNode.getOwnMac()) +
                                     " to the pending list for mac: " + HexString.toHexString(mac));
                pendingAccessNodeList.add(accessNode);
                accessNode.setParentMeshNode(null);
                accessNode.setSlaMet(false);
            }
        }
    }

    /**
     *
     * @param mac
     * @return List of removed access nodes; otherwise, empty list
     */
    public List<AccessNode> removePendingAccessNodes(long mac) {
        // Remove access node list, if present
        List<AccessNode> accessNodeList = pendingAccessNodeListMap.remove(mac);

        // Return removed pending access node list, if any; otherwise an empty list.
        return (accessNodeList != null) ? accessNodeList : new ArrayList<>();
    }

    /**
     *
     * @param debugLevel
     * @return
     */
    public String toString(byte debugLevel) {
        String myString = "\n\nSlice Map:";

        switch(debugLevel) {
            case MeshConstants.TRACE:
            case MeshConstants.DEBUG:
                if (logger.isDebugEnabled()) { //includes logger.TRACE
                    myString += "\n\tpending access nodes:";
                    for (List<AccessNode> accessNodeList : pendingAccessNodeListMap.values()) {
                        for (AccessNode accessNode : accessNodeList) {
                            myString += accessNode.toString();
                        }
                    }
                }
                break;
            case MeshConstants.INFO:
            case MeshConstants.WARNING:
            case MeshConstants.ERROR:
            default:
                break;
        }
        return myString;
    }

    /**
     *
     * @param accessNode
     */
    public void sendLatestSLA(AccessNode accessNode) {
        logger.trace("Send SLA IE for: " + HexString.toHexString(accessNode.getOwnMac()));
        MeshNode gwNode = meshDatabase.getGwNode();
        MeshNode leafNode = accessNode.getParentMeshNode();
        MeshNCConfigSetup slaCfgGw, slaCfgLeaf;

        if (leafNode != null) {
            // Build SLA_CFG for LEAF node
            slaCfgLeaf = new MeshNCConfigSetup();
            slaCfgLeaf.setBasics(leafNode.getVersion(), leafNode.getNodeId(), leafNode.getNewTid());
            slaCfgLeaf.encodeSlaIe(accessNode, MeshConstants.UPLINK);
            logger.trace("LEAF: " + slaCfgLeaf.toString());
            if (!slaCfgLeaf.isEmptyPayload()) {
                meshRoutingManager.sendMsgToSBI(leafNode.getMeshDeviceId(), slaCfgLeaf);
            }
        }

        // Build SLA_CFG for GW node
        slaCfgGw = new MeshNCConfigSetup();
        slaCfgGw.setBasics(gwNode.getVersion(), gwNode.getNodeId(), gwNode.getNewTid());
        slaCfgGw.encodeSlaIe(accessNode, MeshConstants.DOWNLINK);
        logger.trace("GW: " + slaCfgGw.toString());
        if (!slaCfgGw.isEmptyPayload()) {
            meshRoutingManager.sendMsgToSBI(gwNode.getMeshDeviceId(), slaCfgGw);
        }
    }

    /**
     *
     * @param session
     * @param vlanInfo
     * @param poaId
     * @param nodeId
     * @param sliceId
     * @return
     */
    public short createSlice(CustomerSession session, VlanInfo vlanInfo, long poaId, long nodeId,
                             AtomicInteger sliceId) {
        MeshNode meshNode;
        AccessNode accessNode;

        // Validate Mesh Node
        meshNode = meshDatabase.getNodeIdMacNodeMap(nodeId);
        if (meshNode == null) {
            logger.error("MAC of meshNode is unrecognized, exiting!");
            return MeshConstants.NC_NBI_INVALID_NODE_ID;
        }

        // Validate Access Node
        accessNode = meshDatabase.getNodeIdMacAccessNodeMap(poaId);
        if (accessNode != null) {
            logger.error("POA already allocated, we cannot recreate it, exiting!");
            return MeshConstants.NC_NBI_INVALID_POA_ID;
        }

        // Make sure we have enough available BW
        if ((meshRoutingManager.getMaxAvailableBW(meshNode, false) < vlanInfo.getReqSlaUl()) ||
                (meshRoutingManager.getMaxAvailableBW(meshNode, true) < vlanInfo.getReqSlaDl())) {
            return MeshConstants.NC_NBI_CANNOT_MEET_SLA;
        }

        // poaId is the mac address of the equipment, this is unique
        if (vlanInfo.getVlanIdUl() == 0) {
            vlanInfo.setVlanIdUl(meshRoutingManager.getAvailableVlanId());
        }
        if (vlanInfo.getVlanIdDl() == 0) {
            short vlanIdDl = meshRoutingManager.getAvailableVlanId();
            vlanInfo.setVlanIdDl(vlanIdDl);
            if (vlanInfo.getUniqueId() == 0) {
                vlanInfo.setUniqueId(vlanIdDl);
            }
        }

        // Create Access node
        accessNode = new AccessNode(poaId, meshNode.getOwnBridgeMac(), null, vlanInfo);

        // Allocate VLAN IDs
        if (!meshRoutingManager.allocateVlan(vlanInfo.getVlanIdUl())) {
            logger.error("Cannot allocate VLAN " + vlanInfo.getVlanIdUl() +
                                 " in uplink, it is already allocated");
            return MeshConstants.NC_NBI_INTERNAL_ERROR_VLAN_ALREADY_ALLOCATED;
        }
        if (!meshRoutingManager.allocateVlan(vlanInfo.getVlanIdDl())) {
            logger.error("Cannot allocate VLAN " + vlanInfo.getVlanIdDl() +
                                 " in downlink, it is already allocated");
            meshRoutingManager.deallocateVlan(vlanInfo.getVlanIdUl());
            return MeshConstants.NC_NBI_INTERNAL_ERROR_VLAN_ALREADY_ALLOCATED;
        }

        // Add slice
        if (!addSlice(accessNode, meshNode)) {
            meshRoutingManager.deallocateVlan(vlanInfo.getVlanIdUl());
            meshRoutingManager.deallocateVlan(vlanInfo.getVlanIdDl());
            return MeshConstants.NC_NBI_CANNOT_MEET_SLA;
        }

        // Slice successfully added
        session.addSlice(accessNode);
        logger.trace("New slice " +  HexString.toHexString(accessNode.getOwnMac()) + " added");
        // uniqueId is meant to be unique per meshNode but since it is using the vlanId,
        // it is unique per network
        sliceId.set(accessNode.getUniqueId());

        return MeshConstants.NC_NBI_OK;
    }

    /**
     *
     * @param session
     * @param sliceId
     * @param sla
     * @return
     */
    public short modifySlice(CustomerSession session, short sliceId, Sla sla) {
        AccessNode accessNode;

        // Create access node based on info provided
        accessNode = session.getSlice(sliceId);
        if (accessNode == null) {
            logger.error("MAC of accessNode is unrecognized, exiting!");
            return MeshConstants.NC_NBI_INVALID_SLICE_ID;
        }
        if (modifySlice(accessNode, sla)) {
            logger.trace("Modify slice " + HexString.toHexString(accessNode.getOwnMac()));
            return MeshConstants.NC_NBI_OK;
        }
        return MeshConstants.NC_NBI_CANNOT_MEET_SLA;
    }

    /**
     *
     * @param session
     * @param sliceId
     * @return
     */
    public short deleteSlice(CustomerSession session, short sliceId) {
        AccessNode accessNode;

        // Create access node based on info provided
        accessNode = session.getSlice(sliceId);
        if (accessNode == null) {
            logger.error("MAC of accessNode is unrecognized, exiting!");
            return MeshConstants.NC_NBI_INVALID_SLICE_ID;
        }
        if (!deleteSlice(accessNode)) {
            return MeshConstants.NC_NBI_INTERNAL_ERROR_MEMORY_ALLOCATION;
        } else {
            logger.trace("Remove slice " + HexString.toHexString(accessNode.getOwnMac()));
            session.removeSlice(accessNode);
        }
        return MeshConstants.NC_NBI_OK;
    }

    /**
     *
     * @param session
     * @param sliceIdList
     * @param accessNodeList
     * @return
     */
    public short getSlices(CustomerSession session, List<Short> sliceIdList,
                           List<AccessNode> accessNodeList) {
        AccessNode accessNode;

        if (sliceIdList.isEmpty()) {
            accessNodeList.addAll(session.getAllSlices());
        } else {
            if (sliceIdList.size() > 1) {
                for (short sliceId : sliceIdList) {
                    accessNode = session.getSlice(sliceId);
                    if (accessNode != null) {
                        accessNodeList.add(accessNode);
                    }
                    // else the slice does not belong to this customer, ignore
                }
            } else {
                accessNode = session.getSlice(sliceIdList.get(0));
                if(accessNode == null) {
                    return MeshConstants.NC_NBI_INVALID_SLICE_ID;
                } else {
                    accessNodeList.add(accessNode);
                }
            }
        }
        return MeshConstants.NC_NBI_OK;
    }

    /**
     *
     * @param accessNode
     * @param meshNode
     * @return
     */
    public boolean addSliceFromPendingAccessNode(AccessNode accessNode, MeshNode meshNode) {
        return addSlice(accessNode, meshNode);
    }

    /**
     *
     * @param accessNode
     * @param meshNode
     * @return
     */
    private boolean addSlice(AccessNode accessNode, MeshNode meshNode) {
        Long accessNodeMac = accessNode.getOwnMac();
        logger.trace("addSlice: " + HexString.toHexString(accessNodeMac));

        // Make sure mesh node is connected
        if (meshNode.getMeshDeviceId() == null) {
            logger.error("meshDeviceId == null, for: " +
                                 HexString.toHexString(meshNode.getOwnBridgeMac()) +
                                 ", ignoring the addSlice");
            return false;
        }

        // Add access node DB mapping
        meshDatabase.setNodeIdMacAccessNodeMap(accessNode.getOwnMac(), accessNode);

        // Add access node to provided mesh node list
        meshNode.addAccessNode(accessNode);

        // Update FWD TBL entries and send required OF & mesh messages
        meshRoutingManager.processFwdTblUpdates(meshNode, accessNode, true);

        // Send SLA to GW & LEAF nodes for access control
        sendLatestSLA(accessNode);

        return true;
    }

    /**
     *
     * @param accessNode
     * @param sla
     * @return
     */
    private boolean modifySlice(AccessNode accessNode, Sla sla) {
        logger.trace("modifySlice: " + HexString.toHexString(accessNode.getOwnMac()));

        // Retrieve associated mesh node
        MeshNode meshNode = accessNode.getParentMeshNode();
        if (meshNode == null) {
            logger.warn("meshNode == null");
            return false;
        }

        // Store old SLA requirements
        VlanInfo storedVlanInfo = new VlanInfo(accessNode.getVlanInfo());

        // Update Access node with latest SLA requirements
        accessNode.updateReqSla(sla.getPriorityConfig().getPriority(),
                                sla.getPriorityConfig().getPriority(),
                                sla.getBwReqUl().getCir(),
                                sla.getBwReqDl().getCir(),
                                sla.getBwReqUl().getCbs().shortValue(),
                                sla.getBwReqDl().getCbs().shortValue());

        // Update FWD TBL entries with new SLA requirements
        // NOTE: Do not send OF & MESH updates yet
        meshRoutingManager.processFwdTblUpdates(meshNode, accessNode, false);

        // If SLA not met, revert changes
        if (!accessNode.isSlaMet()) {
            // Restore previous SLA requirements
            accessNode.setVlanInfo(storedVlanInfo);

            // Update FWD TBL entries with new SLA requirements
            // NOTE: Must send updates here in case path has changed
            meshRoutingManager.processFwdTblUpdates(meshNode, accessNode, true);
            return false;
        }

        // SLA is met with new requirements, apply changes and send OF & Mesh updates.
        // Update all FWD TBL entries and send required OF & Mesh messages
        meshRoutingManager.processFwdTblUpdates();

        // Send SLA to GW & LEAF nodes for access control
        sendLatestSLA(accessNode);
        return true;
    }

    /**
     *
     * @param accessNode
     * @return
     */
    private boolean deleteSlice(AccessNode accessNode) {
        logger.trace("deleteSlice: " + HexString.toHexString(accessNode.getOwnMac()));

        // Send updated SLA with access node SLA requirements set to zero (delete)
        accessNode.updateReqSla((byte)0, (byte)0, 0, 0, (short)0, (short)0);
        sendLatestSLA(accessNode);

        // Retrieve associated mesh node, if any
        MeshNode meshNode = accessNode.getParentMeshNode();
        if (meshNode == null) {
            // Remove Access node from pending list
            pendingAccessNodeListMap.get(accessNode.getParentMeshNodeMac()).remove(accessNode);

        } else {
            // Remove Access node from routing manager
            meshRoutingManager.removeAccessNode(accessNode);

            // Remove access node from mesh node
            meshNode.removeAccessNode(accessNode);

            // Update all FWD TBL entries and send required OF & Mesh messages
            meshRoutingManager.processFwdTblUpdates();
        }

        // Remove access node from DB
        meshDatabase.setNodeIdMacAccessNodeMap(accessNode.getOwnMac(), null);

        return true;
    }

}

