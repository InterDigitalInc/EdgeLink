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
import org.onosproject.net.ConnectPoint;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class MeshSector {
    private final Logger logger = getLogger(getClass());

    private MeshNode parentNode;
    private MeshRoutingManager meshRoutingManager;
    private Long ownSectMac;
    private short sectorIndex;
    private int usedBW = 0;

    private List<NeighborNode> neighborNodeList = new ArrayList<>();
    private List<NeighborNode> potentialNeighborNodeList = new ArrayList<>();
    private List<MeshLink> meshLinkList = new ArrayList<>();
    private List<HashMap<Short, Integer>> usedBWMap = new ArrayList<>();

    public static final byte STATE_UNCONFIRMED_NO_ASSOC = 0x00;
    public static final byte STATE_UNCONFIRMED_ASSOC = 0x01;
    public static final byte STATE_CONFIRMED_NO_ASSOC = 0x02;
    public static final byte STATE_CONFIRMED_ASSOC = 0x03;
    private byte state;

    public static final byte MESH_PERSONALITY_STA_MODE = 0x00;
    public static final byte MESH_PERSONALITY_PCP_MODE = 0x01;
    private byte sectorPersonality;

    private ConnectPoint connectPoint; //one-to-one mapping for using the topology;

    /**
     * Constructor
     * @param mac
     * @param parent
     * @param rm
     */
    public MeshSector(Long mac, short index, MeshNode parent, MeshRoutingManager rm) {
        ownSectMac = mac;
        sectorIndex = index;
        parentNode = parent;
        meshRoutingManager = rm;
        sectorPersonality = MESH_PERSONALITY_STA_MODE;
        state = STATE_UNCONFIRMED_NO_ASSOC;
        usedBW = 0;

        // Create STA to PCP & PCP to STA BW usage maps
        usedBWMap.add(new HashMap<>());
        usedBWMap.add(new HashMap<>());
    }

    /**
     *
     * @return
     */
    public Long getOwnSectMac() {
        return ownSectMac;
    }

    /**
     *
     * @return
     */
    public MeshNode getParentNode() {
        return parentNode;
    }

    /**
     *
     * @return
     */
    public int getUsedBW() {
        return usedBW;
    }

    /**
     *
     * @return
     */
    public ConnectPoint getConnectPoint() {
        return connectPoint;
    }

    /**
     *
     * @param connectPoint
     */
    public void setConnectPoint(ConnectPoint connectPoint) {
        logger.trace("setConnect : " + ((connectPoint == null) ? "null" : connectPoint.toString()));
        this.connectPoint = connectPoint;
    }

    /**
     *
     * @return
     */
    public List<MeshLink> getMeshLinkList() {
        return meshLinkList;
    }

    /**
     *
     * @param peerMac
     * @return
     */
    public MeshLink getMeshLink(long peerMac) {
        for (MeshLink link : meshLinkList) {
            if (link.getPeerMac() == peerMac) {
                return link;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public NeighborNode getFirstNeighborNode() {
        if (neighborNodeList.isEmpty()) {
            return null;
        }
        return neighborNodeList.get(0);
    }

    /**
     *
     * @param peerMac
     * @return
     */
    public NeighborNode getNeighborNode(Long peerMac) {
        NeighborNode neighNode = null;
        for (NeighborNode node: neighborNodeList) {
            if (node.getPeerSectMac().equals(peerMac)) {
                neighNode = node;
            }
        }
        return neighNode;
    }

    /**
     *
     * @return
     */
    public List<NeighborNode> getNeighborNodeList() {
        return neighborNodeList;
    }

    /**
     *
     * @return
     */
    public List<NeighborNode> getPotentialNeighborNodeList() {
        return potentialNeighborNodeList;
    }

    /**
     *
     * @param node
     */
    public void addPotentialNeighbor(NeighborNode node) {
        potentialNeighborNodeList.add(node);
    }

    /**
     *
     * @param node
     */
    public void removePotentialNeighbor(NeighborNode node) {
        potentialNeighborNodeList.remove(node);
    }

    /**
     *
     */
    public void clearPotentialNeighborList() {
        potentialNeighborNodeList.clear();
    }

    /**
     *
     * @param node
     * @return
     */
    public boolean addNeighbor(NeighborNode node) {
        // Check if node already in list
        for (NeighborNode currentNeighborNode : neighborNodeList) {
            if ((currentNeighborNode.getOwnSector().getOwnSectMac().equals(
                    node.getOwnSector().getOwnSectMac())) &&
                    (currentNeighborNode.getPeerSectMac().equals(node.getPeerSectMac()))) {
                return false;
            }
        }

        // Add node to list & set state
        neighborNodeList.add(node);
        setSectorState(STATE_UNCONFIRMED_ASSOC);
        return true;
    }

    /**
     *
     * @param node
     */
    public void removeNeighbor(NeighborNode node) {
        neighborNodeList.remove(node);

        // State of the sector should be reset too, if necessary
        if (neighborNodeList.isEmpty()) {
            if ((state == STATE_CONFIRMED_ASSOC) &&
                    (sectorPersonality == MESH_PERSONALITY_PCP_MODE)) {
                setSectorState(STATE_CONFIRMED_NO_ASSOC);
            } else {
                setSectorState(STATE_UNCONFIRMED_NO_ASSOC);
            }
        }
    }

    /**
     *
     * @param state
     */
    public void setSectorState(byte state) {
        logger.debug("SECTOR: " + HexString.toHexString(ownSectMac) +
                             " STATE: " + getStateString(this.state) +
                             " --> " + getStateString(state));
        this.state = state;
    }

    /**
     *
     * @return
     */
    public boolean isConfirmed() {
        return ((state == STATE_CONFIRMED_NO_ASSOC) || (state == STATE_CONFIRMED_ASSOC));
    }

    /**
     *
     * @param link
     * @param dir
     * @return
     */
    private long getLinkAvailBW(MeshLink link, byte dir) {
        return meshRoutingManager.getAvailBW(usedBW, link.getMCS(dir));
    }

    /**
     * Refresh the link metrics for each link
     */
    private void refreshLinkMetrics() {
        // For each link on sector, refresh the available BW
        for (MeshLink link : meshLinkList) {
            // PCP TO STA
            meshRoutingManager.setLinkMetric(
                    link.getOwnMac(), link.getPeerMac(), MeshRoutingManager.LINK_COST_METRIC_BW,
                    meshRoutingManager.getAvailBW(usedBW, link.getMCS(MeshRoutingManager.PCP_TO_STA)));

            // STA TO PCP
            meshRoutingManager.setLinkMetric(
                    link.getPeerMac(), link.getOwnMac(), MeshRoutingManager.LINK_COST_METRIC_BW,
                    meshRoutingManager.getAvailBW(usedBW, link.getMCS(MeshRoutingManager.STA_TO_PCP)));
        }
    }

    /**
     * Add BW to sector Map & sector BW usage.
     * NOTE: BW is represented as a fraction of MAX LINK BW
     * @param vlanId
     * @param bw
     * @param dir
     */
    private void addBW(short vlanId, int bw, byte dir) {
        Integer val = usedBWMap.get(dir).get(vlanId);
        if (val == null) {
            usedBWMap.get(dir).put(vlanId, bw);
            usedBW += bw;
            logger.trace("Set usedBWMap[{}][vlanId({})] to [{}]", dir, vlanId, bw);
        } else if (bw > val) {
            usedBWMap.get(dir).put(vlanId, bw);
            usedBW += (bw - val);
            logger.trace("Set usedBWMap[{}][vlanId({})] to [{}]", dir, vlanId, bw);
        }
        logger.debug("Sector[" + HexString.toHexString(ownSectMac) + "] usedBW[" + usedBW + "/1000000]");
    }

    /**
     * Remove BW from sector Map & sector BW usage.
     * NOTE: BW is represented as a fraction of MAX LINK BW
     * @param vlanId
     * @param dir
     */
    private void removeBW(short vlanId, byte dir) {
        Integer val = usedBWMap.get(dir).remove(vlanId);
        if (val != null) {
            usedBW -= val;
            logger.trace("Removed usedBWMap[{}][vlanId({})]", dir, vlanId);
        }
        logger.debug("Sector[" + HexString.toHexString(ownSectMac) + "] usedBW[" + usedBW + "/1000000]");
    }

    /**
     * Recalculate BW usage for provided VLAN ID & direction
     * @param vlanId
     * @param dir
     */
    private void refreshBW(short vlanId, byte dir) {
        Integer reqSla;

        // Remove BW usage for provided VLAN ID
        removeBW(vlanId, dir);

        // Recalculate BW usage for provided vlanID
        for (MeshLink link : meshLinkList) {
            reqSla = link.getPrimReqSlaMap(dir).get(vlanId);
            if (reqSla == null) {
                reqSla = link.getAltReqSlaMap(dir).get(vlanId);
            }

            if (reqSla != null) {
                addBW(vlanId, meshRoutingManager.getBWUsage(reqSla, link.getMCS(dir)), dir);
            }
        }
    }

    /**
     * Remove BW from sector Map & sector BW usage for provided VLAN IDs
     * @param vlanIdList
     */
    public void releaseBW(List<Short> vlanIdList) {
        for (Short vlanId : vlanIdList) {
            logger.trace("releaseBWRequirements for vlanId={} on sector={}",
                         vlanId, HexString.toHexString(ownSectMac));

            // Remove reqSla form all LinkAttributes lists
            // NOTE: Only 1 possible reqSla per entry
            for (MeshLink link : meshLinkList) {
                if (link.removeReqSla(vlanId, MeshRoutingManager.STA_TO_PCP, true)) {
                    continue;
                }
                if (link.removeReqSla(vlanId, MeshRoutingManager.STA_TO_PCP, false)) {
                    continue;
                }
                if (link.removeReqSla(vlanId, MeshRoutingManager.PCP_TO_STA, true)) {
                    continue;
                }
                if (link.removeReqSla(vlanId, MeshRoutingManager.PCP_TO_STA, false)) {
                    continue;
                }
            }

            // Remove BW from Sector list
            removeBW(vlanId, MeshRoutingManager.STA_TO_PCP);
            removeBW(vlanId, MeshRoutingManager.PCP_TO_STA);
        }

        // Refresh Link Metrics
        refreshLinkMetrics();
    }

    /**
     * Update Mesh Link attributes.
     * Add reqSla, update sector BW usage & update link metrics.
     * @param vlanId
     * @param peerMac
     * @param reqSla
     * @param dir
     * @param isPrimPath
     * @return
     */
    public boolean updateLinkAttributes(
            short vlanId, long peerMac, int reqSla, byte dir, boolean isPrimPath) {

        logger.trace("Updating link attributes: sector[" + HexString.toHexString(ownSectMac) +
                             "] peerMac[" + HexString.toHexString(peerMac) +
                             "] vlanId[" + vlanId +
                             "] direction[" + dir +
                             "] isPrimPath[" + isPrimPath +
                             "] reqSla[" + reqSla + "]");

        // Find link to update
        for (MeshLink link : meshLinkList) {
            if (link.getPeerMac() == peerMac) {
                // Add reqSla to link attributes list
                if (!link.addReqSla(vlanId, reqSla, dir, isPrimPath)) {
                    throw new AssertionError("Failed to add reqSla to LinkAttributes");
                }

                // Add sector BW calculated from reqSla and link MCS
                addBW(vlanId, meshRoutingManager.getBWUsage(reqSla, link.getMCS(dir)), dir);

                refreshLinkMetrics();
                return true;
            }
        }
        logger.trace("Failed to find link");
        return false;
    }

    /**
     * Update Mesh Link MCS for provided direction.
     * Update sector BW usage & update link metrics.
     * @param peerMac
     * @param mcs
     * @param dir
     * @return
     */
    public boolean updateLinkMcs(long peerMac, byte mcs, byte dir) {
        logger.trace("Setting MCS to " + mcs + " for " + HexString.toHexString(peerMac) +
                             " on " + HexString.toHexString(ownSectMac) + " direction: " + dir);

        // Retrieve attributes table entry for provided peer MAC address
        for (MeshLink link : meshLinkList) {
            if (link.getPeerMac() == peerMac) {
                // Update MCS in attributes table entry, if different
                if (link.getMCS(dir) != mcs) {
                    logger.info("updateLinkMcs:" +
                                        " newMCS=" + mcs +
                                        " oldMCS=" + link.getMCS(dir) +
                                        " sector=" + HexString.toHexString(ownSectMac) +
                                        " peer=" + HexString.toHexString(peerMac) +
                                        " dir=" + dir);

                    // Update entry with new link MCS
                    link.updateMCS(mcs, dir);

                    // Refresh impacted nodes BW usage based on new MCS
                    // NOTE: Impacted nodes are for primary and backup paths
                    for (Short vlanId : link.getPrimReqSlaMap(dir).keySet()) {
                        refreshBW(vlanId, dir);
                    }
                    for (Short vlanId : link.getAltReqSlaMap(dir).keySet()) {
                        refreshBW(vlanId, dir);
                    }

                    refreshLinkMetrics();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Create a new Mesh Link
     * @param peerMac
     */
    public void createMeshLink(long peerMac) {
        logger.trace("createMeshLink on " + HexString.toHexString(ownSectMac) +
                             " for " + HexString.toHexString(peerMac));
        MeshLink link = new MeshLink(peerMac, ownSectMac);

        // Adding the link no matter what, because it may become valid based on changing network
        // metrics (at which point, it will be removed from the prune list)
        if (!addMeshLink(link)) {
            logger.trace("Link already existed, no need to add");
        }
    }

    /**
     * Add new link (if not already present in list)
     * @param newLink
     * @return
     */
    private boolean addMeshLink(MeshLink newLink) {
        for (MeshLink link : meshLinkList) {
            if (link.getPeerMac() == newLink.getPeerMac()) {
                return false;
            }
        }
        meshLinkList.add(newLink);
        return true;
    }

    /**
     *
     * @param link
     * @return
     */
    public boolean removeMeshLink(MeshLink link) {
        return meshLinkList.remove(link);
    }

    /**
     * Remove link matching requested peer MAC, if present
     * @param peerMac
     * @return
     */
    public boolean removeMeshLink(long peerMac) {
        Iterator<MeshLink> iterator = meshLinkList.iterator();
        while (iterator.hasNext()) {
            MeshLink link = iterator.next();
            if (link.getPeerMac() == peerMac) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param personality
     */
    public void setSectorPersonality(byte personality) {
        sectorPersonality = personality;
    }

    /**
     *
     * @return
     */
    public byte getSectorPersonality() {
        return sectorPersonality;
    }

    /**
     *
     * @param index
     */
    public void setSectorIndex(short index) {
        sectorIndex = index;
    }

    /**
     *
     * @return
     */
    public short getSectorIndex() {
        return sectorIndex;
    }

    /**
     * Returns a summary of the sector data
     */
    public String toString() {
        String str = "\n\t***** MESH SECTOR *****" +
                "\n\townMac=" + HexString.toHexString(ownSectMac) +
                "\n\tparentId=" + HexString.toHexString(parentNode.getNodeId()) +
                "\n\tsectorPersonality=" + getSectorPersonalityString(sectorPersonality) +
                "\n\tstate=" + getStateString(state) + " : " + state +
                "\n\tsectorIndex=" + sectorIndex +
                "\n\tneighborNodeList size=" + neighborNodeList.size() +
                "\n\tpotentialNeighborNodeList size=" + potentialNeighborNodeList.size() +
                "\n\tmeshLinkList size=" + meshLinkList.size() +
                "\n\tusedBW=" + usedBW;

        if (connectPoint != null) {
            str += "\n\tconnectPoint=" + connectPoint.toString();
        } else {
            str += "\n\tconnectPoint= null";
        }
        str += "\n";
        return str;
    }

    /**
     * Get sector personality string
     * @param personality
     * @return
     */
    private String getSectorPersonalityString(byte personality) {
        String string = null;
        switch(personality) {
            case MESH_PERSONALITY_STA_MODE:
                string = "STA";
                break;
            case MESH_PERSONALITY_PCP_MODE:
                string = "PCP";
                break;
            default:
        }
        return string;
    }

    /**
     * Get sector state string
     * @param state
     * @return
     */
    private String getStateString(byte state) {
        String string = null;
        switch(state) {
            case STATE_UNCONFIRMED_NO_ASSOC:
                string = "STATE_UNCONFIRMED_NO_ASSOC";
                break;
            case STATE_UNCONFIRMED_ASSOC:
                string = "STATE_UNCONFIRMED_ASSOC";
                break;
            case STATE_CONFIRMED_NO_ASSOC:
                string = "STATE_CONFIRMED_NO_ASSOC";
                break;
            case STATE_CONFIRMED_ASSOC:
                string = "STATE_CONFIRMED_ASSOC";
                break;
            default:
        }
        return string;
    }

    /**
     * Returns a summary of the sector data and its sublists
     */
    public String toString(int printAll) {
        String myString = toString();

        if (printAll == 1) {
            if (!potentialNeighborNodeList.isEmpty()) {
                myString = myString + "\n\tPotential Neighbor Nodes(" + potentialNeighborNodeList.size() + "):";
                for (NeighborNode node : potentialNeighborNodeList) {
                    myString += node.toString();
                }
            }
            if (!neighborNodeList.isEmpty()) {
                myString = myString + "\n\tNeighbor Nodes(" + neighborNodeList.size() + "):";
                for (NeighborNode node : neighborNodeList) {
                    myString += node.toString();
                }
            }
            if (!meshLinkList.isEmpty()) {
                myString = myString + "\n\tMesh Link List(" + meshLinkList.size() + "):";
                for (MeshLink link : meshLinkList) {
                    myString += link.toString();
                }
            }
        }
        myString += "\n";
        return myString;
    }

    /**
     *
     * @return
     */
    public byte getSectorState() {
        return state;
    }

}


