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

import org.onlab.packet.ChassisId;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onlab.util.HexString;
import org.onlab.util.Tools;
import org.onosproject.common.DefaultTopology;
import org.onosproject.common.DefaultTopologyGraph;
import org.onosproject.meshmanager.api.MeshProvider;
import org.onosproject.meshmanager.api.message.MeshConstants;
import org.onosproject.meshmanager.api.message.MeshMessage;
import org.onosproject.meshmanager.api.message.MeshNCConfigConfirm;
import org.onosproject.meshmanager.api.message.MeshNCConfigSetup;
import org.onosproject.meshmanager.api.message.MeshNCReportInd;
import org.onosproject.meshmanager.api.message.MeshSBIMessage;
import org.onosproject.meshmanager.api.message.MeshSBIMessageType;
import org.onosproject.meshmanager.api.message.OpenFlowMessage;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultDevice;
import org.onosproject.net.DefaultLink;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.topology.DefaultGraphDescription;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.onlab.packet.EthType.EtherType.IPV4;
import static org.onlab.util.Tools.toHex;
import static org.onosproject.net.DeviceId.deviceId;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class MeshRoutingManager {
    private final Logger logger = getLogger(getClass());

    public static final int LINK_COST_METRIC_NBHOPS = 1;
    public static final int LINK_COST_METRIC_BW = 2;
    public static final int MAX_HOPS = 5;
    public static final byte PCP_TO_STA = 0;
    public static final byte STA_TO_PCP = 1;

    // Maximum available sector BW as a percentage (100%)
    private static final long MAX_SECTOR_BW = 1000000;

    // Maximum BW per MCS (mbps)
    //   - Assumes equal ratio of UL & DL TDD
    //   - Values provided by system team
    private static final int[] MAX_MCS_BW = {
            275,
            385,
            770,
            962,    // 962.5
            1155,
            1251,   // 1251.25
            1540,
            1925,
            2310,
            2502,   // 2502.5
            3080,
            3850,
            4620
    };

    private Map<Link, Long> linkCostMapNbHops = new HashMap<>();
    private Map<Link, Long> linkCostMapBW = new HashMap<>();
    private Map<Integer,Map<Link, Long>> linkCostType = new HashMap<>();

    // This mapping is used to convert an ONOS link info (ConnectPoints) to Sector MAC addresses
    // It is updated when new sectors are added
    private Map<ConnectPoint, MeshSector> connectPointSectorMap = new HashMap<>();
    private Map<Long, ConnectPoint> sectorMacConnectPointMap = new HashMap<>();

    private ConcurrentHashMap<Long, VlanInfo> vlanInfoMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Short, Long> vlanMacDlMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Long> sectorMacFakeSPGWMacMap = new ConcurrentHashMap<>();

    private MeshManager meshManager;
    private MeshDB meshDatabase;

    private long spGwMAC;
    private short nextAvailableVlanId;
    private boolean[] allocationVLanArray = new boolean[MeshConstants.MAX_AVAILABLE_VLAN_ID + 1];

    private TopologyService topologyService;
    private DefaultTopology meshTopology;
    private Set<Device> meshTopoVertexes;
    private Set<Link> meshTopoLinks;
    private boolean modifiedGraphSets;
    private List<Link> prunedLinkList = new ArrayList<>();

    /**
     * Constructor
     * @param meshDatabase
     */
    public MeshRoutingManager(MeshManager meshManager, MeshDB meshDatabase) {
        this.meshManager = meshManager;
        this.meshDatabase = meshDatabase;

        linkCostType.put(LINK_COST_METRIC_NBHOPS, linkCostMapNbHops);
        linkCostType.put(LINK_COST_METRIC_BW, linkCostMapBW);

        nextAvailableVlanId = MeshConstants.MIN_AVAILABLE_VLAN_ID;
        for (int i = 0; i < this.allocationVLanArray.length; i++) {
            allocationVLanArray[i] = false;
        }

        // Initialize the local topology
        topologyService = meshManager.getTopologyService();
        meshTopoVertexes = new HashSet<>();
        meshTopoLinks = new HashSet<>();
        modifiedGraphSets = true;
    }

    /**
     *
     * @return
     */
    public short getAvailableVlanId() {
        short index = nextAvailableVlanId;
        short vlanId;
        do {
            if (MeshConstants.MAX_AVAILABLE_VLAN_ID == index) {
                index = MeshConstants.MIN_AVAILABLE_VLAN_ID;
            } else {
                index++;
            }
            if (index == nextAvailableVlanId) {
                vlanId = -1;
                break;
            }
        } while (allocationVLanArray[index]);

        nextAvailableVlanId = index;
        vlanId = index;
        logger.trace("get VLAN available " + vlanId + " -> next : " + index);

        return vlanId;
    }

    /**
     *
     * @param vlanId
     * @return
     */
    public boolean allocateVlan(short vlanId) {
        if (allocationVLanArray[vlanId]) {
            return false;
        }
        allocationVLanArray[vlanId] = true;
        logger.trace("VLAN allocated " + vlanId);
        return true;
    }

    /**
     *
     * @param vlanId
     */
    public void deallocateVlan(short vlanId) {
        allocationVLanArray[vlanId] = false;
    }

    /**
     *
     * @param key
     * @return
     */
    public VlanInfo getVlanInfo(long key) {
        return vlanInfoMap.get(key);
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setVlanInfo(long key, VlanInfo value) {
        vlanInfoMap.put(key, value);
    }

    /**
     *
     * @param key
     * @return
     */
    public Long getVlanMacDl(short key) {
        return vlanMacDlMap.get(key);
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setVlanMacDl(short key, long value) {
        vlanMacDlMap.put(key, value);
    }

    /**
     *
     * @param key
     * @return
     */
    public Long getSectorMacFakeSPGWMacMap(long key) {
        return sectorMacFakeSPGWMacMap.get(key);
    }

    /**
     *
     * @param spgwValue
     */
    public void setSPGW(long spgwValue) {
        spGwMAC = spgwValue;
        logger.trace("getting the SPGW mac : " + HexString.toHexString(spGwMAC));
    }

    /**
     *
     * @return
     */
    public long getSPGW() {
        return spGwMAC;
    }

    /**
     *
     * @param spgwMappingString
     */
    public void setSPGWFakeBridgeMapping(String spgwMappingString) {
        int nbOfRead;
        long fakeMac, sectorMac;

        logger.trace("getting the mac Mapping for SPGW settings");
        //the string is in the form and need to cut it in relevant portions
        //mac1,reqSla1,mac2,reqSla2

        List<String> macMappings = Arrays.asList(spgwMappingString.split("\\s*,\\s*"));

        if (spgwMappingString != null) {
            int i=0;
            try {
                //we are supposed to have even number of entries
                nbOfRead = macMappings.size();
                while (i < nbOfRead) {
                    fakeMac = Tools.fromHex(macMappings.get(i++));
                    sectorMac = Tools.fromHex(macMappings.get(i++));
                    sectorMacFakeSPGWMacMap.put(sectorMac, fakeMac);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid spgw value:{}, around index({})", spgwMappingString, i);
            }
        }
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setConnectPointSectorMap(ConnectPoint key, MeshSector value) {
        connectPointSectorMap.put(key, value);
    }

    /**
     *
     * @param key
     * @return
     */
    public MeshSector getConnectPointSectorMap(ConnectPoint key) {
        return connectPointSectorMap.get(key);
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setSectorMacConnectPointMap(long key, ConnectPoint value) {
        sectorMacConnectPointMap.put(key, value);
    }

    /**
     *
     * @param key
     * @return
     */
    public ConnectPoint getSectorMacConnectPointMap(long key) {
        return sectorMacConnectPointMap.get(key);
    }

    /**
     *
     * @param debugLevel
     */
    public void toString(byte debugLevel) {
        printGraph(debugLevel);
        logger.debug("\n\nMeshRoutingManager Mappings:" +
                             "\n\tsectorMacConnectPointMap: " + printSectorMacConnectPointMap() +
                             "\n\tconnectPointSectorMap: " + printConnectPointSectorMap());
    }

    /**
     *
     * @param debugLevel
     */
    public void printGraph(byte debugLevel) {
        // Make sure topology is up to date
        updateMeshTopology();

        DefaultTopologyGraph graph = (DefaultTopologyGraph)topologyService.getGraph(meshTopology);

        switch(debugLevel) {
            case MeshConstants.TRACE:
                logger.trace("DB graph: " + graph.toString());
                break;
            case MeshConstants.DEBUG:
                logger.debug("DB graph: " + graph.toString());
                break;
            default:
                break;
        }
    }

    /**
     * Add Mesh Node to Topology graph, if not already present
     * @param meshNode
     * @return
     */
    public boolean addNode(MeshNode meshNode) {
        boolean nodeAdded = false;
        Device device = new DefaultDevice(ProviderId.NONE, meshNode.getMeshDeviceId(),
                                          Device.Type.OTHER, "", "", "", "", new ChassisId());

        if (meshTopoVertexes.add(device)) {
            modifiedGraphSets = true;
            logger.trace("Graph addNode: " + device.toString());
            nodeAdded = true;
        }
        return nodeAdded;
    }

    /**
     * Remove Mesh Node from Topology graph, if present
     * Remove Mesh Sector connect points
     * @param meshNode
     * @return
     */
    public void removeMeshNode(MeshNode meshNode) {
        List<Short> vlanIdList = new ArrayList<>();
        Device device = new DefaultDevice(ProviderId.NONE, meshNode.getMeshDeviceId(),
                                          Device.Type.OTHER, "", "", "", "", new ChassisId());

        // Reset FWD TBL entries and release BW used by mesh node & associated access nodes
        vlanIdList.add(meshNode.getVlanIdDl());
        vlanIdList.add(meshNode.getVlanIdUl());
        for (AccessNode accessNode : meshNode.getAccessNodeList()) {
            vlanIdList.add(accessNode.getVlanIdDl());
            vlanIdList.add(accessNode.getVlanIdUl());
        }
        meshDatabase.releaseBW(vlanIdList);
        resetFwdTbl(vlanIdList);

        // Remove all mesh sector Connect Points
        for (MeshSector meshSector : meshNode.getMeshSectList()) {
            removeMeshSector(meshSector);
        }

        // Remove Mesh Node vertex from topology
        if (meshTopoVertexes.remove(device)) {
            modifiedGraphSets = true;
            logger.trace("Graph removeNode: " + device.toString());
        } else {
            logger.trace("Graph removeNode - device not found: " + device.toString());
        }
    }

    /**
     * Create connection point for provided sector
     * @param meshNode
     * @param meshSector
     */
    public void addMeshSector(MeshNode meshNode, MeshSector meshSector) {
        // Create new ConnectPoint for sector
        DeviceId devId = meshNode.getMeshDeviceId();
        PortNumber portNb = PortNumber.portNumber((long)meshSector.getSectorIndex());
        ConnectPoint connectPoint = new ConnectPoint(devId, portNb);

        // Add sector ConnectPoint mappings
        connectPointSectorMap.put(connectPoint, meshSector);
        sectorMacConnectPointMap.put(meshSector.getOwnSectMac(), connectPoint);

        // Store ConnectPoint in sector
        meshSector.setConnectPoint(connectPoint);
    }

    /**
     * Create connection point for provided sector
     * @param meshSector
     */
    public void removeMeshSector(MeshSector meshSector) {
        // Remove sector ConnectPoint mappings
        sectorMacConnectPointMap.remove(meshSector.getOwnSectMac());
        connectPointSectorMap.remove(meshSector.getConnectPoint());

        // Remove ConnectPoint from sector
        meshSector.setConnectPoint(null);
    }

    /**
     * Release BW & reset FWD TBL entries for access node
     * @param accessNode
     */
    public void removeAccessNode(AccessNode accessNode) {
        List<Short> vlanIdList = new ArrayList<>();

        // Reset FWD TBL entries and release BW used by access node
        vlanIdList.add(accessNode.getVlanIdDl());
        vlanIdList.add(accessNode.getVlanIdUl());
        meshDatabase.releaseBW(vlanIdList);
        resetFwdTbl(vlanIdList);
    }

    /**
     * Add link and reverse link to Topology graph, if not already present.
     * Set link metrics for both directional links.
     * @param meshSectorMac1
     * @param meshSectorMac2
     * @param metricType
     * @param value
     * @param valueReverse
     * @return
     */
    public boolean addDualLinks(
            long meshSectorMac1,
            long meshSectorMac2,
            int metricType,
            long value,
            long valueReverse) {

        boolean linksAdded = false;
        ConnectPoint srcConnectPoint = sectorMacConnectPointMap.get(meshSectorMac1);
        ConnectPoint dstConnectPoint = sectorMacConnectPointMap.get(meshSectorMac2);

        if ((null == srcConnectPoint) || (null == dstConnectPoint)) {
            logger.debug("Src or Dst connect point not found in sectorMacConnectPointMap");
            return false;
        }

        logger.trace("addLink connectPoints : " + srcConnectPoint.toString() +
                             " " + dstConnectPoint.toString());

        Link link = DefaultLink.builder()
                .src(srcConnectPoint)
                .dst(dstConnectPoint)
                .providerId(ProviderId.NONE)
                .state(Link.State.ACTIVE)
                .type(Link.Type.DIRECT)
                .build();

        Link reverseLink = DefaultLink.builder()
                .src(dstConnectPoint)
                .dst(srcConnectPoint)
                .providerId(ProviderId.NONE)
                .state(Link.State.ACTIVE)
                .type(Link.Type.DIRECT)
                .build();

        // Add original and reverse links
        linksAdded &= addLink(link);
        linksAdded &= addLink(reverseLink);

        // Update link metrics for the newly added links
        setLinkMetric(link, metricType, value);
        setLinkMetric(reverseLink, metricType, valueReverse);

        return linksAdded;
    }


    /**
     * Remove link and reverse link from Topology graph, if present.
     * @param ownSectMac
     * @param peerSectMac
     * @return
     */
    public boolean removeDualLinks(long ownSectMac, long peerSectMac) {
        boolean linksRemoved = false;
        ConnectPoint srcConnectPoint = sectorMacConnectPointMap.get(ownSectMac);
        ConnectPoint dstConnectPoint = sectorMacConnectPointMap.get(peerSectMac);

        if ((null != srcConnectPoint) && (null != dstConnectPoint)) {
            Link link = DefaultLink.builder()
                    .src(srcConnectPoint)
                    .dst(dstConnectPoint)
                    .providerId(ProviderId.NONE)
                    .state(Link.State.ACTIVE)
                    .type(Link.Type.DIRECT)
                    .build();
            linksRemoved = removeDualLinks(link);
        }
        return linksRemoved;
    }

    /**
     * ownMac is the mac of the NGW node to which an accessNode could be
     * connected to (will eventually call addSlice)
     * @param meshNode
     * @param isRouteToGw
     * @return
     */
    public long getMaxAvailableBW(MeshNode meshNode, boolean isRouteToGw) {
        MeshNode srcNode, dstNode;
        long maxPathBW = 0;
        List<Link> removedLinks = new ArrayList<>();

        // this is not real time... so we take the time to compute every path
        // using the same algo as usual, but we then evaluate the max BW within
        // that path (which is the minimum value of the BW available on all
        // the edges)
        logger.trace("getMaxAvailableBW : isRouteToGw: " + isRouteToGw);
        logger.trace("whole maps: " + linkCostMapBW.toString());

        // Validate input parameters
        if (meshNode == null) {
            logger.error("meshNode == null");
            return -1;
        }

        // Retrieve source and destination nodes
        if (isRouteToGw) {
            srcNode = meshNode;
            dstNode = meshNode.getGwNode();
        } else {
            srcNode = meshNode.getGwNode();
            dstNode = meshNode;
        }

        // Loop through all possible paths to find path with highest min metric
        while (true) {
            long linkBW, minBW = -1;
            Link linkToRemove = null;

            // Retrieve path from latest topology
            updateMeshTopology();
            Set<Path> paths = topologyService.getPaths(
                    meshTopology, srcNode.getMeshDeviceId(), dstNode.getMeshDeviceId());

            // Exit loop if no more paths available
            if (paths.isEmpty()) {
                break;
            }

            Path path = paths.iterator().next();
            logger.debug("Path metric result (#hops: " + path.links().size() +
                                 ") -- " + path.toString());

            // Loop through path links and find link with lowest metric value closest to
            // the source node
            for (Link link : path.links()) {
                linkBW = getLinkMetric(link, MeshRoutingManager.LINK_COST_METRIC_BW);
                if ((minBW == -1) || (linkBW < minBW)) {
                    minBW = linkBW;
                    linkToRemove = link;
                }
            }

            // Remove link with lowest metric
            if (linkToRemove == null) {
                logger.error("No links to delete!!!");
                break;
            }
            removedLinks.add(linkToRemove);
            removeLink(linkToRemove);

            // If current path minimum metric is greater than current maximum
            // end-to-end path metric, update maximum path metric
            if (minBW > maxPathBW) {
                maxPathBW = minBW;
            }
        }

        // Revert topology back to original state by adding removed links
        for (Link link : removedLinks) {
            addLink(link);
        }

        logger.trace("getMaxAvailableBW: returnValue " + maxPathBW);
        return maxPathBW;
    }



    /**
     * Set the provided link metric
     * @param mac1
     * @param mac2
     * @param type
     * @param value
     */
    public void setLinkMetric(long mac1, long mac2, int type, long value) {
        ConnectPoint srcConnectPoint = sectorMacConnectPointMap.get(mac1);
        ConnectPoint dstConnectPoint = sectorMacConnectPointMap.get(mac2);
        if ((srcConnectPoint == null) || (dstConnectPoint == null)) {
            return;
        }

        Link link = DefaultLink.builder()
                .src(srcConnectPoint)
                .dst(dstConnectPoint)
                .providerId(ProviderId.NONE)
                .state(Link.State.ACTIVE)
                .type(Link.Type.DIRECT)
                .build();

        if (link != null) {
            setLinkMetric(link, type, value);
        }
    }

    /**
     * Get the BW usage based on the requested SLA & link MCS
     * NOTE: BW usage is represented as a fraction of the MAX LINK BW
     * @param reqSla
     * @param mcs
     * @return
     */
    public int getBWUsage(int reqSla, int mcs) {
        if (reqSla <= 0) {
            return 0;
        }
        long value = (((reqSla * MAX_SECTOR_BW) / ((long)MAX_MCS_BW[mcs])) + 1);
        return (int)value;
    }

    /**
     * Get the available BW based on the provided BW usage.
     * NOTE: BW usage is represented as a fraction of the MAX LINK BW
     * @param bwUsage
     * @param mcs
     * @return
     */
    public int getAvailBW(int bwUsage, int mcs) {
        long value = ((((getMaxAvailBW() - bwUsage) * ((long)MAX_MCS_BW[mcs])) / MAX_SECTOR_BW));
        return (int)value;
    }

    /**
     *
     * @param meshNode
     */
    public void sendBroadcastAsPacketIn(MeshNode meshNode) {
        logger.trace("sendBroadcastAsPacketIn");
        MacAddress dstMac = MacAddress.valueOf(meshNode.getOwnBridgeMac());
        FlowRuleService flowRuleService = meshManager.getFlowRuleService();

        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder()
                .matchEthDst(MacAddress.BROADCAST);
        TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder()
                .setOutput(PortNumber.CONTROLLER);

        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(meshNode.getAssociatedDeviceId())
                .withSelector(selectorBuilder.build())
                .withTreatment(treatmentBuilder.build())
                .withPriority(MeshConstants.ODL_FLOW_COOKIE_PACKET_IN_PRIORITY)
                .withCookie(MeshConstants.ODL_FLOW_COOKIE_SELF)
                .makePermanent()
                .build();

        //FlowRuleOperations.Builder buildMesser = FlowRuleOperations.builder();
        //builder.add(flowRule);
        logger.debug("FlowRule: " + flowRule.toString());
        flowRuleService.applyFlowRules(flowRule);
    }

    /**
     *
     * @param meshNode
     */
    public void sendDefaultDrop(MeshNode meshNode) {
        logger.trace("sendDefaultDrop");
        FlowRuleService flowRuleService = meshManager.getFlowRuleService();
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder();

        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(meshNode.getAssociatedDeviceId())
                .withSelector(selectorBuilder.build())
                .withTreatment(treatmentBuilder.build())
                .withPriority(MeshConstants.ODL_FLOW_COOKIE_LOWEST_PRIORITY)
                .withCookie(MeshConstants.ODL_FLOW_COOKIE_SELF)
                .makePermanent()
                .build();

        //FlowRuleOperations.Builder builder = FlowRuleOperations.builder();
        //builder.add(flowRule);
        logger.debug("FlowRule: " + flowRule.toString());
        flowRuleService.applyFlowRules(flowRule);
    }

    /**
     *
     * @param meshNode
     */
    public void sendDefaultPop(MeshNode meshNode) {
        logger.trace("sendDefaultPop");
        FlowRuleService flowRuleService = meshManager.getFlowRuleService();
        MacAddress dstMac = MacAddress.valueOf(meshNode.getOwnBridgeMac());
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder()
                .matchVlanId(VlanId.vlanId(meshNode.getVlanIdDl()));

        TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder()
                .setEthDst(dstMac)
                .popVlan()
                .setOutput(PortNumber.LOCAL);

        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(meshNode.getAssociatedDeviceId())
                .withSelector(selectorBuilder.build())
                .withTreatment(treatmentBuilder.build())
                .withPriority(MeshFwdTblEntry.PRIO_DL_SIG)
                .withCookie(MeshConstants.ODL_FLOW_COOKIE_SELF_POP_VLAN)
                .makePermanent()
                .build();

        logger.debug("FlowRule: " + flowRule.toString());
        flowRuleService.applyFlowRules(flowRule);
    }

    /**
     *
     * @param gwNode
     */
    public void sendDefaultFromGWSelfFlow(MeshNode gwNode) {
        logger.trace("sendDefaultFromGWSelfFlow");
        FlowRuleService flowRuleService = meshManager.getFlowRuleService();

        // Set mac to SPGW, if present; otherwise, no need for this rule
        if (spGwMAC == 0) {
            return;
        }

        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder()
                .matchEthSrc(MacAddress.valueOf(gwNode.getOwnBridgeMac()));

        TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder()
                .setEthDst(MacAddress.valueOf(spGwMAC))
                .setOutput(PortNumber.portNumber(gwNode.getSignalingPortIndex()));

        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(gwNode.getAssociatedDeviceId())
                .withSelector(selectorBuilder.build())
                .withTreatment(treatmentBuilder.build())
                .withPriority((short)(MeshConstants.ODL_FLOW_COOKIE_HIGHEST_BASIC_PRIORITY +
                        MeshConstants.ODL_FLOW_COOKIE_VLAN_PRIORITY_INCREMENT))
                .withCookie(MeshConstants.CATCH_ALL_TO_NC_ENTRY_ID)
                .makePermanent()
                .build();

        logger.debug("FlowRule: " + flowRule.toString());
        flowRuleService.applyFlowRules(flowRule);
    }

    /**
     *
     * @param meshNode
     */
    public void sendDefaultSelf(MeshNode meshNode) {
        logger.trace("sendDefaultSelf");
        FlowRuleService flowRuleService = meshManager.getFlowRuleService();
        MacAddress dstMac = MacAddress.valueOf(meshNode.getOwnBridgeMac());
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder()
                .matchEthDst(dstMac);

        TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder()
                .setEthDst(dstMac)
                .setOutput(PortNumber.LOCAL);

        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(meshNode.getAssociatedDeviceId())
                .withSelector(selectorBuilder.build())
                .withTreatment(treatmentBuilder.build())
                .withPriority(MeshConstants.ODL_FLOW_COOKIE_SELF_PRIORITY)
                .withCookie(MeshConstants.ODL_FLOW_COOKIE_SELF)
                .makePermanent()
                .build();

        logger.debug("FlowRule: " + flowRule.toString());
        flowRuleService.applyFlowRules(flowRule);
    }

    /**
     *
     * @param meshNode
     * @param command
     */
    public void sendDefaultFlood(MeshNode meshNode, short command) {
        logger.trace("sendDefaultFlood - flood default");
        FlowRuleService flowRuleService = meshManager.getFlowRuleService();

        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder()
                .matchEthType(IPV4.ethType().toShort());
        TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder()
                .setOutput(PortNumber.FLOOD);
        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(meshNode.getAssociatedDeviceId())
                .withSelector(selectorBuilder.build())
                .withTreatment(treatmentBuilder.build())
                .withPriority(MeshConstants.ODL_FLOW_COOKIE_GW_FLOOD_PRIORITY)
                .withCookie(MeshConstants.ODL_FLOW_COOKIE_TO_OUTSIDE_GW)
                .makePermanent()
                .build();

        logger.debug("FlowRule: " + flowRule.toString());
        if (command == MeshFwdTblEntry.ADD) {
            flowRuleService.applyFlowRules(flowRule);
        } else {
            flowRuleService.removeFlowRules(flowRule);
        }
    }

    /**
     *
     * @param id
     * @param msg
     * @return
     */
    public boolean sendMsgToSBI(DeviceId id, MeshSBIMessage msg) {
        MeshProvider provider;
        logger.info("SendMessage to SBI:\n" + msg.toString());

        // Retrieve provider
        provider = meshManager.getMeshProvider(id);
        if (provider == null) {
            logger.error("provider == null");
            return false;
        }

        // Retrieve & store CONFIG SETUP transaction ID
        if (msg.getType() == MeshSBIMessageType.NC_CONFIG_SETUP) {
            MeshNCConfigSetup cfgSetupMsg = (MeshNCConfigSetup)msg;
            if (cfgSetupMsg.isEmptyPayload()) {
                logger.trace("empty payload message, don't send");
                return false;
            }

            MeshNode meshNode = meshDatabase.getNodeIdMacNodeMap(cfgSetupMsg.getNodeId());
            meshNode.addTid(cfgSetupMsg.getTid(), cfgSetupMsg.getCfgType());
        }

        // Send SBI message
        provider.sendMessage(id, msg);
        return true;
    }

    /**
     *
     */
    public void processAssocUpdates() {
        // Send any ASSOC CFG messages that have no prerequisites
        processSerializedAssocMsgs();
    }

    /**
     * Process Mesh message by removing it as a prerequisite to the serialized ASSOC messages
     */
    public void processAssocPrereqMsg(MeshMessage msg) {
        // Only REPORT_IND LINK_FAILURE messages supported as ASSOC CFG prerequisite
        if (!(msg instanceof MeshNCReportInd)) {
            throw new AssertionError("instance != MeshNCReportInd");
        }
        if (((MeshNCReportInd)msg).getReportCode() != MeshNCReportInd.REPORT_LINK_FAILURE) {
            throw new AssertionError("code != REPORT_LINK_FAILURE");
        }

        // Remove prerequisite message from all serialized ASSOC messages, if match found
        for (SerializedMeshMessage serMsg : meshDatabase.getSerializedAssocMsgList()) {
            serMsg.removePrerequisite(msg);
        }

        // Process serialized ASSOC messages and send OF & mesh messages that are ready to be sent
        processSerializedAssocMsgs();
    }

    /**
     * Process Mesh message by removing it as a prerequisite to the serialized FWD TBL messages
     */
    public void processFwdTblPrereqMsg(MeshMessage msg) {
        // Only CFG_CNF messages supported as FWD TBL prerequisite
        if (!(msg instanceof MeshNCConfigConfirm)) {
            throw new AssertionError("instance != MeshNCConfigConfirm");
        }

        // Remove prerequisite message from all serialized FWD TBL messages, if match found
        for (SerializedMeshMessage serMsg : meshDatabase.getSerializedFwdTblMsgList()) {
            serMsg.removePrerequisite(msg);
        }

        // Process serialized FWD TBL messages and send OF & mesh messages that are ready to be sent
        processSerializedFwdTblMsgs();
    }

    /**
     * Update, serialize and send OF & Mesh messages for all required FWD TBL entries.
     * Reevaluate all primary & alternate, signaling & data paths.
     */
    public void processFwdTblUpdates() {
        logger.debug("processFwdTblUpdates");

        // Update FWD TBL entries for all mesh & access nodes
        updateFwdTbl();

        // Serialize updates to be sent
        serializeAllFwdTblMsgs();

        // Process serialized messages and send OF & mesh messages that are ready to be sent
        processSerializedFwdTblMsgs();
    }

    /**
     * Update, serialize and send OF & Mesh messages for provided mesh or access node FWD TBL
     * entries. Reevaluate primary & alternate, signaling or data paths.
     * @param meshNode
     * @param accessNode
     */
    public void processFwdTblUpdates(MeshNode meshNode, AccessNode accessNode, boolean sendUpdates) {
        boolean isData = (accessNode != null);
        logger.debug("processFwdTblUpdates: meshNode[" +
                             HexString.toHexString(meshNode.getOwnBridgeMac()) + "] accessNode[" +
                             ((isData) ? HexString.toHexString(accessNode.getOwnMac()) : "null") + "]");

        // Update FWD TBL entries for provided mesh node
        if (!updateFwdTbl(meshNode, accessNode)) {
            if (isData) {
                // Set SLA NOT MET flag if data path no longer available
                accessNode.setSlaMet(false);
            } else {
                // Remove mesh node if signaling path no longer available
                meshManager.removeNode(meshNode);
            }
        }

        if (sendUpdates) {
            // Serialize updates to be sent
            serializeAllFwdTblMsgs();

            // Process serialized messages and send OF & mesh messages that are ready to be sent
            processSerializedFwdTblMsgs();
        }
    }


    /**
     * Recalculate all Mesh & Access node primary & alternate paths
     * Update all FWD TBL entries accordingly
     */
    private void updateFwdTbl() {
        List<MeshNode> meshNodesToRemove = new ArrayList<>();
        logger.debug("updateFwdTbl");

        // Recalculate signaling paths
        logger.trace("Recalculate signaling paths");
        for (MeshNode meshNode : meshDatabase.getMeshNodeList()) {
            if (!updateFwdTbl(meshNode, null)) {
                // Add node to removal list if signaling path no longer available
                meshNodesToRemove.add(meshNode);
            }
        }

        // Remove mesh nodes that are no longer reachable
        for (MeshNode meshNode : meshNodesToRemove) {
            meshManager.removeNode(meshNode);
        }

        // Recalculate data paths
        logger.trace("Recalculate data paths");
        for (MeshNode meshNode : meshDatabase.getMeshNodeList()) {
            for (AccessNode accessNode : meshNode.getAccessNodeList()) {
                if (!updateFwdTbl(meshNode, accessNode)) {
                    // Set SLA NOT MET flag if data path no longer available
                    accessNode.setSlaMet(false);
                }
            }
        }
    }

    /**
     *
     * @param meshNode
     * @param accessNode
     */
    private boolean updateFwdTbl(MeshNode meshNode, AccessNode accessNode) {
        short vlanDl, vlanUl;
        byte pcpDl, pcpUl;
        int reqSlaUl, reqSlaDl, hopCount;
        boolean isData = (accessNode != null);
        MeshSector srcSector;
        MeshNode gwNode, parentNode, srcNode, dstNode;
        List<Link> removedLinks = new ArrayList<>();
        List<Link> primDlPath, altDlPath, primUlPath, altUlPath;
        List<Short> vlanIdList = new ArrayList<>();
        logger.debug("updateFwdTbl: meshNode[" +
                             HexString.toHexString(meshNode.getOwnBridgeMac()) + "] accessNode[" +
                             ((isData) ? HexString.toHexString(accessNode.getOwnMac()): "null") + "]");

        // Retrieve GW Node from mesh node
        gwNode = meshNode.getGwNode();

        // Ignore FWD TBL update request if mesh node is the GW node
        if (gwNode == null) {
            logger.debug("Mesh Node equal to GW node... ignoring");
            return true;
        }

        // Retrieve reqSla from mesh or access node if present
        if (isData) {
            reqSlaDl = accessNode.getReqSlaDl();
            reqSlaUl = accessNode.getReqSlaUl();
            vlanDl = accessNode.getVlanIdDl();
            vlanUl = accessNode.getVlanIdUl();
            pcpDl =  accessNode.getPcpDl();
            pcpUl = accessNode.getPcpUl();
        } else {
            reqSlaDl = meshNode.getReqSlaDl();
            reqSlaUl = meshNode.getReqSlaUl();
            vlanDl = meshNode.getVlanIdDl();
            vlanUl = meshNode.getVlanIdUl();
            pcpDl = meshNode.getPcpDl();
            pcpUl = meshNode.getPcpUl();
        }

        // Release DL & UL, primary and backup BW reserved for requested VLAN IDs
        vlanIdList.add(vlanDl);
        vlanIdList.add(vlanUl);
        meshDatabase.releaseBW(vlanIdList);

        // Reset FWD TBL entries for requested VLAN IDs
        resetFwdTbl(vlanIdList);

        // =============
        // PRIMARY PATHS
        // =============

        // PRIMARY DL PATH
        logger.debug("Calculating primary DL path for VLAN ID: " + vlanDl);

        // Retrieve primary DL path & add FWD TBL entries if path exists
        srcNode = gwNode;
        dstNode = meshNode;
        primDlPath = getPath(srcNode, dstNode, reqSlaDl, 0);

        // Release BW & delete installed flows if no primary DL path
        if (primDlPath.isEmpty()) {
            meshDatabase.releaseBW(vlanIdList);
            resetFwdTbl(vlanIdList);
            return false;
        }

        // Store number of hops in destination node
        if (isData) {
            accessNode.setNbHopsAway((short)(primDlPath.size()), MeshConstants.DOWNLINK);
        } else {
            meshNode.setNbHopsAway((short)(primDlPath.size()), MeshConstants.DOWNLINK);
        }

        // Process primary DL path (update FWD TBL entries & link attributes)
        processPath(primDlPath, srcNode, dstNode, gwNode, meshNode, accessNode,
                    vlanDl, pcpDl, reqSlaDl, false, true, (primDlPath.size() > 1));

        // PRIMARY UL PATH
        logger.debug("Calculating primary UL path for VLAN ID: " + vlanUl);

        // Retrieve primary UL path & add FWD TBL entries if path exists
        srcNode = meshNode;
        dstNode = gwNode;
        primUlPath = getPath(srcNode, dstNode, reqSlaUl, 0);

        // Release BW & delete installed flows if no primary UL path
        if (primUlPath.isEmpty()) {
            meshDatabase.releaseBW(vlanIdList);
            resetFwdTbl(vlanIdList);
            return false;
        }

        // Store number of hops in destination node
        if (isData) {
            accessNode.setNbHopsAway((short)(primUlPath.size()), MeshConstants.UPLINK);
        } else {
            meshNode.setNbHopsAway((short)(primUlPath.size()), MeshConstants.UPLINK);
        }

        // Set SLA MET flag
        if (isData) {
            accessNode.setSlaMet(true);
        }

        // Process primary UL path (update FWD TBL entries & link attributes)
        processPath(primUlPath, srcNode, dstNode, gwNode, meshNode, accessNode,
                    vlanUl, pcpUl, reqSlaUl, true, true, (primUlPath.size() > 1));

        // ===============
        // ALTERNATE PATHS
        // ===============
        // NOTE: Alternate path calculation only provides support for single link failure
        //      (i.e. there are no alternate paths for alternate paths)

        // Only process alternate paths if node is fully connected
        if (!meshNode.isComplete()) {
            return true;
        }

        // ALTERNATE DL PATH
        logger.debug("Calculating alternate DL path for VLAN ID: " + vlanDl);

        // Temporarily remove DL path links from topology
        for (Link link : primDlPath) {
            removeDualLinks(link);
            removedLinks.add(0, link);
        }

        // Start at last hop and look for alternate paths. Restore primary path 1 hop at
        // a time and check for alternate paths.
        hopCount = primDlPath.size();
        for (Link link : removedLinks) {
            srcSector = getMeshSectorFromLink(link, MeshConstants.TYPE_SRC_MAC);
            parentNode = srcSector.getParentNode();

            // Retrieve alternate path to destination
            srcNode = parentNode;
            dstNode = meshNode;
            altDlPath = getPath(parentNode, meshNode, reqSlaDl, --hopCount);

            // Process alternate DL path (update FWD TBL entries & link attributes)
            if (!altDlPath.isEmpty()) {
                processPath(altDlPath, srcNode, dstNode, gwNode, meshNode, accessNode,
                            vlanDl, pcpDl, reqSlaDl, false, false,
                            ((altDlPath.size() + hopCount) > 1));
            }

            // Add last removed link before checking for other alt paths
            addDualLinks(link);
        }

        // Reset removed links list
        removedLinks.clear();

        // ALTERNATE UL PATH
        logger.debug("Calculating alternate UL path for VLAN ID: " + vlanUl);

        // Temporarily remove UL path links from topology
        for (Link link : primUlPath) {
            removeDualLinks(link);
            removedLinks.add(0, link);
        }

        // Start at last hop and look for alternate paths. Restore primary path 1 hop at
        // a time and check for alternate paths.
        hopCount = primUlPath.size();
        for (Link link : removedLinks) {
            srcSector = getMeshSectorFromLink(link, MeshConstants.TYPE_SRC_MAC);
            parentNode = srcSector.getParentNode();

            // Retrieve alternate path to destination
            srcNode = parentNode;
            dstNode = gwNode;
            altUlPath = getPath(parentNode, gwNode, reqSlaUl, --hopCount);

            // Process alternate UL path (update FWD TBL entries & link attributes)
            if (!altUlPath.isEmpty()) {
                processPath(altUlPath, srcNode, dstNode, gwNode, meshNode, accessNode,
                            vlanUl, pcpUl, reqSlaUl, true, false,
                            ((altUlPath.size() + hopCount) > 1));
            }

            // Add last removed link before checking for other alt paths
            addDualLinks(link);
        }

        // Reset removed links list
        removedLinks.clear();

        return true;
    }

    /**
     *
     * @param vlanIdList
     */
    private void resetFwdTbl(List<Short> vlanIdList) {
        List<MeshFwdTblEntry> removalList = new ArrayList<>();
        logger.debug("resetFwdTbl");

        logger.trace("Setting FWD TBL entries for provided VLAN IDs to DELETE");
        for (MeshNode node : meshDatabase.getMeshNodeList()) {
            for (MeshFwdTblEntry entry : node.getMeshFwdTblEntryList()) {
                for (Short vlanId : vlanIdList) {
                    if (entry.getVlanId() == vlanId) {
                        // Remove entries that were never added
                        if (entry.getStatus() == MeshFwdTblEntry.STATUS_NEW) {
                            removalList.add(entry);
                            continue;
                        }

                        // Reset current entry
                        entry.reset();
                    }
                }
            }

            // Remove necessary entries
            for (MeshFwdTblEntry entry : removalList) {
                node.delMeshFwdTblEntry(entry);
            }
            removalList.clear();
        }
    }

    /**
     * Get path between 2 nodes
     * @param srcNode
     * @param dstNode
     * @param reqSla
     * @param hopCountOffset
     * @return
     */
    private List<Link> getPath(MeshNode srcNode, MeshNode dstNode, int reqSla, int hopCountOffset) {
        DeviceId srcDevId = srcNode.getMeshDeviceId();
        DeviceId dstDevId = dstNode.getMeshDeviceId();
        Link srcLink = null, dstLink = null;
        List<Link> links = new ArrayList<>();
        int hopCount = hopCountOffset;
        logger.debug("getPath: from " + HexString.toHexString(srcNode.getOwnBridgeMac()) +
                             " to " + HexString.toHexString(dstNode.getOwnBridgeMac()));
        logger.trace("getPath: from " + ((srcDevId == null) ? "null" : srcDevId.toString()) +
                             " to " + ((dstDevId == null) ? "null" : dstDevId.toString()));

        // If node is not connected with ovs agent so no deviceId has been created yet.
        // In this case, manually create the missing link and add it to the beginning or end
        // of the path (if src or dst respectively). This can be done because there is only
        // 1 link leading to the new node.
        if (!srcNode.isComplete()) {
            srcLink = createLink(srcNode.getTmpAssocMeshSect(), true);
            if (srcLink == null) {
                return links;
            }
            // Set new src Device ID to next hop
            srcDevId = srcLink.dst().deviceId();
            logger.trace("new srcDevId: " + srcDevId.toString());
        } else if (!dstNode.isComplete()) {
            dstLink = createLink(dstNode.getTmpAssocMeshSect(), false);
            if (dstLink == null) {
                return links;
            }
            // Set new dst Device ID to previous hop
            dstDevId = dstLink.src().deviceId();
            logger.trace("new dstDevId: " + dstDevId.toString());
        }

        // Only calculate path if source and destination devices are different
        if (srcDevId != dstDevId) {
            // Prune links that can't support requested SLA
            pruneLinks(reqSla);

            // Update mesh topology before retrieving paths
            updateMeshTopology();

            // Get all the paths between the src and dest device IDs
            logger.trace("real calculation from: " + srcDevId.toString() + " to " + dstDevId.toString());
            Set<Path> paths = topologyService.getPaths(meshTopology, srcDevId, dstDevId);

            // Restore pruned links
            restorePrunedLinks();
            printGraph(MeshConstants.TRACE);

            // Add first path to link list, if it exists
            if (!paths.isEmpty()) {
                Path path = paths.iterator().next();
                links.addAll(path.links());
                hopCount += links.size();
                logger.trace("Path metric result (#hops: " + hopCount + ") -- " + path.toString());
            } else {
                // Return empty link list if no path available
                logger.trace("Destination unreachable from " +
                                     HexString.toHexString(srcNode.getNodeId()) + " to " +
                                     HexString.toHexString(dstNode.getNodeId()));
                return links;
            }
        }

        // Add new link to the Link list, if necessary
        if (srcLink != null) {
            logger.trace("Adding new link at the beginning of the list");
            links.add(0, srcLink);
            hopCount++;
        } else if (dstLink != null) {
            logger.trace("Adding new link at the end of the list");
            links.add(dstLink);
            hopCount++;
        }

        // Make sure path does not exceed limit
        if (hopCount >= MAX_HOPS) {
            links.clear();
            logger.trace("Destination from " + HexString.toHexString(srcNode.getNodeId()) +
                                 " to " + HexString.toHexString(dstNode.getNodeId()) +
                                 " beyond our reach");
        }

        logger.trace("Path metric result (#hops: " + hopCount + ")");
        return links;
    }

    /**
     * Remove all (directional) edges that cannot meet SLA requirements
     * @param reqSla
     */
    private void pruneLinks(int reqSla) {
        int availBW;
        logger.trace("Pruning links that cannot meet SLA requirements");

        // If reqSla is 0, return immediately
        if (reqSla == 0) {
            logger.trace("reqSla == 0, no pruning required");
            return;
        }

        // Search through all links in DB
        for (MeshNode node : meshDatabase.getMeshNodeList()) {
            for (MeshSector sector : node.getMeshSectList()) {
                for (MeshLink link : sector.getMeshLinkList()) {
                    // Prune STA to PCP link if needed BW is greater than available BW
                    availBW = getAvailBW(sector.getUsedBW(), link.getMCS(STA_TO_PCP));
                    if (reqSla > availBW) {
                        logger.trace("Adding to pruning list: " +
                                             HexString.toHexString(link.getPeerMac()) +
                                             " to " + HexString.toHexString(sector.getOwnSectMac()) +
                                             " because availBW=" + availBW +
                                             " < reqSla=" + reqSla);
                        pruneLink(link.getPeerMac(), sector.getOwnSectMac());
                    }
                    // Prune PCP to STA link if needed BW is greater than available BW
                    availBW = getAvailBW(sector.getUsedBW(), link.getMCS(PCP_TO_STA));
                    if (reqSla > availBW) {
                        logger.trace("Adding to pruning list: " +
                                             HexString.toHexString(sector.getOwnSectMac()) +
                                             " to " + HexString.toHexString(link.getPeerMac()) +
                                             " because availBW=" + availBW +
                                             " < reqSla=" + reqSla);
                        pruneLink(sector.getOwnSectMac(), link.getPeerMac());
                    }
                }
            }
        }
        logger.trace("Pruning complete");
    }

    /**
     * Prune (directional) link
     * @param ownMac
     * @param peerMac
     */
    private void pruneLink(long ownMac, long peerMac) {
        ConnectPoint ownConnectPoint = sectorMacConnectPointMap.get(ownMac);
        ConnectPoint peerConnectPoint = sectorMacConnectPointMap.get(peerMac);

        if ((null == ownConnectPoint) || (null == peerConnectPoint)) {
            logger.debug("Src or Dst connect point not found in sectorMacConnectPointMap");
            return;
        }

        Link linkToPrune = DefaultLink.builder()
                .src(ownConnectPoint)
                .dst(peerConnectPoint)
                .providerId(ProviderId.NONE)
                .state(Link.State.ACTIVE)
                .type(Link.Type.DIRECT)
                .build();

        if (!prunedLinkList.contains(linkToPrune)) {
            prunedLinkList.add(linkToPrune);
            removeLink(linkToPrune);
        }
    }

    /**
     * Restore all pruned links
     */
    private void restorePrunedLinks() {
        logger.trace("Restoring pruned links");
        for (Link link : prunedLinkList) {
            addLink(link);
        }
        prunedLinkList.clear();
    }

    /**
     *
     * @param path
     * @param srcNode
     * @param dstNode
     * @param accessNode
     * @param vlanId
     * @param pcp
     * @param reqSla
     * @param isRouteToGw
     * @param isPrimPath
     */
    private void processPath(
            List<Link> path,
            MeshNode srcNode,
            MeshNode dstNode,
            MeshNode gwNode,
            MeshNode leafNode,
            AccessNode accessNode,
            short vlanId,
            byte pcp,
            int reqSla,
            boolean isRouteToGw,
            boolean isPrimPath,
            boolean isMultihop) {

        byte mode, state, dstMode = 0;
        short srcPort, primDstPort = 0;
        long srcMac, dstMac, primDstMac = 0;
        long linkSrcSectMac, linkDstSectorMac, linkDstNodeMac;
        MeshSector linkSrcSect, linkDstSector;
        MeshNode linkSrcNode, linkDstNode, peerNode = null;
        boolean isDstLink, isData = (accessNode != null);
        logger.debug("processPath");

        // Retrieve leaf node status
        if (leafNode.isComplete()) {
            state = MeshFwdTblEntry.STATE_COMPLETE;
        } else if (leafNode.isConfirmed()) {
            state = MeshFwdTblEntry.STATE_CONFIRMED;
        } else if (leafNode.getAssociatedDeviceId() != null) {
            state = MeshFwdTblEntry.STATE_UNCONFIRMED;
        } else {
            state = MeshFwdTblEntry.STATE_DISCONNECTED;
        }

        // Determine source and destination MAC addresses
        srcMac = (isRouteToGw) ?
                ((isData) ? accessNode.getOwnMac() : leafNode.getOwnBridgeMac()) :
                gwNode.getOwnBridgeMac();
        dstMac = (isRouteToGw) ?
                gwNode.getOwnBridgeMac() :
                ((isData) ? accessNode.getOwnMac() : leafNode.getOwnBridgeMac());

        // For each link on path, add/update FWD TBL entry for link source node
        // When last link is reached, add/update FWD TBL entry for link destination node
        for (Link link : path) {

            // Retrieve Mesh Sectors
            linkSrcSect = getMeshSectorFromLink(link, MeshConstants.TYPE_SRC_MAC);
            linkDstSector = getMeshSectorFromLink(link, MeshConstants.TYPE_DST_MAC);

            // Handle case where Src or Dst sector connection point does not exist yet
            // (i.e. for a new node)
            if (linkSrcSect == null) {
                linkSrcSect = srcNode.getTmpAssocMeshSect();
            } else if (linkDstSector == null) {
                linkDstSector = dstNode.getTmpAssocMeshSect();
            }

            linkSrcNode = linkSrcSect.getParentNode();
            linkDstNode = linkDstSector.getParentNode();
            linkDstNodeMac = linkDstNode.getOwnBridgeMac();
            linkSrcSectMac = linkSrcSect.getOwnSectMac();
            linkDstSectorMac = linkDstSector.getOwnSectMac();
            srcPort = linkSrcSect.getSectorIndex();
            logger.debug("Port info: " + HexString.toHexString(linkSrcSectMac) + " to " +
                                 HexString.toHexString(linkDstSectorMac));

            // Determine Mesh Node path mode
            if (linkSrcNode.equals(leafNode)) {
                mode = MeshFwdTblEntry.MODE_LEAF;
                peerNode = linkDstNode;
            } else if (linkSrcNode.equals(gwNode)) {
                mode = MeshFwdTblEntry.MODE_GW;
            } else if (linkSrcNode.equals(peerNode) || linkDstNode.equals(leafNode)) {
                mode = MeshFwdTblEntry.MODE_PEER;
            } else {
                mode = MeshFwdTblEntry.MODE_TRANSIT;
            }

            // VTB PATCH: For DL signaling entries on GW node, DST MAC matching rule must be set
            //            to mapped bridge MAC value.
            Long fakeMac = null;
            if ((!isRouteToGw) && (!isData) && (mode == MeshFwdTblEntry.MODE_GW)) {
                fakeMac = getSectorMacFakeSPGWMacMap(leafNode.getOwnBridgeMac());
                if (fakeMac != null) {
                    dstMac = fakeMac;
                }
            }

            // Set primary or alternate port information on the provided path, when necessary
            MeshFwdTblEntry entry = linkSrcNode.getMeshFwdTblEntryByVlanId(vlanId);
            if (entry == null) {
                // Create new FWD TBL Entry
                logger.debug("Set primary port info on new entry");
                MeshFwdTblEntry fwdTblEntry = new MeshFwdTblEntry(
                        meshDatabase, linkSrcNode, vlanId, mode, state,
                        isRouteToGw, isMultihop, isData, srcMac, dstMac, vlanId, pcp, srcPort,
                        linkSrcSectMac, linkDstSectorMac, linkDstNodeMac);

                // Add new FWD TBL Entry to mesh node
                linkSrcNode.addMeshFwdTblEntry(fwdTblEntry);
            } else if (entry.getFwdTblEntry() == null) {
                // Set primary port info for existing FWD TBL entry
                logger.debug("Set primary port info on existing entry");
                entry.setPrimInfo(
                        mode, state, isRouteToGw, isMultihop, isData,
                        srcMac, dstMac, vlanId, pcp, srcPort,
                        linkSrcSectMac, linkDstSectorMac, linkDstNodeMac);
            } else if (linkSrcNode.equals(srcNode)) {
                // First link in alternate path. Add alternate port.
                logger.debug("Set alternate port info on existing entry");
                entry.setAltInfo(linkSrcSect.getSectorIndex(), linkSrcSectMac,
                                 linkDstSectorMac, linkDstNodeMac);
            } else {
                // Primary port on alternate path node already set. Return immediately.
                logger.trace("Primary port info already set on existing entry. Breaking.");
                break;
            }

            // VTB PATCH: Reset DST MAC if it was previously altered
            if (fakeMac != null) {
                dstMac = leafNode.getOwnBridgeMac();
            }

            // Update link attributes for all links along the path
            // NOTE: Links are only available on PCP side of link
            if (linkSrcSect.getSectorPersonality() == MeshSector.MESH_PERSONALITY_PCP_MODE) {
                linkSrcSect.updateLinkAttributes(
                        vlanId, linkDstSectorMac, reqSla,
                        MeshRoutingManager.PCP_TO_STA, isPrimPath);
            } else {
                linkDstSector.updateLinkAttributes(
                        vlanId, linkSrcSectMac, reqSla,
                        MeshRoutingManager.STA_TO_PCP, isPrimPath);
            }

            // Set primary port information on destination if GW or LEAF
            isDstLink = false;
            if (linkDstNode.equals(gwNode)) {
                primDstMac = (spGwMAC != 0) ? spGwMAC : linkDstNodeMac;
                primDstPort = (isData) ?
                        ((linkDstNode.getDataPortIndex() != 0) ? linkDstNode.getDataPortIndex() : -1) :
                        ((linkDstNode.getSignalingPortIndex() != 0) ? linkDstNode.getSignalingPortIndex() : -1);
                dstMode = MeshFwdTblEntry.MODE_GW;
                isDstLink = true;
            } else if (linkDstNode.equals(leafNode)) {
                primDstMac = dstMac;
                primDstPort = (isData) ? linkDstNode.getDataPortIndex() : -1;
                dstMode = MeshFwdTblEntry.MODE_LEAF;
                isDstLink = true;
            }

            if (isDstLink) {
                MeshFwdTblEntry dstEntry = linkDstNode.getMeshFwdTblEntryByVlanId(vlanId);
                if (dstEntry == null) {
                    // Create new FWD TBL Entry
                    logger.debug("Set primary port info on new destination entry");
                    MeshFwdTblEntry fwdTblEntry = new MeshFwdTblEntry(
                            meshDatabase, linkDstNode, vlanId, dstMode, state,
                            isRouteToGw, isMultihop, isData, srcMac, dstMac,
                            vlanId, pcp, primDstPort, -1, primDstMac, -1);

                    // Add new FWD TBL Entry to mesh node
                    linkDstNode.addMeshFwdTblEntry(fwdTblEntry);
                } else if (dstEntry.getFwdTblEntry() == null) {
                    // Set primary port info for existing FWD TBL entry
                    logger.debug("Set primary port info on existing destination entry");
                    dstEntry.setPrimInfo(
                            dstMode, state, isRouteToGw, isMultihop, isData,
                            srcMac, dstMac, vlanId, pcp, primDstPort, -1, primDstMac, -1);
                }
            }
        }
    }

    /**
     * Clear serialized messages and reevaluate FWD TBL Entries to determine new messages
     * that must be serialized.
     */
    private void serializeAllFwdTblMsgs() {
        logger.debug("serializeAllFwdTblMsgs");

        // Reset serialized flag for all FWD TBL entries
        for (MeshNode meshNode : meshDatabase.getMeshNodeList()) {
            for (MeshFwdTblEntry entry : meshNode.getMeshFwdTblEntryList()) {
                entry.setSerialized(false);
            }
        }

        // Empty serialized message list
        meshDatabase.getSerializedFwdTblMsgList().clear();

        // For DL & UL VLAN of each mesh node, encode and serialize Config Setup messages
        for (MeshNode meshNode : meshDatabase.getMeshNodeList()) {
            serializeFwdTblMsgs(meshNode, null, false);
            serializeFwdTblMsgs(meshNode, null, true);

            // For DL & UL VLAN of each access node, encode and serialize Config Setup messages
            for (AccessNode accessNode : meshNode.getAccessNodeList()) {
                serializeFwdTblMsgs(meshNode, accessNode, false);
                serializeFwdTblMsgs(meshNode, accessNode, true);
            }
        }

        // Look through all FWD TBL Entries and serialize any remaining messages
        serializeFwdTblMsgs((short)-1, null);
    }

    /**
     *
     * @param meshNode
     * @param accessNode
     * @param isRouteToGw
     */
    private void serializeFwdTblMsgs(MeshNode meshNode, AccessNode accessNode, boolean isRouteToGw) {
        List<MeshNode> revPrimPath;
        MeshFwdTblEntry fwdTblEntry;
        MeshNCConfigSetup cfgSetup;
        MeshNCConfigConfirm prereqCfgConf = null;
        OpenFlowMessage ofMsg;
        SerializedMeshMessage serMsg, tmpSerMsg = new SerializedMeshMessage(true);
        boolean isData = (accessNode != null);
        logger.debug("serializeFwdTblMsgs: meshNode[" +
                             HexString.toHexString(meshNode.getOwnBridgeMac()) + "] accessNode[" +
                             ((accessNode == null) ? "null" :
                                     HexString.toHexString(accessNode.getOwnMac())) + "]");

        // Ignore GW node
        if (meshNode.isGwNodeRole()) {
            return;
        }

        // Retrieve VLAN ID
        short vlanId = (isData) ?
                ((isRouteToGw) ? accessNode.getVlanIdUl() : accessNode.getVlanIdDl()) :
                ((isRouteToGw) ? meshNode.getVlanIdUl() : meshNode.getVlanIdDl());

        // Retrieve reverse primary path
        revPrimPath = getReversedPrimaryPath(meshNode, accessNode, isRouteToGw);
        logger.trace("DL reversed: " + revPrimPath.toString());

        // Encode & Serialize FWD TBL entry updates along reverse primary path
        for (MeshNode node : revPrimPath) {

            // Retrieve FWD TBL entry for VLAN ID
            fwdTblEntry = node.getMeshFwdTblEntryByVlanId(vlanId);
            if (fwdTblEntry == null) {
                throw new AssertionError("fwdTblEntry == null");
            }

            // Make sure FWD TBL entry is not already serialized
            if (fwdTblEntry.isSerialized()) {
                throw new AssertionError("FWD TBL entry already serialized");
            }

            // Serialize OF & Mesh updates, if required
            if (fwdTblEntry.isOfUpdateRequired() || fwdTblEntry.isMeshUpdateRequired()) {
                // Create serialized mesh message
                serMsg = new SerializedMeshMessage();

                // Set prerequisites, if any
                if (prereqCfgConf != null) {
                    serMsg.addPrerequisite(prereqCfgConf);
                }

                // Create & serialize Flow Rule update, if required
                if (fwdTblEntry.isOfUpdateRequired()) {
                    ofMsg = new OpenFlowMessage();
                    if (ofMsg.createFlowRule(node, fwdTblEntry, false)) {
                        serMsg.addOpenFlowMsg(ofMsg);
                    }
                }

                // Create & serialize config setup to send & encode FWD TBL IE, if required
                if (fwdTblEntry.isMeshUpdateRequired()) {
                    cfgSetup = new MeshNCConfigSetup(node);
                    if (cfgSetup.encodeFwdTblIe(node, fwdTblEntry) != 0) {
                        serMsg.addMeshMsg(cfgSetup);

                        // Set as prerequisite for subsequent modifications
                        prereqCfgConf = new MeshNCConfigConfirm();
                        prereqCfgConf.setNodeId(cfgSetup.getNodeId());
                        prereqCfgConf.setDeviceId(cfgSetup.getDeviceId());
                        prereqCfgConf.setTid(cfgSetup.getTid());
                    }
                }

                // Add FWD TBL entry to list of serialized entries
                serMsg.addFwdTblEntry(fwdTblEntry);

                // Add to serialized message list
                meshDatabase.addSerializedFwdTblMsg(serMsg);
            }

            // Create & store TMP tunnel update to send after primary path update, if required
            if (fwdTblEntry.isTmpUpdateRequired()) {
                ofMsg = new OpenFlowMessage();
                if (ofMsg.createFlowRule(node, fwdTblEntry, true)) {
                    tmpSerMsg.addOpenFlowMsg(ofMsg);

                    // Add FWD TBL entry to list of serialized entries
                    tmpSerMsg.addFwdTblEntry(fwdTblEntry);
                }
            }

            // Set serialized flag in FWD TBL entry
            fwdTblEntry.setSerialized(true);
        }

        // Serialize TMP tunnel flow rule updates
        if (!tmpSerMsg.getOpenFlowMsgList().isEmpty()) {
            // Set prerequisites, if any
            if (prereqCfgConf != null) {
                tmpSerMsg.addPrerequisite(prereqCfgConf);
            }

            // Add to serialized message list
            meshDatabase.addSerializedFwdTblMsg(tmpSerMsg);
        }

        // Encode & Serialize FWD TBL entry updates for alternate path & for deletes
        serializeFwdTblMsgs(vlanId, prereqCfgConf);
    }

    /**
     *
     */
    private void serializeFwdTblMsgs(short vlanId, MeshNCConfigConfirm prereqCfgConf) {
        MeshNCConfigSetup cfgSetup;
        OpenFlowMessage ofMsg;
        SerializedMeshMessage serMsg = new SerializedMeshMessage();
        SerializedMeshMessage tmpSerMsg = new SerializedMeshMessage(true);
        logger.debug("serializeFwdTblMsgs");

        // Set prerequisite, if any
        if (prereqCfgConf != null) {
            serMsg.addPrerequisite(prereqCfgConf);
            tmpSerMsg.addPrerequisite(prereqCfgConf);
        }

        // Look through all FWD TBL Entries and serialize all pending messages
        for (MeshNode node : meshDatabase.getMeshNodeList()) {
            for (MeshFwdTblEntry entry : node.getMeshFwdTblEntryList()) {

                // Ignore entry if it does not match requested VLAN ID (-1 --> accept all)
                if ((vlanId != -1) && (vlanId != entry.getVlanId())) {
                    continue;
                }

                // Ignore entry if already serialized
                if (entry.isSerialized()) {
                    continue;
                }

                if (entry.isOfUpdateRequired() || entry.isMeshUpdateRequired()) {
                    // Create & serialize Flow Rule update, if required
                    if (entry.isOfUpdateRequired()) {
                        ofMsg = new OpenFlowMessage();
                        if (!ofMsg.createFlowRule(node, entry, false)) {
                            throw new AssertionError("OF update required but encoding failed");
                        }
                        serMsg.addOpenFlowMsg(ofMsg);
                    }

                    // Create & serialize config setup to send & encode FWD TBL IE, if required
                    if (entry.isMeshUpdateRequired()) {
                        cfgSetup = new MeshNCConfigSetup(node);
                        if (cfgSetup.encodeFwdTblIe(node, entry) == 0) {
                            throw new AssertionError("Mesh FWD TBL update required but encoding failed");
                        }
                        serMsg.addMeshMsg(cfgSetup);
                    }

                    // Add FWD TBL entry to list of serialized entries
                    serMsg.addFwdTblEntry(entry);
                }

                // Create & serialize TMP tunnel Flow Rule update, if required
                if (entry.isTmpUpdateRequired()) {
                    ofMsg = new OpenFlowMessage();
                    if (!ofMsg.createFlowRule(node, entry, true)) {
                        throw new AssertionError("TMP OF update required but encoding failed");
                    }
                    tmpSerMsg.addOpenFlowMsg(ofMsg);

                    // Add FWD TBL entry to list of serialized entries
                    tmpSerMsg.addFwdTblEntry(entry);
                }

                // Set serialized flag in FWD TBL entry
                entry.setSerialized(true);
            }
        }

        // Add serialized message to list if at least one message is queued
        if (!serMsg.isEmpty()) {
            meshDatabase.addSerializedFwdTblMsg(serMsg);
        }

        // Add TMP serialized message to list if at least one message is queued
        if (!tmpSerMsg.isEmpty()) {
            meshDatabase.addSerializedFwdTblMsg(tmpSerMsg);
        }
    }

    /**
     * Process serialized messages with no pending prerequisites; send the corresponding
     * Mesh messages.
     */
    private void processSerializedAssocMsgs() {
        List<SerializedMeshMessage> serMsgList = meshDatabase.getSerializedAssocMsgList();
        logger.debug("processSerializedAssocMsgs");

        // Process all serialized messages that are ready to send
        Iterator<SerializedMeshMessage> iterator = serMsgList.iterator();
        while (iterator.hasNext()) {
            SerializedMeshMessage serMsg = iterator.next();

            // If no more prerequisites, do the following:
            //  - Send Mesh messages to switches
            //  - Remove serialized mesh message from list
            if (serMsg.isPrerequisiteListEmpty()) {

                // Process SBI messages to send
                for (MeshMessage msg : serMsg.getMeshMsgList()) {
                    MeshSBIMessage sbiMsg = (MeshSBIMessage)msg;

                    // Retrieve Mesh node to send message to
                    MeshNode node = meshDatabase.getNodeIdMacNodeMap(sbiMsg.getNodeId());
                    if (node == null) {
                        logger.warn("Unable to find mesh node ID: ", sbiMsg.getNodeId());
                        continue;
                    }

                    // Send Mesh message
                    sendMsgToSBI(node.getMeshDeviceId(), sbiMsg);
                }

                // Remove serialized message when processing complete
                iterator.remove();
            }
        }
    }

    /**
     * Process serialized messages with no pending prerequisites; send the corresponding
     * OF & Mesh messages.
     */
    private void processSerializedFwdTblMsgs() {
        FlowRuleService flowRuleService = meshManager.getFlowRuleService();
        List<SerializedMeshMessage> serMsgList = meshDatabase.getSerializedFwdTblMsgList();
        logger.debug("processSerializedFwdTblMsgs");

        // Process all serialized messages that are ready to send
        Iterator<SerializedMeshMessage> iterator = serMsgList.iterator();
        while (iterator.hasNext()) {
            SerializedMeshMessage serMsg = iterator.next();

            // If no more prerequisites, do the following:
            //  - Send Flow Rules to flow rule manager
            //  - Send Mesh messages to switches
            //  - Update FWD TBL Entry states
            //  - Remove serialized mesh message from list
            //  - Reset serialized flag in respective FWD TBL Entries
            if (serMsg.isPrerequisiteListEmpty()) {

                // Send Flow Rules to flow rule manager
                for (OpenFlowMessage ofMsg : serMsg.getOpenFlowMsgList()) {
                    FlowRule flowRule = ofMsg.getFlowRule();
                    logger.debug("FlowRule: " + flowRule.toString());
                    if (ofMsg.getCommand() == OpenFlowMessage.DELETE_FLOW) {
                        flowRuleService.removeFlowRules(flowRule);
                    } else {
                        flowRuleService.applyFlowRules(flowRule);
                    }
                }

                // Process SBI messages to send
                for (MeshMessage msg : serMsg.getMeshMsgList()) {
                    MeshSBIMessage sbiMsg = (MeshSBIMessage)msg;

                    // Retrieve Mesh node to send message to
                    MeshNode node = meshDatabase.getNodeIdMacNodeMap(sbiMsg.getNodeId());
                    if (node == null) {
                        logger.warn("Unable to find mesh node ID: ", sbiMsg.getNodeId());
                        continue;
                    }

                    // Send Mesh message
                    sendMsgToSBI(node.getMeshDeviceId(), sbiMsg);
                }

                // Update FWD TBL entry for OF & Mesh messages that were sent
                for (MeshFwdTblEntry entry : serMsg.getFwdTblEntryList()) {
                    if (serMsg.isTmp()) {
                        // Reset TMP FWD TBL entry, indicating that temp tunnel was deleted
                        entry.setTmpFwdTblEntry(null);
                    } else {
                        // Set FWD TBL status to APPLIED, or delete entry
                        switch (entry.getStatus()) {
                            case MeshFwdTblEntry.STATUS_NEW:
                            case MeshFwdTblEntry.STATUS_MODIFY:
                                entry.setStatus(MeshFwdTblEntry.STATUS_APPLIED);
                                break;
                            case MeshFwdTblEntry.STATUS_DELETE:
                                entry.getParentNode().delMeshFwdTblEntry(entry);
                                break;
                            default:
                                break;
                        }
                    }
                }

                // Remove serialized message when processing complete
                iterator.remove();
            }
        }
    }

    /**
     * Update mesh Topology with latest node & link modifications
     */
    private void updateMeshTopology() {
        if (modifiedGraphSets) {
            DefaultGraphDescription graphDesc = new DefaultGraphDescription(
                    0, 0, meshTopoVertexes, meshTopoLinks);
            meshTopology = new DefaultTopology(ProviderId.NONE, graphDesc);
            modifiedGraphSets = false;
            logger.trace("Vertex/Links sets modified: update topology");
        } else {
            logger.trace("Vertex/Links not modified: do not update topology");
        }
    }

    /**
     * Add link and reverse link to Topology graph, if not already present.
     * This function is called to add temporary Links that were removed during
     * computation of a path, so no update to the metrics of the links.
     * @param link
     * @return
     */
    private boolean addDualLinks(Link link) {
        boolean linksAdded = true;

        // Make sure link is valid
        if (null == link) {
            logger.debug("link == null");
            return false;
        }

        Link reverseLink = DefaultLink.builder()
                .src(link.dst())
                .dst(link.src())
                .providerId(ProviderId.NONE)
                .state(link.state())
                .type(link.type())
                .build();

        // Add original and reverse links
        linksAdded &= addLink(link);
        linksAdded &= addLink(reverseLink);

        // Return true only if both links added
        return linksAdded;
    }


    /**
     * Add link to Topology graph, if not already present
     * @param link
     * @return
     */
    private boolean addLink(Link link) {
        boolean linkAdded = false;
        if (meshTopoLinks.add(link)) {
            modifiedGraphSets = true;
            logger.trace("Graph addLink: " + link.toString());
            linkAdded = true;
        }
        return linkAdded;
    }



    /**
     * Remove link and reverse link from Topology graph, if present.
     * @param link
     * @return
     */
    private boolean removeDualLinks(Link link) {
        boolean linksRemoved = true;

        Link reverseLink = DefaultLink.builder()
                .src(link.dst())
                .dst(link.src())
                .providerId(ProviderId.NONE)
                .state(Link.State.ACTIVE)
                .type(Link.Type.DIRECT)
                .build();

        // Remove original and reverse links
        linksRemoved &= removeLink(link);
        linksRemoved &= removeLink(reverseLink);

        // Return true only if both links removed
        return linksRemoved;
    }

    /**
     * Remove link from Topology graph, if present
     * @param link
     * @return
     */
    private boolean removeLink(Link link) {
        boolean linkRemoved = false;
        if (meshTopoLinks.remove(link)) {
            modifiedGraphSets = true;
            logger.trace("Graph removeLink: " + link.toString());
            linkRemoved = true;
        }
        return linkRemoved;
    }

    /**
     *
     * @param sector
     * @return
     */
    private Link createLink(MeshSector sector, boolean isSrcLink) {
        Link newLink;

        // Make sure sector is valid
        if (sector == null) {
            return null;
        }

        // Create new Device ID using mesh node MAC
        DeviceId newDeviceId;
        try {
            newDeviceId = deviceId(new URI(
                    "temp-mesh", toHex(sector.getParentNode().getOwnBridgeMac()), null));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        // Create new connect point using new node Device ID
        ConnectPoint ownConnectPoint = new ConnectPoint(
                newDeviceId, PortNumber.portNumber((long)sector.getSectorIndex()));

        // Retrieve existing peer connect point
        ConnectPoint peerConnectPoint = meshDatabase.getSectorMacSectorMap(
                sector.getFirstNeighborNode().getPeerSectMac()).getConnectPoint();

        // Create new link
        if (isSrcLink) {
            newLink = DefaultLink.builder()
                    .src(ownConnectPoint)
                    .dst(peerConnectPoint)
                    .providerId(ProviderId.NONE)
                    .state(Link.State.ACTIVE)
                    .type(Link.Type.DIRECT)
                    .build();
        } else {
            newLink = DefaultLink.builder()
                    .src(peerConnectPoint)
                    .dst(ownConnectPoint)
                    .providerId(ProviderId.NONE)
                    .state(Link.State.ACTIVE)
                    .type(Link.Type.DIRECT)
                    .build();
        }
        return newLink;
    }

    /**
     * Get the requested link metric
     * @param link
     * @param type
     * @return
     */
    private long getLinkMetric(Link link, int type) {
        long value = -1;
        switch(type) {
            case LINK_COST_METRIC_NBHOPS:
                value = linkCostMapNbHops.get(link);
                break;
            case LINK_COST_METRIC_BW:
                value = linkCostMapBW.get(link);
                break;
            default:
                break;
        }
        return value;
    }

    /**
     * Set the provided link metric
     * @param link
     * @param type
     * @param value
     */
    private void setLinkMetric(Link link, int type, long value) {
        switch(type) {
            case LINK_COST_METRIC_NBHOPS:
                linkCostMapNbHops.put(link, value);
                break;
            case LINK_COST_METRIC_BW:
                linkCostMapBW.put(link, value);
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param link
     * @param type
     * @return
     */
    private MeshSector getMeshSectorFromLink(Link link, int type) {
        ConnectPoint connectPoint;

        switch(type) {
            case MeshConstants.TYPE_SRC_MAC:
                connectPoint = link.src();
                break;
            case MeshConstants.TYPE_DST_MAC:
            default:
                connectPoint = link.dst();
                break;
        }
        return connectPointSectorMap.get(connectPoint);
    }

    /**
     *
     * @return
     */
    private String printConnectPointSectorMap() {
        String myString = "";
        for (Map.Entry<ConnectPoint, MeshSector> entry : connectPointSectorMap.entrySet()) {
            myString += "\n\tkey[" + entry.getKey().toString() + "] sector[" +
                    HexString.toHexString(entry.getValue().getOwnSectMac()) + "]";
        }
        myString += "\n";
        return myString;
    }

    /**
     *
     * @return
     */
    private String printSectorMacConnectPointMap() {
        String myString = "";
        for(Map.Entry<Long, ConnectPoint> entry : sectorMacConnectPointMap.entrySet()) {
            myString += "\n\tkey[" + HexString.toHexString(entry.getKey()) + "] connectPoint[" +
                    entry.getValue().toString() + "]";
        }
        myString += "\n";
        return myString;
    }

    /**
     * Return reverse primary path from or to the provided leafNode
     * @param leafNode
     * @param accessNode
     * @param isRouteToGw
     * @return
     */
    private List<MeshNode> getReversedPrimaryPath(
            MeshNode leafNode, AccessNode accessNode, boolean isRouteToGw) {
        List<MeshNode> nodeList;
        List<MeshNode> reversedNodeList = new ArrayList<>();
        logger.debug("getReversedPrimaryPath: leafNode[" +
                             HexString.toHexString(leafNode.getOwnBridgeMac()) + "] accessNode[" +
                             ((accessNode == null) ? "null" :
                                     HexString.toHexString(accessNode.getOwnMac())) +
                             "] isRouteToGw[" + isRouteToGw + "]");

        nodeList = getPrimaryPath(leafNode, accessNode, isRouteToGw);
        for (MeshNode node : nodeList) {
            reversedNodeList.add(0, node);
        }
        return reversedNodeList;
    }

    /**
     * Return primary path from or to the provided leafNode
     * @param leafNode
     * @param accessNode
     * @param isRouteToGw
     * @return
     */
    private List<MeshNode> getPrimaryPath(MeshNode leafNode, AccessNode accessNode,
                                          boolean isRouteToGw) {
        MeshNode gwNode = leafNode.getGwNode(), currentNode, endNode;
        List<MeshNode> primaryPath = new ArrayList<>();
        MeshFwdTblEntry entry;
        short vlanId;

        // Retrieve FWD TBL Entry associated to VLAN ID on source node
        if (isRouteToGw) {
            vlanId = (accessNode == null) ? leafNode.getVlanIdUl() : accessNode.getVlanIdUl();
            currentNode = leafNode;
            endNode = gwNode;
        } else {
            vlanId = (accessNode == null) ? leafNode.getVlanIdDl() : accessNode.getVlanIdDl();
            currentNode = gwNode;
            endNode = leafNode;
        }

        // Retrieve source FWD TBL Entry using VLAN ID & direction
        entry = currentNode.getMeshFwdTblEntryByVlanId(vlanId);

        // Follow primary path for VLAN ID and add mesh nodes to list along the way
        while (entry != null) {

            // Add src node to path node list
            primaryPath.add(currentNode);

            // Retrieve primary peer node for FWD TBL Entry
            currentNode = meshDatabase.getNodeIdMacNodeMap(entry.getPrimDstNodeMac());

            // Clear primary path node list and return immediately if peer node not found
            if (currentNode == null) {
                logger.warn("No primary path found for vlan[" + vlanId + "]");
                primaryPath.clear();
                break;
            }

            // If peer node is end node, add it to node list and return primary path
            if (currentNode.equals(endNode)) {
                primaryPath.add(currentNode);
                break;
            }

            // Retrieve next FWD TBL Entry
            entry = currentNode.getMeshFwdTblEntryByVlanId(vlanId);
        }
        return primaryPath;
    }

    /**
     * Get the maximum available BW
     * @return
     */
    private int getMaxAvailBW() {
        // Only allocate up to 80% of sector maximum BW
        return (int)(MeshRoutingManager.MAX_SECTOR_BW * 80 / 100);
    }
}

