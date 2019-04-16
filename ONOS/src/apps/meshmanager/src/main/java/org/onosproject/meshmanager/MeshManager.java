
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

 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Modified;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.ReferenceCardinality;
 import org.apache.felix.scr.annotations.Service;
 import org.onlab.osgi.DefaultServiceDirectory;
 import org.onlab.packet.Ethernet;
 import org.onlab.util.HexString;
 import org.onlab.util.Tools;
 import org.onosproject.cfg.ComponentConfigService;
 import org.onosproject.core.ApplicationId;
 import org.onosproject.core.CoreService;
 import org.onosproject.meshmanager.api.MeshProvider;
 import org.onosproject.meshmanager.api.MeshProviderRegistry;
 import org.onosproject.meshmanager.api.MeshProviderService;
 import org.onosproject.meshmanager.api.message.AlertStateE;
 import org.onosproject.meshmanager.api.message.AlertTypeE;
 import org.onosproject.meshmanager.api.message.AlertsReport;
 import org.onosproject.meshmanager.api.message.BwReq;
 import org.onosproject.meshmanager.api.message.LinkReportInfo;
 import org.onosproject.meshmanager.api.message.LinkStats;
 import org.onosproject.meshmanager.api.message.MeshConstants;
 import org.onosproject.meshmanager.api.message.MeshDeviceAdded;
 import org.onosproject.meshmanager.api.message.MeshDeviceEvent;
 import org.onosproject.meshmanager.api.message.MeshDeviceRemoved;
 import org.onosproject.meshmanager.api.message.MeshMessage;
 import org.onosproject.meshmanager.api.message.MeshNBICreateSliceReq;
 import org.onosproject.meshmanager.api.message.MeshNBICreateSliceRsp;
 import org.onosproject.meshmanager.api.message.MeshNBICustomerDeregistrationReq;
 import org.onosproject.meshmanager.api.message.MeshNBICustomerRegistrationReq;
 import org.onosproject.meshmanager.api.message.MeshNBICustomerRegistrationRsp;
 import org.onosproject.meshmanager.api.message.MeshNBIDeleteSliceReq;
 import org.onosproject.meshmanager.api.message.MeshNBIDeleteSliceRsp;
 import org.onosproject.meshmanager.api.message.MeshNBIGetAlertsReq;
 import org.onosproject.meshmanager.api.message.MeshNBIGetAlertsRsp;
 import org.onosproject.meshmanager.api.message.MeshNBIGetMaxAvailableBWReq;
 import org.onosproject.meshmanager.api.message.MeshNBIGetMaxAvailableBWRsp;
 import org.onosproject.meshmanager.api.message.MeshNBIGetNodeInfoReq;
 import org.onosproject.meshmanager.api.message.MeshNBIGetNodeInfoRsp;
 import org.onosproject.meshmanager.api.message.MeshNBIGetSliceReq;
 import org.onosproject.meshmanager.api.message.MeshNBIGetSliceRsp;
 import org.onosproject.meshmanager.api.message.MeshNBIModifySliceReq;
 import org.onosproject.meshmanager.api.message.MeshNBIModifySliceRsp;
 import org.onosproject.meshmanager.api.message.MeshNBIReadStatsReq;
 import org.onosproject.meshmanager.api.message.MeshNBIReadStatsRsp;
 import org.onosproject.meshmanager.api.message.MeshNBIRequest;
 import org.onosproject.meshmanager.api.message.MeshNBIResponse;
 import org.onosproject.meshmanager.api.message.MeshNCConfigConfirm;
 import org.onosproject.meshmanager.api.message.MeshNCConfigSetup;
 import org.onosproject.meshmanager.api.message.MeshNCConnect;
 import org.onosproject.meshmanager.api.message.MeshNCHello;
 import org.onosproject.meshmanager.api.message.MeshNCReportInd;
 import org.onosproject.meshmanager.api.message.MeshNCReset;
 import org.onosproject.meshmanager.api.message.MeshSBIMessage;
 import org.onosproject.meshmanager.api.message.MeshSBIMessageFactory;
 import org.onosproject.meshmanager.api.message.NeighborList;
 import org.onosproject.meshmanager.api.message.NodeInfo;
 import org.onosproject.meshmanager.api.message.PriorityConfig;
 import org.onosproject.meshmanager.api.message.ResultCode;
 import org.onosproject.meshmanager.api.message.ResultCodeE;
 import org.onosproject.meshmanager.api.message.Sla;
 import org.onosproject.meshmanager.api.message.SliceInfo;
 import org.onosproject.meshmanager.api.message.StatsConfig;
 import org.onosproject.meshmanager.api.message.StatsReport;
 import org.onosproject.net.ConnectPoint;
 import org.onosproject.net.Device;
 import org.onosproject.net.DeviceId;
 import org.onosproject.net.Link;
 import org.onosproject.net.Port;
 import org.onosproject.net.device.DeviceAdminService;
 import org.onosproject.net.device.DeviceEvent;
 import org.onosproject.net.device.DeviceListener;
 import org.onosproject.net.device.DeviceService;
 import org.onosproject.net.flow.FlowEntry;
 import org.onosproject.net.flow.FlowRuleService;
 import org.onosproject.net.flow.FlowRuleStore;
 import org.onosproject.net.flow.criteria.Criterion;
 import org.onosproject.net.flow.criteria.VlanIdCriterion;
 import org.onosproject.net.flow.instructions.Instruction;
 import org.onosproject.net.flow.instructions.L2ModificationInstruction;
 import org.onosproject.net.group.GroupService;
 import org.onosproject.net.link.DefaultLinkDescription;
 import org.onosproject.net.link.LinkDescription;
 import org.onosproject.net.link.LinkEvent;
 import org.onosproject.net.link.LinkListener;
 import org.onosproject.net.link.LinkProvider;
 import org.onosproject.net.link.LinkProviderRegistry;
 import org.onosproject.net.link.LinkProviderService;
 import org.onosproject.net.link.LinkService;
 import org.onosproject.net.packet.InboundPacket;
 import org.onosproject.net.packet.PacketContext;
 import org.onosproject.net.packet.PacketProcessor;
 import org.onosproject.net.packet.PacketService;
 import org.onosproject.net.provider.AbstractListenerProviderRegistry;
 import org.onosproject.net.provider.AbstractProvider;
 import org.onosproject.net.provider.AbstractProviderService;
 import org.onosproject.net.provider.ProviderId;
 import org.onosproject.net.statistic.StatisticStore;
 import org.onosproject.net.topology.TopologyService;
 import org.osgi.service.component.ComponentContext;
 import org.slf4j.Logger;

 import java.nio.ByteBuffer;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.atomic.AtomicInteger;

 import static com.google.common.base.Strings.isNullOrEmpty;
 import static java.lang.Math.abs;
 import static org.onlab.util.Tools.groupedThreads;
 import static org.slf4j.LoggerFactory.getLogger;

/**
 * Provides basic implementation of the mesh SB & NB APIs.
 */
@Component(immediate = true)
@Service
public class MeshManager
        extends AbstractListenerProviderRegistry<MeshEvent, MeshListener, MeshProvider, MeshProviderService>
        implements MeshService, MeshProviderRegistry {

    private static final String MESH_APP_ID = "org.onosproject.mesh";
    private static final String SCHEME = "of";
    private static final String DEFAULT_REQSLA = "<EMPTY>";
    private static final String DEFAULT_SPGW_MAPPING = "<EMPTY>";
    private static final long DEFAULT_SPGW_ADDRESS = 0;
    private static final long DEFAULT_INTERSTAT_TIME_DIFF = 5;
    private static final byte CONTROLLER_VERSION = 1;

    private static ProviderId providerId = null;
    private static LinkProviderService linkProviderService = null;
    private static MeshLinkProvider meshLinkProvider = null;

    private final Logger logger = getLogger(getClass());
    private final DeviceListener deviceListener = new InternalDeviceListener();
    private final LinkListener linkListener = new InternalLinkListener();
    private final InternalPacketProcessor processor = new InternalPacketProcessor();

    @Property(name = "spgw", longValue = DEFAULT_SPGW_ADDRESS,
              label = "MAC Address of the SP-GW (Hex(0xABCDABCD) or MAC(XX:XX:XX:XX:XX) " +
                      "format), 0 if NC is one hop away from GW")
    private long spgw = DEFAULT_SPGW_ADDRESS;
    @Property(name = "reqSLA", value = DEFAULT_REQSLA,
              label = "SLA requirements and static vlan settings, (supports Hex(0xABCDABCD) " +
                      "or MAC(XX:XX:XX:XX:XX) format)")
    private String reqSla = DEFAULT_REQSLA;
    @Property(name = "spgwMapping", value = DEFAULT_SPGW_MAPPING,
              label = "SPGW bridge MAC and Mesh bridge/sector MAC mapping, (supports " +
                      "Hex(0xABCDABCD) or MAC(XX:XX:XX:XX:XX) format)")
    private String spgwMapping = DEFAULT_SPGW_MAPPING;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceAdminService deviceAdminService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected GroupService groupService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleStore flowRuleStore;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StatisticStore statisticStore;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService cfgService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkProviderRegistry linkProviderRegistry;

    private ApplicationId appId;
    private byte version;
    private NbiNotificationListener nbiNotificationListener;
    private MeshProviderService providerService;
    private ExecutorService executor;
    private BlockingQueue<MeshMessage> messageQ;
    private MeshMessageQueue messageQueueRunnable;
    private MeshDB meshDatabase = new MeshDB(this);
    private MeshRoutingManager meshRoutingManager = new MeshRoutingManager(this, meshDatabase);
    private MeshSliceManager meshSliceManager = new MeshSliceManager(meshRoutingManager, meshDatabase);

    private final Object nbiLock = new Object();
    private MeshNBIResponse lastNBIResponse;

    private short nextAvailableNbiSessionId = MeshConstants.SLICE_MANAGER_FIRST_AVAILABLE_SESSION_ID;
    private Map<Long, CustomerSession> customerIdCustomerSessionMap = new HashMap<>();
    private Map<Short, CustomerSession> sessionIdCustomerSessionMap = new HashMap<>();


    /**
     * Activate component
     * @param context
     */
    @Activate
    public void activate(ComponentContext context) {
//        store.setDelegate(delegate);
        appId = coreService.registerApplication(MESH_APP_ID);
        eventDispatcher.addSink(MeshEvent.class, listenerRegistry);
        cfgService.registerProperties(getClass());
        version = CONTROLLER_VERSION;

        // Start main execution thread
        executor = Executors.newSingleThreadExecutor(groupedThreads("onos/mesh", "build-%d", logger));
        messageQ = new LinkedBlockingQueue<>();
        messageQueueRunnable = new MeshMessageQueue();
        executor.execute(messageQueueRunnable);

        // Add listeners
        deviceService.addListener(deviceListener);
        linkService.addListener(linkListener);
        packetService.addProcessor(processor, PacketProcessor.director(0));

        if (linkProviderService == null) {
            meshLinkProvider = new MeshLinkProvider();
//            linkProviderRegistry = get(LinkProviderRegistry.class);
            linkProviderService = linkProviderRegistry.register(meshLinkProvider);
        }

        // Read configuration
        modified(context);
        logger.info("Started");
    }

    /**
     * Deactivate component
     * @param context
     */
    @Deactivate
    public void deactivate(ComponentContext context) {
//        store.unsetDelegate(delegate);
        eventDispatcher.removeSink(MeshEvent.class);
        cfgService.unregisterProperties(getClass(), false);

        // Remove listeners
        deviceService.removeListener(deviceListener);
        linkService.removeListener(linkListener);
        packetService.removeProcessor(processor);

        // Stop main execution thread
        executor.shutdownNow();
        executor = null;

        logger.info("Stopped");
    }

    /**
     * Process component configuration change
     * @param context
     */
    @Modified
    public void modified(ComponentContext context) {
        readComponentConfiguration(context);
    }

    /**
     * Personalized host provider service issued to the supplied provider.
     * @param provider provider
     * @return
     */
    @Override
    protected MeshProviderService createProviderService(MeshProvider provider) {
        logger.debug("creation of internal mesh provider service");
        return new InternalMeshProviderService(provider, this);
    }

    /**
     *
     * @return
     */
    @Override
    public MeshRoutingManager getMeshRoutingManager() {
        return meshRoutingManager;
    }

    /**
     *
     * @return
     */
    @Override
    public MeshDB getMeshDB() {
        return meshDatabase;
    }

    /**
     *
     * @return
     */
    @Override
    public MeshSliceManager getMeshSliceManager() {
        return meshSliceManager;
    }

    /**
     *
     * @param req NBI Request message
     * @return
     */
    @Override
    public MeshNBIResponse processNBIRequest(MeshNBIRequest req) {
        synchronized(nbiLock) {
            // Post message for MeshManager processing
            try {
                messageQ.put(req);
            } catch (Exception e) {
                return null;
            }

            // Wait for processing to complete
            try {
                nbiLock.wait(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
            return lastNBIResponse;
        }
    }

    /**
     *
     * @param msg
     * @return
     */
    @Override
    public boolean addToMsgQ(MeshMessage msg) {
        try {
            messageQ.put(msg);
        } catch (Exception e) {
            return false;
        }
        return true; 
    }

    /**
     *
     * @return
     */
    public FlowRuleService getFlowRuleService() {
        return flowRuleService;
    }

    /**
     *
     * @param deviceId
     * @return
     */
    public MeshProvider getMeshProvider(DeviceId deviceId) {
        MeshProvider provider = null;
        if (deviceId != null) {
            provider = getProvider(deviceId);
            if (provider == null) {
                logger.debug("Provider not found for {}", deviceId);
            }
        } else {
            logger.info("Device Id is null");
        }
        return provider;
    }

    /**
     *
     * @return
     */
    public TopologyService getTopologyService() {
        if (topologyService == null) {
            logger.trace("topo null");
            topologyService = DefaultServiceDirectory.getService(TopologyService.class);
        }
        return topologyService;
    }

    /**
     *
     * @param nbiNotificationListener
     */
    public void registerNbiNotificationListener(NbiNotificationListener nbiNotificationListener) {
        logger.info("Registering NbiNotificationListener");
        this.nbiNotificationListener = nbiNotificationListener;
    }

    /**
     *
     */
    public void unregisterNbiNotificationListener() {
        logger.info("Unregistering NbiNotificationListener");
        nbiNotificationListener = null;
    }

    /**
     * Remove Mesh Node from Topology graph, if present
     * Remove Mesh Sector connect points
     * @param meshNode
     * @return
     */
    public void removeNode(MeshNode meshNode) {

        // Remove serialized ASSOC messages for provided mesh node
        List<SerializedMeshMessage> serMsgsToRemove = new ArrayList<>();
        for (SerializedMeshMessage serMsg : meshDatabase.getSerializedAssocMsgList()) {
            // Remove serialized ASSOC messages
            for (MeshMessage msg : serMsg.getMeshMsgList()) {
                MeshNCConfigSetup cfgSetup = (MeshNCConfigSetup)msg;
                if (cfgSetup.getNodeId() == meshNode.getNodeId()) {
                    serMsgsToRemove.add(serMsg);
                    break;
                }
            }

            // Remove prerequisites with provided mesh node ID
            serMsg.removePrerequisite(meshNode.getNodeId());
        }
        for (SerializedMeshMessage serMsg : serMsgsToRemove) {
            meshDatabase.removeSerializedAssocMsg(serMsg);
        }

        // Remove Mesh Node from routing manager & Topology graph
        meshRoutingManager.removeMeshNode(meshNode);

        // Reset all access node routes for provided mesh node
        // Add all access nodes to pending list in slice manager
        meshSliceManager.addPendingAccessNodes(meshNode.getOwnBridgeMac(),
                                               meshNode.getAccessNodeList());
        meshNode.getAccessNodeList().clear();

        // Remove device flow rules
        if (meshNode.getAssociatedDeviceId() != null) {
            flowRuleService.purgeFlowRules(meshNode.getAssociatedDeviceId());
        }

        // Remove mesh & associated device
        disconnectAssociatedDevice(meshNode.getAssociatedDeviceId());
        meshNode.setAssociatedDeviceId(null);
        disconnectMeshDevice(meshNode.getMeshDeviceId());
        meshNode.setMeshDeviceId(null);

        // Remove mesh node, sectors from DB
        meshDatabase.removeMeshNode(meshNode);
    }

    /**
     * Extracts properties from the component configuration context.
     *
     * @param context the component context
     */
    private void readComponentConfiguration(ComponentContext context) {

        if (context == null) {
            meshRoutingManager.setSPGW(spgw);
            parseReqSla(reqSla);
            meshRoutingManager.setSPGWFakeBridgeMapping(spgwMapping);
            return;
        }

        Dictionary properties = context.getProperties();
        long newSpgw;
        String newReqSla;
        String newSpgwMapping;

        try {
            String s = Tools.get(properties, "spgw");
            s = s.trim();
            s = s.replace(":", "");
            s = s.replace("x", "");
            s = s.replace("X", "");
            newSpgw = isNullOrEmpty(s) ? spgw : Tools.fromHex(s);

        } catch (NumberFormatException | ClassCastException e) {
            return;
        }

        if (newSpgw == spgw) {
            logger.info("Spgw address has not changed, using current value of {}",
                        HexString.toHexString(spgw));
        } else {
            spgw = newSpgw;
            meshRoutingManager.setSPGW(spgw);
            logger.info("Configured. Pending spgw address is configured to {}",
                        HexString.toHexString(spgw));
        }

        try {
            String s = Tools.get(properties, "reqSLA");
            s = s.trim();
            s = s.replace(":", "");
            s = s.replace("x", "");
            s = s.replace("X", "");
            newReqSla = s;
        } catch (NumberFormatException | ClassCastException e) {
            return;
        }

        if (newReqSla.equals(reqSla)) {
            logger.info("ReqSla has not changed, using current value of {}", reqSla);
        } else {
            reqSla = newReqSla;
            parseReqSla(reqSla);
            logger.info("Configured. Pending reqSla is configured to {}", reqSla);
        }

        try {
            String s = Tools.get(properties, "spgwMapping");
            s = s.trim();
            s = s.replace(":", "");
            s = s.replace("x", "");
            s = s.replace("X", "");
            newSpgwMapping = s;
            meshRoutingManager.setSPGWFakeBridgeMapping(newSpgwMapping);

        } catch (NumberFormatException | ClassCastException e) {
            return;
        }

        if (newSpgwMapping.equals(spgwMapping)) {
            logger.info("spgwMapping has not changed, using current value of {}", spgwMapping);
        } else {
            spgwMapping = newSpgwMapping;
            meshRoutingManager.setSPGWFakeBridgeMapping(spgwMapping);
            logger.info("Configured. Pending spgwMapping is configured to {}", spgwMapping);
        }
    }

    /**
     * Determine if message is for NBI Request, SBI Message or Mesh Device Event
     * @param msg
     */
    private synchronized void handleAllMessages(MeshMessage msg) {
        logger.debug("handleAllMessages: " + msg.toString());

        // Process Mesh Message
        if (msg instanceof MeshSBIMessage) {
            handleSBIMessage((MeshSBIMessage)msg);
        } else if (msg instanceof MeshNBIRequest) {
            handleNBIRequest((MeshNBIRequest)msg);
        } else if (msg instanceof MeshDeviceEvent) {
            handleDeviceEvent((MeshDeviceEvent)msg);
        } else {
            throw new AssertionError("Unsupported MeshMessage");
        }
    }

    /**
     * Process SBI Message by invoking corresponding SBI message handler
     * @param msg
     */
    private void handleSBIMessage(MeshSBIMessage msg) {
        logger.info("handleSBIMessage");

        // Process SBI Message
        if (msg instanceof MeshNCConnect) {
            processNcConnect((MeshNCConnect)msg);
        } else if (msg instanceof MeshNCHello) {
            processNcHello((MeshNCHello)msg);
        } else if (msg instanceof MeshNCConfigConfirm) {
            processNcConfigConfirm((MeshNCConfigConfirm)msg);
        } else if (msg instanceof MeshNCReportInd) {
            processNcReportInd((MeshNCReportInd)msg);
        } else if (msg instanceof MeshNCReset) {
            processNcReset((MeshNCReset)msg, false);
        } else {
            throw new AssertionError("Unsupported SBI Message");
        }
    }

    /**
     * NC_CONNECT message handler
     * NOTE: GW does not send an NC_CONNECT. GW Node is created when GW device is added.
     * @param msg
     */
    private void processNcConnect(MeshNCConnect msg) {
        logger.info("NC_CONNECT");
        int version;
        long nodeId, nodeMac, ownSectMac, peerSectMac;
        short ownSectPortIndex;
        MeshNode ownNode;
        MeshSector ownSector, peerSector;
        NeighborNode neighNode;
        meshDatabase.dumpDB(MeshConstants.TRACE);

        // Retrieve version & node MAC from NC_CONNECT
        // Set Node ID to node MAC
        version = msg.getVersion();
        nodeMac = msg.getBridgeMacAddr();
        nodeId = nodeMac;

        // Retrieve Mesh Node using node ID (if it already exists; null otherwise)
        ownNode = meshDatabase.getNodeIdMacNodeMap(nodeId);

        // Retrieve own sector MAC from NC_CONNECT
        ownSectMac = msg.getOwnSector();
        if (ownSectMac == 0) {
            throw new AssertionError("ownSectMac == 0");
        }

        // Retrieve peer sector MAC from NC_CONNECT
        peerSectMac = msg.getPeerSector();
        if (peerSectMac == 0) {
            throw new AssertionError("peerSectMac == 0");
        }

        // Retrieve port information from NC_CONNECT
        ownSectPortIndex = msg.getOwnSectPortIndex();

        // Retrieve peer mesh sector using peer sector MAC
        peerSector = meshDatabase.getSectorMacSectorMap(peerSectMac);
        if (peerSector == null) {
            throw new AssertionError("peerSector == null");
        }

        // NC_CONNECT should be rejected right away if:
        //   1) coming from a non-PCP sector
        //   2) coming from a non-connected node
        if (peerSector.getSectorPersonality() != MeshSector.MESH_PERSONALITY_PCP_MODE) {
            throw new AssertionError("Received NC_CONNECT on a non-PCP sector");
        }
        if (peerSector.getParentNode().getNodeStatus() != MeshNode.CONNECTED_CONFIRMED_COMPLETE) {
            throw new AssertionError("Received NC_CONNECT on a non-connected node");
        }

        // If Mesh Node already exists, one of the following scenarios occurred:
        //   1) Connection retry timer expiry triggered an NC_CONNECT retransmission
        //   2) Failed final association triggered a new NC_CONNECT on a new temp assoc
        // In both cases, we must remove the mesh node and process the request as a new one.
        if (ownNode != null) {
            logger.trace("Second NC_CONNECT received for node: " + HexString.toHexString(nodeMac));
            removeNode(ownNode);
            meshDatabase.dumpDB(MeshConstants.TRACE);
        }

        // Create new mesh node
        ownNode = createMeshNode(nodeMac, nodeId);

        // Add new mesh sector
        logger.trace("Adding mesh sector: mac[" + HexString.toHexString(ownSectMac) + "] index[" +
                             ownSectPortIndex + "] ");
        ownSector = new MeshSector(ownSectMac, ownSectPortIndex, ownNode, meshRoutingManager);
        meshDatabase.addMeshSector(ownNode, ownSector);
        meshDatabase.dumpDB(MeshConstants.TRACE);

        // Set temporary association mesh sector
        ownNode.setTmpAssocMeshSect(ownSector);

        // Create and add neighbor nodes
        neighNode = new NeighborNode(ownSectMac, peerSector);
        peerSector.addNeighbor(neighNode);
        neighNode = new NeighborNode(peerSectMac, ownSector);
        ownSector.addNeighbor(neighNode);

        // Add mesh link on peer PCP sector
        logger.trace("Adding MeshLink on peer PCP " + HexString.toHexString(peerSectMac) +
                             " from own STA " + HexString.toHexString(ownSectMac));
        peerSector.createMeshLink(ownSectMac);

        // Update FWD TBL entries and send required OF & Mesh messages
        meshRoutingManager.processFwdTblUpdates(ownNode, null, true);

        logger.trace("NC Connect processing done");
        meshDatabase.dumpDB(MeshConstants.DEBUG);
    }

    /**
     * NC_HELLO message handler
     * @param msg
     */
    private void processNcHello(MeshNCHello msg) {
        boolean isGwNode;
        long nodeMac, sectorMac, nodeId;
        short sectorPortIndex;
        MeshSector tmpAssocSector;
        MeshNode ownNode;
        NeighborNode tmpAssocNeighNode, finalAssocNeighNode;
        logger.debug("NC_HELLO received");
        meshDatabase.dumpDB(MeshConstants.TRACE);

        // Retrieve node MAC from NC_HELLO. Set Node ID to node MAC.
        nodeMac = msg.getBridgeMacAddr();
        nodeId = nodeMac;

        // Retrieve mesh node
        ownNode = meshDatabase.getNodeIdMacNodeMap(nodeId);
        if (ownNode == null) {
            throw new AssertionError("Unable to find mesh node for NC_HELLO node ID: " +
                                             HexString.toHexString(nodeId));
        }
        if (ownNode.getNodeStatus() != MeshNode.CONNECTING) {
            throw new AssertionError("Invalid Mesh Node status during NC_HELLO: " +
                                             ownNode.getNodeStatus());
        }
        logger.trace("Node Bridge MAC: " + HexString.toHexString(ownNode.getOwnBridgeMac()));

        // Copy the capInfo content into the mesh node and retrieve node role
        ownNode.addCapabilityInfo(msg.getCapabilityInfo());
        isGwNode = ownNode.isGwNodeRole();

        // Update sig & data port index, mesh device ID
        ownNode.setSignalingPortIndex(msg.getSignalingPortIndex());
        ownNode.setDataPortIndex(msg.getDataPortIndex());
        ownNode.setMeshDeviceId(msg.getDeviceId());

        // Update node status. For GW node, simulate FINAL association is done.
        ownNode.setNodeStatus((isGwNode) ? MeshNode.CONNECTED_CONFIRMED_SINGLE_SECTOR :
                                      MeshNode.CONNECTED_NOT_CONFIRMED);

        // Add Mesh Node to Mesh Routing DB
        meshRoutingManager.addNode(ownNode);
        meshDatabase.dumpDB(MeshConstants.TRACE);

        // If GW node, add self rule and delete FLOOD rule
        if (isGwNode) {
            meshRoutingManager.sendDefaultFromGWSelfFlow(ownNode);
            meshRoutingManager.sendDefaultFlood(ownNode, MeshFwdTblEntry.DELETE);
        }

        // Add sectors provided in NC_HELLO
        for (NeighborList neighList : msg.getNeighborList()) {

            // Retrieve sector MAC & port
            sectorMac = neighList.getOwnSectMac();
            sectorPortIndex = neighList.getOwnSectPortIndex();

            // Add sector to DB, if not already present
            MeshSector sector = meshDatabase.getSectorMacSectorMap(sectorMac);
            if (sector == null) {
                logger.trace("Adding mesh sector: mac[" + HexString.toHexString(sectorMac) +
                                     "] index[" + sectorPortIndex + "] ");
                sector = new MeshSector(sectorMac, sectorPortIndex, ownNode, meshRoutingManager);
                meshDatabase.addMeshSector(ownNode, sector);
            }

            // Add sector to Mesh Routing DB
            meshRoutingManager.addMeshSector(ownNode, sector);

            // Add potential neighbors for sector
            for (NeighborNode neighNode : neighList.getNeighbors()) {
                neighNode.setParentSector(sector);
                sector.addPotentialNeighbor(neighNode);
            }
        }
        meshDatabase.dumpDB(MeshConstants.TRACE);

        // If GW node, immediately process all sectors. Otherwise, select and configure final assoc.
        if (isGwNode) {
            for (MeshSector sector : ownNode.getMeshSectList()) {
                // Determine next neighbor to associate with, if any
                processPotentialNeighbors(sector);

                // Send ASSOC CFG setup message
                sendAssocCfg(ownNode, sector);
            }
        } else {
            // Retrieve temp association sector & Neigh node
            tmpAssocSector = ownNode.getTmpAssocMeshSect();
            tmpAssocNeighNode = tmpAssocSector.getFirstNeighborNode();

            // Determine next neighbor to associate with for final association
            processPotentialNeighbors(tmpAssocSector);

            // Make sure final assoc neighbor is selected
            if (tmpAssocSector.getNeighborNodeList().isEmpty()) {
                throw new AssertionError("Final neighbor not determined for NGW node");
            }

            // Send ASSOC CFG setup message
            sendAssocCfg(ownNode, tmpAssocSector);

            // Update FWD TBL entries only if final != temp assoc
            finalAssocNeighNode = tmpAssocSector.getFirstNeighborNode();
            if (!finalAssocNeighNode.equals(tmpAssocNeighNode)) {
                // Remove associated device ID. It will reconnect on final association.
                ownNode.setAssociatedDeviceId(null);

                // Update FWD TBL entries and send required OF & Mesh messages
                meshRoutingManager.processFwdTblUpdates(ownNode, null, true);
            }
        }
    }

    /**
     *
     * @param msg
     */
    private void processNcConfigConfirm(MeshNCConfigConfirm msg) {
        MeshNode meshNode;
        ResultCode resultCode;

        Byte cfgType;
        logger.trace("NC_CONFIG_CONFIRM received");

        // Retrieve Mesh Node
        meshNode = meshDatabase.getNodeIdMacNodeMap(msg.getNodeId());
        if (meshNode == null) {
            logger.error("meshNode == null");
            return;
        }

        // Make sure transaction ID was expected and valid
        cfgType = meshNode.removeTid(msg.getTid());
        if (cfgType == null) {
            logger.error("Ignoring unexpected TID: " + Integer.toHexString(msg.getTid()));
            return;
        }

        // Retrieve result code (currently only 1 per CONFIG_CNF)
        resultCode = msg.getFirstResultCode();
        logger.trace("result code: " + resultCode.getResult() + " (" +
                             resultCode.getResultString() + ") for " +
                             HexString.toHexString(resultCode.getSectMac()));

        // Process CONFIG_CNF based on TID type
        if ((cfgType & MeshNCConfigSetup.CFG_TYPE_ASSOC) != 0) {
            processAssocCfgCnf(meshNode, resultCode);
        }
        if ((cfgType & MeshNCConfigSetup.CFG_TYPE_FWD_TBL) != 0) {
            processFwdTblCfgCnf(meshNode, resultCode, msg);
        }
        if ((cfgType & MeshNCConfigSetup.CFG_TYPE_LINK_REPORT) != 0) {
            processLinkReportCfgCnf(meshNode, resultCode);
        }
        if ((cfgType & MeshNCConfigSetup.CFG_TYPE_BUFFER_REPORT) != 0) {
            processBufferReportCfgCnf(meshNode, resultCode);
        }
        if ((cfgType & MeshNCConfigSetup.CFG_TYPE_SLA) != 0) {
            processSlaCfgCnf(meshNode, resultCode);
        }
    }

    /**
     * NC_REPORT_IND handler
     * @param ind
     */
    private void processNcReportInd(MeshNCReportInd ind) {
        logger.debug("NC_REPORT_IND received, reportCode = " + ind.getReportCode() +
                             "-" + ind.getReportCodeString());

        // Handle Indication Message based on Report Code
        switch (ind.getReportCode()) {
            case MeshNCReportInd.REPORT_LINK_FAILURE:
                logger.debug("LINK_FAILURE received");
                processLinkFailure(ind);
                break;
            case MeshNCReportInd.REPORT_NL_UPDATE:
                logger.debug("NL_UPDATE received");
                processNLUpdate(ind);
                break;
            case MeshNCReportInd.REPORT_PERIODIC:
                logger.debug("PERIODIC_REPORT received");
                processPeriodicReport(ind);
                break;
            case MeshNCReportInd.REPORT_SCHED_FAILURE:
            case MeshNCReportInd.REPORT_NEW_NODE:
            default:
                break;
        }
        logger.trace("NC_REPORT_IND processing done");
    }

    /**
     *
     * @param msg
     */
    private void processNcReset(MeshNCReset msg, boolean isThisFaked) {
        logger.trace("NC RESET message received");
        meshDatabase.dumpDB(MeshConstants.DEBUG);

        // Ignore NC RESET if already processed
        if (!msg.isInternallyGenerated() && (meshDatabase.getLastResetId() == msg.getResetId())) {
            logger.trace("Duplicate resetId (" + msg.getResetId() + "), ignoring...");
            return;
        }

        logger.trace("Resetting the mesh controller");

        // Sleep for 1 second to allow all the mesh nodes to process the stop on their end.
        // This is specific to VTB test code to give enough time to the mesh nodes to send the
        // information to the TC.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Flush all mesh device connections
        for (MeshNode meshNode : meshDatabase.getMeshNodeList()) {
            disconnectMeshDevice(meshNode.getMeshDeviceId());
        }

        // Flush OF device flow rules
        Iterable<Device> devices = deviceService.getAvailableDevices();
        for (Device device : devices) {
            flowRuleService.purgeFlowRules(device.id());
        }
        flowRuleStore.purgeFlowRules();

        // Flush remaining device flow rules
        for (Device device : deviceAdminService.getDevices()) {
            flowRuleService.purgeFlowRules(device.id());
        }

        // Flush device groups
        for (Device device : deviceAdminService.getDevices()) {
            groupService.purgeGroupEntries(device.id());
        }

        // Flush devices
        while (deviceAdminService.getDeviceCount() > 0) {
            try {
                for (Device device : deviceAdminService.getDevices()) {
                    deviceAdminService.removeDevice(device.id());
                }
            } catch (Exception e) {
                logger.info("Unable to wipe-out devices", e);
            }
        }

        // Reset blocking message queue
        executor.shutdownNow();
        messageQueueRunnable.stop();
        messageQ.clear();

        if(isThisFaked == false) {
            logger.trace("Resetting the mesh controller with resetId " + msg.getResetId());
            //clear the sessions already registered since it is a forced NC RESET from vtb
            for (Entry<Long, CustomerSession>entry : customerIdCustomerSessionMap.entrySet()) {
                entry.getValue().terminateCustomerSession();
            }
            customerIdCustomerSessionMap.clear();
            sessionIdCustomerSessionMap.clear();
            // Reset session ID counter
            nextAvailableNbiSessionId = MeshConstants.SLICE_MANAGER_FIRST_AVAILABLE_SESSION_ID;
            meshSliceManager = new MeshSliceManager(null, null);
        }
        else {
            logger.trace("This NC RESET is faked");
            //all the flow rules were purged, all the nodes will disappear... 
            //we need to put the access node in a pending list since the sliceManager and the sessions to the orchestrator stay as valid ones
            for (MeshNode meshNode : meshDatabase.getMeshNodeList()) {

                for (AccessNode accessNode : meshNode.getAccessNodeList()) {
                    meshSliceManager.addPendingAccessNodes(meshNode.getOwnBridgeMac(),
                                                           meshNode.getAccessNodeList());
                }     
            }
        } 

        // Creating new objects and letting the garbage collector destroy old ones
        meshDatabase = new MeshDB(this);
        meshRoutingManager = new MeshRoutingManager(this, meshDatabase);
        meshSliceManager.setConstructorPointers(meshRoutingManager, meshDatabase);

        // Read the configuration
        modified(null);

        executor = Executors.newSingleThreadExecutor(
                groupedThreads("onos/mesh", "build-%d", logger));
        messageQueueRunnable = new MeshMessageQueue();
        executor.execute(messageQueueRunnable);

        // Store Reset ID to prevent duplicate requests
        if (!msg.isInternallyGenerated()) {
            logger.trace("Resetting the mesh controller with resetId " + msg.getResetId());
            meshDatabase.setLastResetId(msg.getResetId());
        }
    }

    /**
     *
     * @param resultCode
     */
    private void processAssocCfgCnf(MeshNode meshNode, ResultCode resultCode) {
        MeshSector ownSector, peerSector;
        long ownSectorMac, peerSectorMac = 0;
        logger.trace("Processing ASSOC_CFG response");

        // Retrieve the result code mesh sector & MAC
        ownSector = meshDatabase.getSectorMacSectorMap(resultCode.getSectMac());
        if (ownSector == null) {
            throw new AssertionError("No valid mesh sector for mac: " +
                                             HexString.toHexString(resultCode.getSectMac()));
        }
        ownSectorMac = ownSector.getOwnSectMac();

        // Make sure mesh sector is not confirmed
        if (ownSector.isConfirmed()) {
            throw new AssertionError("Sector is already confirmed");
        }

        // Process result
        switch (resultCode.getResult()) {

            // RESULT_SUCCESS
            case ResultCode.RESULT_SUCCESS: {
                // Process successful config confirm
                logger.trace("Confirming sector: " + HexString.toHexString(ownSectorMac));

                // Clear potential neighbor list
                ownSector.clearPotentialNeighborList();

                // Set new sector state
                switch (ownSector.getSectorState()) {
                    case MeshSector.STATE_UNCONFIRMED_NO_ASSOC: {
                        // Successfully confirmed STA/0 or PCP/0
                        ownSector.setSectorState(MeshSector.STATE_CONFIRMED_NO_ASSOC);
                        break;
                    }
                    case MeshSector.STATE_UNCONFIRMED_ASSOC: {
                        // Make sure mesh sector is STA
                        if (ownSector.getSectorPersonality() != MeshSector.MESH_PERSONALITY_STA_MODE) {
                            throw new AssertionError("TMP ASSOC sector not in STA");
                        }

                        // Successfully confirmed STA with neighbor
                        peerSectorMac = ownSector.getFirstNeighborNode().getPeerSectMac();
                        peerSector = meshDatabase.getSectorMacSectorMap(peerSectorMac);

                        ownSector.setSectorState(MeshSector.STATE_CONFIRMED_ASSOC);
                        peerSector.setSectorState(MeshSector.STATE_CONFIRMED_ASSOC);
                        break;
                    }
                    default:
                        throw new AssertionError("Unsupported sector state: " + ownSector.getSectorState());
                }

                // NOTE: Add links to topology only when all sectors are confirmed

                // Process CONFIG CNF based on node state
                switch (meshNode.getNodeStatus()) {
                    case MeshNode.CONNECTED_NOT_CONFIRMED: {
                        // Update node state
                        logger.trace("NODE is confirmed for Single Assoc");
                        meshNode.setNodeStatus(MeshNode.CONNECTED_CONFIRMED_SINGLE_SECTOR);

                        // Process all other sector associations
                        for (MeshSector sector : meshNode.getMeshSectList()) {
                            // Ignore current sector
                            if (sector.equals(ownSector)) {
                                continue;
                            }

                            // Determine next neighbor to associate with, if any
                            processPotentialNeighbors(sector);

                            // Send ASSOC CFG setup message
                            sendAssocCfg(meshNode, sector);
                        }

                        // If all sectors are confirmed, move to COMPLETED state
                        if (meshNode.isSectorsConfirmed()) {
                            processConfirmedComplete(meshNode);
                        }
                        break;
                    }
                    case MeshNode.CONNECTED_CONFIRMED_SINGLE_SECTOR: {
                        // If all sectors are confirmed, move to COMPLETED state
                        if (meshNode.isSectorsConfirmed()) {
                            processConfirmedComplete(meshNode);
                        }
                        break;
                    }
                    case MeshNode.CONNECTED_CONFIRMED_COMPLETE: {

                        switch (ownSector.getSectorState()) {
                            case MeshSector.STATE_CONFIRMED_NO_ASSOC:
                                // Nothing to do when STA/0 or PCP/0 is confirmed.
                                // If STA/0, wait for NL_UPDATE
                                // If PCP/0, wait for NL_UPDATE (PCP SCAN) or ASSOC_IND
                                break;
                            case MeshSector.STATE_CONFIRMED_ASSOC:
                                // Add link to topology for confirmed association
                                peerSectorMac = ownSector.getFirstNeighborNode().getPeerSectMac();
                                meshRoutingManager.addDualLinks(
                                        ownSectorMac, peerSectorMac,
                                        MeshRoutingManager.LINK_COST_METRIC_NBHOPS, 1, 1);
                                meshDatabase.dumpDB(MeshConstants.TRACE);

                                // Update all FWD TBL entries and send required OF & Mesh messages
                                meshRoutingManager.processFwdTblUpdates();
                                break;
                            default:
                                throw new AssertionError("Invalid sector state: " +
                                                                 ownSector.getSectorState());
                        }
                        break;
                    }
                    default:
                        throw new AssertionError("Unsupported node status: " + meshNode.getNodeStatus());
                }
                break;
            }

            // RESULT_ASSOC_FAILED
            case ResultCode.RESULT_ASSOC_FAILED: {
                logger.trace("ASSOC_CFG_FAILED for sector: " + HexString.toHexString(ownSectorMac));

                // Make sure mesh sector is STA
                if (ownSector.getSectorPersonality() != MeshSector.MESH_PERSONALITY_STA_MODE) {
                    throw new AssertionError("ASSOC_FAIL currently only supported in STA mode");
                }
                // Make sure sector is associating
                if (ownSector.getSectorState() != MeshSector.STATE_UNCONFIRMED_ASSOC) {
                    throw new AssertionError("ASSOC_FAIL while not associating");
                }
                // Make sure node is confirmed
                if (!meshNode.isConfirmed()) {
                    throw new AssertionError("ASSOC_FAIL while not in node confirmed state");
                }

                // Remove failed link (was not added to topology yet)
                removeTmpLink(ownSector, ownSector.getFirstNeighborNode());

                // Determine next neighbor to associate with, if any
                processPotentialNeighbors(ownSector);

                // Send ASSOC CFG setup message (either STA with neighbor or STA/0)
                sendAssocCfg(meshNode, ownSector);
                break;
            }
            default:
                throw new AssertionError("Unsupported result code: " + resultCode.getResult());
        }
    }

    /**
     *
     * @param resultCode
     */
    private void processFwdTblCfgCnf(MeshNode meshNode, ResultCode resultCode,
                                     MeshNCConfigConfirm cnfMsg) {
        logger.trace("Processing FWD_TBL_CFG response");

        // FWD TBL CFG failure is not supported
        if (resultCode.getResult() != ResultCode.RESULT_SUCCESS) {
            throw new AssertionError("FWD TBL CFG failed with code: " + resultCode.getResult());
        }

        // Remove FWD TBL CNF message from serialized prerequisites. Send serialized messages that
        // can now be sent, if any.
        meshRoutingManager.processFwdTblPrereqMsg(cnfMsg);
    }

    /**
     *
     * @param resultCode
     */
    private void processLinkReportCfgCnf(MeshNode meshNode, ResultCode resultCode) {
        logger.trace("Processing LINK_REPORT_CFG response");
    }

    /**
     *
     * @param resultCode
     */
    private void processBufferReportCfgCnf(MeshNode meshNode, ResultCode resultCode) {
        logger.trace("Processing BUFFER_REPORT_CFG response");
    }

    /**
     *
     * @param resultCode
     */
    private void processSlaCfgCnf(MeshNode meshNode, ResultCode resultCode) {
        logger.trace("Processing SLA_CFG response");
    }

    /**
     *
     * @param indMsg
     */
    private void processLinkFailure(MeshNCReportInd indMsg) {
        long ownSectMac, peerSectMac;
        meshDatabase.dumpDB(MeshConstants.DEBUG);
        meshRoutingManager.printGraph(MeshConstants.DEBUG);

        // Retrieve link sector MAC addresses
        ownSectMac = indMsg.getOwnSector();
        peerSectMac = indMsg.getPeerSector();
        logger.debug("LINK_FAILURE: own: " + HexString.toHexString(ownSectMac) + " peer: " +
                             HexString.toHexString(peerSectMac));

        // Remove link and serialize ASSOC CFG requests, if required.
        // If link was already removed, send any ASSOC CFG pending reception of this link failure.
        if (!removeLink(ownSectMac, peerSectMac)) {
            meshRoutingManager.processAssocPrereqMsg(indMsg);
            return;
        }

        // Update all FWD TBL entries and send required OF & Mesh messages
        // NOTE: Mesh nodes that are no longer reachable are removed
        meshRoutingManager.processFwdTblUpdates();

        // Process serialized ASSOC messages and send mesh messages that are ready to be sent
        meshRoutingManager.processAssocUpdates();
    }

    /**
     *
     * @param indMsg
     */
    private void processNLUpdate(MeshNCReportInd indMsg) {
        long meshSectorMac = indMsg.getOwnSector();
        MeshSector meshSector = meshDatabase.getSectorMacSectorMap(meshSectorMac);
        MeshNode meshNode = meshSector.getParentNode();

        // Validate sector state
        if (meshSector.getSectorState() != MeshSector.STATE_CONFIRMED_NO_ASSOC) {
            throw new AssertionError("sector state != STATE_CONFIRMED_NO_ASSOC");
        }

        // If PCP sector, PCP SCAN has changed sector to STA personality. Update sector accordingly.
        if (meshSector.getSectorPersonality() == MeshSector.MESH_PERSONALITY_PCP_MODE) {
            logger.debug("NL_UPDATE for PCP sector: " + HexString.toHexString(meshSectorMac));

            // Set the sector personality to STA
            meshSector.setSectorPersonality(MeshSector.MESH_PERSONALITY_STA_MODE);
        }

        // Make sure sector has no neighbors
        if (!meshSector.getNeighborNodeList().isEmpty()) {
            throw new AssertionError("NL_UPDATE received for PCP sector with neighbors");
        }

        // Add new potential neighbors to sector
        meshSector.clearPotentialNeighborList();
        for (NeighborList neighList : indMsg.getNeighborList()) {
            for (NeighborNode neighNode : neighList.getNeighbors()) {
                neighNode.setParentSector(meshSector);
                meshSector.addPotentialNeighbor(neighNode);
            }
        }

        // Determine which neighbor to attempt to associate with first
        processPotentialNeighbors(meshSector);

        // If no neighbor selected, set sector personality to PCP
        if (meshSector.getFirstNeighborNode() == null) {
            meshSector.setSectorPersonality(MeshSector.MESH_PERSONALITY_PCP_MODE);
            meshSector.setSectorState(MeshSector.STATE_UNCONFIRMED_NO_ASSOC);
        }

        // Send ASSOC CFG (STA with neighbor or PCP/0)
        sendAssocCfg(meshNode, meshSector);
    }

    /**
     *
     * @param indMsg
     */
    private void processPeriodicReport(MeshNCReportInd indMsg) {
        MeshSector ownSector, peerSector, pcpSector;
        Long staSectorMac;
        byte mcs, dir;
        boolean mcsUpdated = false;

        // Update link metrics based on received MCS values
        for (LinkReportInfo linkReport : indMsg.getLinkReportInfoList()) {
            // Retrieve link sectors and make sure they are valid
            ownSector = meshDatabase.getSectorMacSectorMap(linkReport.getOwnSectMac());
            if (ownSector == null) {
                logger.debug("ownSector == null");
                continue;
            }
            peerSector = meshDatabase.getSectorMacSectorMap(linkReport.getPeerSectMac());
            if (peerSector == null) {
                logger.debug("peerSector == null");
                continue;
            }

            // MCS attribute is only applied to PCP sector. If own sector is PCP, update MCS;
            // otherwise, update MCS on peer sector.
            if (ownSector.getSectorPersonality() == MeshSector.MESH_PERSONALITY_PCP_MODE) {
                pcpSector = ownSector;
                staSectorMac = linkReport.getPeerSectMac();
                dir = MeshRoutingManager.PCP_TO_STA;
            } else {
                pcpSector = peerSector;
                staSectorMac = linkReport.getOwnSectMac();
                dir = MeshRoutingManager.STA_TO_PCP;
            }

            // Update link MCS on PCP sector
            mcs = linkReport.getLinkReport().getLinkMcs();
            if (pcpSector.updateLinkMcs(staSectorMac, mcs, dir)) {
                mcsUpdated = true;
            }
        }

        // Update all FWD TBL entries and send required OF & Mesh messages if MCS has changed
        if (mcsUpdated) {
            meshRoutingManager.processFwdTblUpdates();
        }
    }

    /**
     * Process NBI Request by invoking corresponding NBI message handler
     * @param req
     */
    private void handleNBIRequest(MeshNBIRequest req) {
        logger.info("handleNBIRequest");
        lastNBIResponse = null;

        // Process NBI request
        if (req instanceof MeshNBICustomerRegistrationReq) {
            lastNBIResponse = processCustomerRegistration((MeshNBICustomerRegistrationReq)req);
        } else if (req instanceof MeshNBICustomerDeregistrationReq) {
            lastNBIResponse = processCustomerDeregistration((MeshNBICustomerDeregistrationReq)req);
        } else if (req instanceof MeshNBICreateSliceReq) {
            lastNBIResponse = processCreateSlice((MeshNBICreateSliceReq)req);
        } else if (req instanceof MeshNBIModifySliceReq) {
            lastNBIResponse = processModifySlice((MeshNBIModifySliceReq)req);
        } else if (req instanceof MeshNBIGetSliceReq) {
            lastNBIResponse = processGetSlice((MeshNBIGetSliceReq)req);
        } else if (req instanceof MeshNBIDeleteSliceReq) {
            lastNBIResponse = processDeleteSlice((MeshNBIDeleteSliceReq)req);
        } else if (req instanceof MeshNBIReadStatsReq) {
            lastNBIResponse = processReadStats((MeshNBIReadStatsReq)req);
        } else if (req instanceof MeshNBIGetMaxAvailableBWReq) {
            lastNBIResponse = processGetMaxAvailableBW((MeshNBIGetMaxAvailableBWReq)req);
        } else if (req instanceof MeshNBIGetAlertsReq) {
            lastNBIResponse = processGetAlerts((MeshNBIGetAlertsReq)req);
        } else if (req instanceof MeshNBIGetNodeInfoReq) {
            lastNBIResponse = processGetNodeInfo((MeshNBIGetNodeInfoReq)req);
        } else {
            throw new AssertionError("Unsupported NBI Request");
        }

        // Signal response available
        synchronized (nbiLock) {
            nbiLock.notify();
        }
    }

    /**
     * NBI Customer Registration request handler
     * @param req
     * @return
     */
    private MeshNBIResponse processCustomerRegistration(MeshNBICustomerRegistrationReq req) {
        logger.info("processCustomerRegistration");
        short result;
        AtomicInteger controllerVersion = new AtomicInteger(0);
        AtomicInteger sessionId = new AtomicInteger(0);
        AtomicInteger sessionDuration = new AtomicInteger(0);

        result = customerRegistration(req.getCustomerId(), req.getPassword(), req.getApiVersion(),
                                      controllerVersion, sessionId, sessionDuration);

        MeshNBICustomerRegistrationRsp response = new MeshNBICustomerRegistrationRsp(
                controllerVersion.byteValue(), mapResultCode(result),
                sessionDuration.shortValue(), sessionId.shortValue());

        return response;
    }

    /**
     * NBI Customer Deregistration request handler
     * @param req
     * @return
     */
    private MeshNBIResponse processCustomerDeregistration(MeshNBICustomerDeregistrationReq req) {
        logger.info("processCustomerDeregistration <<< NOT IMPLEMENTED >>>");
        return null;
    }

    /**
     * NBI Create Slice request handler
     * @param req
     * @return
     */
    private MeshNBIResponse processCreateSlice(MeshNBICreateSliceReq req) {
        short result;
        AtomicInteger sliceId;
        MeshNBICreateSliceRsp response = new MeshNBICreateSliceRsp();
        Integer cirUl = 0, cbsUl = 0, ebsUl = 0, latencyUl = 0, jitterUl = 0;
        Integer cirDl = 0, cbsDl = 0, ebsDl = 0, latencyDl = 0, jitterDl = 0;
        Short eirUl = 0, flrUl = 0;
        Short eirDl = 0, flrDl = 0;
        Short accumulationInterval = 0, reportingInterval = 0;
        Byte priority = 0, statsReporting = 0;
        logger.info("MeshNBICreateSliceReq [sessionId=" + req.getSessionId() +
                            ", poaId=" + req.getPoaId() +
                            ", nodeId=" + req.getNodeId() + "]");

        // Validate session ID
        CustomerSession session = getSession(req.getSessionId());
        if (session == null) {
            response.setReturnCode(mapResultCode(MeshConstants.NC_NBI_INVALID_SESSION_ID));
            return response;
        }

        // Process request
        if ((req.getSessionId() != 0) && (req.getPoaId() != 0) && (req.getNodeId() != 0) &&
                (req.getSla() != null)) {
            BwReq bwReqUl = req.getSla().getBwReqUl();
            if (bwReqUl != null) {
                cirUl = bwReqUl.getCir();
                cbsUl = bwReqUl.getCbs();
                eirUl = bwReqUl.getEir();
                ebsUl = bwReqUl.getEbs();
                latencyUl = bwReqUl.getCommMaxLatency();
                jitterUl = bwReqUl.getCommMaxJitter();
                flrUl = bwReqUl.getCommMaxFlr();
                logger.info("SLA :: BwReqUl [cirUl=" + cirUl + ", cbsUl=" + cbsUl +
                                    ", eirUl=" + eirUl + ", ebsUl=" + ebsUl +
                                    ", latencyUl=" + latencyUl + ", jitterUl=" + jitterUl +
                                    ", flrUl=" + flrUl + "]");
            }

            BwReq bwReqDl = req.getSla().getBwReqDl();
            if (bwReqDl != null) {
                cirDl = bwReqDl.getCir();
                cbsDl = bwReqDl.getCbs();
                eirDl = bwReqDl.getEir();
                ebsDl = bwReqDl.getEbs();
                latencyDl = bwReqDl.getCommMaxLatency();
                jitterDl = bwReqDl.getCommMaxJitter();
                flrDl = bwReqDl.getCommMaxFlr();
                logger.info("SLA :: BwReqDl [cirDl=" + cirDl + ", cbsDl=" + cbsDl +
                                    ", eirDl=" + eirDl + ", ebsDl=" + ebsDl +
                                    ", latencyDl=" + latencyDl + ", jitterDl=" + jitterDl +
                                    ", flrDl=" + flrDl + "]");
            }

            PriorityConfig priorityConfig = req.getSla().getPriorityConfig();
            if (priorityConfig != null) {
                priority = priorityConfig.getPriority();
                logger.info("SLA :: PriorityConfig [priority=" + priority + "]");
            }

            StatsConfig statsConfig = req.getSla().getStatsConfig();
            if (statsConfig != null) {
                accumulationInterval = statsConfig.getAccumulationInterval();
                reportingInterval = statsConfig.getReportingInterval();
                statsReporting = statsConfig.getStatsReporting();
                logger.info("SLA :: StatsConfig [accumulationInterval=" + accumulationInterval +
                                    ", reportingInterval=" + reportingInterval +
                                    ", statsReporting=" + statsReporting + "]");
            }
        }

        // lot of this information is not even taken care of right now in a slice...
        // so we only take the useful ones
        VlanInfo vlanInfo = new VlanInfo((short)0, priority, (short)0, priority,
                                         cirUl, cirDl, cbsUl.shortValue(),
                                         cbsDl.shortValue(), (short)0);

        // Create slice in slice manager
        sliceId = new AtomicInteger(0);
        result = meshSliceManager.createSlice(session, vlanInfo, req.getPoaId(),
                                              req.getNodeId(), sliceId);
        logger.info("cfgUpdate CreateSlice RPC result: " + result);

        // Fill response
        response.setReturnCode(mapResultCode(result));
        response.setSliceId((short)sliceId.intValue());

        return response;
    }

    /**
     * NBI Modify Slice request handler
     * @param req
     * @return
     */
    private MeshNBIResponse processModifySlice(MeshNBIModifySliceReq req) {
        short result;
        MeshNBIModifySliceRsp response = new MeshNBIModifySliceRsp();
        logger.info("MeshNBIModifySliceReq [sessionId=" + req.getSessionId() +
                            ", sliceId=" + req.getSliceId() +
                            ", sla=" + req.getSla().toString() + "]");

        // Validate session ID
        CustomerSession session = getSession(req.getSessionId());
        if (session == null) {
            response.setReturnCode(mapResultCode(MeshConstants.NC_NBI_INVALID_SESSION_ID));
            return response;
        }

        // Modify slice in slice manager
        result = meshSliceManager.modifySlice(session, req.getSliceId(), req.getSla());
        logger.info("cfgUpdate ModifySlice RPC result: " + result);

        // Fill response
        response.setReturnCode(mapResultCode(result));
        return response;
    }

    /**
     * NBI Delete Slice request handler
     * @param req
     * @return
     */
    private MeshNBIResponse processDeleteSlice(MeshNBIDeleteSliceReq req) {
        short result;
        MeshNBIDeleteSliceRsp response = new MeshNBIDeleteSliceRsp();
        logger.info("MeshNBIDeleteSliceReq [sessionId=" + req.getSessionId() +
                            ", sliceId=" + req.getSliceId() + "]");

        // Validate session ID
        CustomerSession session = getSession(req.getSessionId());
        if (session == null) {
            response.setReturnCode(mapResultCode(MeshConstants.NC_NBI_INVALID_SESSION_ID));
            return response;
        }

        // Delete slice in slice manager
        result = meshSliceManager.deleteSlice(session, req.getSliceId());

        // Fill response
        response.setReturnCode(mapResultCode(result));
        return response;
    }

    /**
     * NBI Get Slice request handler
     * @param req
     * @return
     */
    private MeshNBIResponse processGetSlice(MeshNBIGetSliceReq req) {
        short result;
        Integer type = req.getType().intValue();
        List<SliceInfo> sliceInfoList = new ArrayList<>();
        List<Short> sliceIds = new ArrayList<>();
        List<AccessNode> accessNodeList = new ArrayList<>();
        MeshNBIGetSliceRsp response = new MeshNBIGetSliceRsp();
        logger.info("MeshNBIGetSliceReq [sessionId=" + req.getSessionId() + ", type=" + type +
                            ", sliceId=" + req.getSliceId() + "]");

        // Validate session ID
        CustomerSession session = getSession(req.getSessionId());
        if (session == null) {
            response.setReturnCode(mapResultCode(MeshConstants.NC_NBI_INVALID_SESSION_ID));
            return response;
        }

        // Process request according to message type
        switch (type) {
            case 0: //all
                //i want to use this update to get a log of the network
                meshDatabase.dumpDB(MeshConstants.TRACE);
                break;
            case 1: //one slice
                sliceIds.add(req.getSliceId());
                break;
            default:
                break;
        }

        // Get slices from slice manager
        result = meshSliceManager.getSlices(session, sliceIds, accessNodeList);

        // Process response from slice manager
        for (AccessNode accessNode : accessNodeList) {
            //build the sla enum
            StatsConfig statsConfig = new StatsConfig();
            statsConfig.setStatsReporting((byte)0);
            statsConfig.setAccumulationInterval((short)0);
            statsConfig.setReportingInterval((short)0);

            PriorityConfig priorityConfig = new PriorityConfig();
            priorityConfig.setPriority(accessNode.getPcpDl());
            //priority is using the DL vlan priority for both direction

            BwReq bwReqUl = new BwReq();

            int cirUl = accessNode.getReqSlaUl();
            bwReqUl.setCir(cirUl);
            bwReqUl.setCbs((int)accessNode.getCbsUl());
            bwReqUl.setEir((short)0);
            bwReqUl.setEbs(0);
            bwReqUl.setCommMaxLatency(0);
            bwReqUl.setCommMaxJitter(0);
            bwReqUl.setCommMaxFlr((short)0);

            BwReq bwReqDl = new BwReq();
            int cirDl = accessNode.getReqSlaDl();
            bwReqDl.setCir(cirDl);
            bwReqDl.setCbs((int)accessNode.getCbsDl());
            bwReqDl.setEir((short)0);
            bwReqDl.setEbs(0);
            bwReqDl.setCommMaxLatency(0);
            bwReqDl.setCommMaxJitter(0);
            bwReqDl.setCommMaxFlr((short)0);

            Sla sla = new Sla();
            sla.setBwReqDl(bwReqDl);
            sla.setBwReqUl(bwReqUl);
            sla.setPriorityConfig(priorityConfig);
            sla.setStatsConfig(statsConfig);
            //end of build the sla enum

            SliceInfo sliceInfo = new SliceInfo();
            sliceInfo.setPoaId(accessNode.getOwnMac());
            sliceInfo.setSliceId(accessNode.getUniqueId());
            sliceInfo.setSla(sla);
            sliceInfoList.add(sliceInfo);
        }

        // Fill response
        response.setReturnCode(mapResultCode(result));
        response.setNumSlice((short)accessNodeList.size());
        response.setSliceConfigList(sliceInfoList);

        return response;
    }

    /**
     * NBI Get Alerts request handler
     * @param req
     * @return
     */
    private MeshNBIResponse processGetAlerts(MeshNBIGetAlertsReq req) {
        logger.info("MeshNBIGetAlertsReq <<< STUBBED >>>");
        MeshNBIGetAlertsRsp response = new MeshNBIGetAlertsRsp();

        // Validate session ID
        CustomerSession session = getSession(req.getSessionId());
        if (session == null) {
            response.setReturnCode(mapResultCode(MeshConstants.NC_NBI_INVALID_SESSION_ID));
            return response;
        }

        // Process request
        logger.info("getAlerts: type:" + req.getType());
        int listSize = req.getType() == 0 ? 2 : 1;
        List<AlertsReport> alertsReportList = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            Short alertId = 1;
            Short alertInfo = 1001;
            AlertsReport alertsReport = new AlertsReport();
            alertsReport.setAlertId(alertId);
            alertsReport.setAlertInfo(alertInfo);
            alertsReport.setAlertState(AlertStateE.NC_NBI_ALERT_ON);
            alertsReport.setAlertType(AlertTypeE.NC_NBI_SLA_NOT_MET_ALERT);
            alertsReportList.add(alertsReport);

        }

        // Fill response
        response.setReturnCode(ResultCodeE.NC_NBI_OK);
        response.setNumAlert((short) alertsReportList.size());
        response.setAlertIdList(alertsReportList);

        return response;
    }

    /**
     * NBI Read Stats request handler
     * @param req
     * @return
     */
    private MeshNBIResponse processReadStats(MeshNBIReadStatsReq req) {
        logger.info("processReadStats RPC");
        Short result = (short) ResultCodeE.NC_NBI_OK.getValue();
        Short sessionId = req.getSessionId();
        logger.info("readStatistics: [sessionId=" + sessionId + ", type:" + req.getType() + "]");

        // Saving current timestamp before processing any of the flows
        Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
        List<Short> sliceIds = new ArrayList<>();
        List<StatsReport> statsReportList = new ArrayList<>();

        CustomerSession session = getSession(sessionId);
        if (session == null) {
            logger.error("readStatistics: session does not exist");
            result = MeshConstants.NC_NBI_INVALID_SESSION_ID;
        } else if (meshDatabase.getGwNode() == null) {
            logger.error("readStatistics: GateWay Node not found");
        } else {
            if (req.getType() == 0) {
                sliceIds = session.getAllSliceIds();
            } else {
                sliceIds.add(req.getSliceId());
            }

            logger.trace("readStatistics: Total number of slices: " + sliceIds.size());
            MeshNode gatewayNode = meshDatabase.getGwNode();
            DeviceId gwDeviceId = gatewayNode.getAssociatedDeviceId();

            for (Short sliceId : sliceIds) {
                AccessNode accessNode = session.getSlice(sliceId);
                // AccessNode can be null only in case, sliceId is user-provided or NC
                // database has invalid sliceId.NBI would send an empty response
                // with error code in such cases
                if (null == accessNode) {
                    result = (short) ResultCodeE.NC_NBI_INVALID_SLICE_ID.getValue();
                    logger.error("readStatistics: Found invalid sliceId:" + sliceId);
                    break;
                }

                long totalPktsSentToNodeDl = 0, totalPktsReceivedAtNodeDl = 0;
                long totalPktsSentToGWUl = 0, totalPktsReceivedAtrGWUl = 0;
                long numBytesUl = 0, numBytesDl = 0;
                StatsReport statsReport = new StatsReport();
                statsReport.setSliceId(sliceId);
                LinkStats linkStats = new LinkStats();
                statsReport.setTimestampStop(currentTimeStamp.getTime());
                statsReport.setTimestampStart(session.getSliceTimeStamp(sliceId).getTime());
                short vlanDl = accessNode.getVlanIdDl();
                short vlanUl = accessNode.getVlanIdUl();

                // This is the final Node to which accessNode is attached.
                MeshNode parentMeshNode = accessNode.getParentMeshNode();

                if (null == parentMeshNode) {
                    result = (short) ResultCodeE.NC_NBI_INVALID_SLICE_ID.getValue();
                    logger.error("readStatistics: Found invalid sliceId:" + sliceId);
                    break;
                }

                DeviceId parentDeviceId = parentMeshNode.getAssociatedDeviceId();


                logger.info("The Vlan for mesh parentMeshNode:[" +
                                    HexString.toHexString(parentMeshNode.getOwnBridgeMac())
                                    + "] VLAN_UL:(" + vlanUl
                                    + ") VLAN_DL:(" + vlanDl + ")");

                List<Port> meshPorts = new ArrayList<>(deviceService.getPorts(parentDeviceId));
                boolean isULVlanFound = false;
                boolean isDLVlanFound = false;

                // updating stats for vlanUl and vlanDl on Node to which accessNode is attached
                for (Port port : meshPorts) {
                    logger.trace("In the list of MESH ports: portNumBer{}", port.number());
                    ConnectPoint cp = new ConnectPoint(parentDeviceId, port.number());

                    Set<FlowEntry> currentFlowEntrySet = statisticStore.getCurrentStatistic(cp);
                    Set<FlowEntry> previousFlowEntrySet = statisticStore.getPreviousStatistic(cp);

                    if (currentFlowEntrySet == null) {
                        continue;
                    }

                    for (FlowEntry flow : currentFlowEntrySet) {
                        boolean statsFound = false;
                        for (Instruction ins : flow.treatment().allInstructions()) {
                            if (ins.type() == Instruction.Type.L2MODIFICATION) {
                                L2ModificationInstruction l2ins = (L2ModificationInstruction) ins;
                                switch (l2ins.subtype()) {
                                    case VLAN_ID:
                                        if (vlanUl == ((L2ModificationInstruction.ModVlanIdInstruction) l2ins)
                                                .vlanId().toShort()) {
                                            totalPktsSentToGWUl = flow.packets();
                                            logger.info("Matching VlanUl{} on finalNode flowId({}) totalPkts({}) Bytes({}))",
                                                        vlanUl, flow.id().value(), flow.packets(), flow.bytes());
                                            statsFound = isULVlanFound = true;
                                        }
                                        break;
                                    case VLAN_POP:
                                        VlanIdCriterion criterion = (VlanIdCriterion) flow.selector().getCriterion(
                                                Criterion.Type.VLAN_VID);
                                        if ((criterion != null) && (criterion.vlanId().toShort() == vlanDl)) {
                                            linkStats.setNumDataBytesDl(flow.bytes());

                                            // In downlink, out of total number of packets sent from gateway,
                                            // only the number of packets received successfully at Node are
                                            // good packets dl
                                            linkStats.setNumGoodPktsDl(flow.packets());
                                            totalPktsReceivedAtNodeDl = flow.packets();
                                            logger.info("Matching VlanDL(" + vlanDl +
                                                                ") on finalNode with flowId(" + flow.id().value() +
                                                                ") NumGoodPktsDl(" + flow.packets() +
                                                                ") DataBytesDl(" + flow.bytes() + ")");
                                            statsFound = isDLVlanFound = true;

                                            // Getting the previous stat entry for this flow id value to find throughput
                                            for (FlowEntry prevFlow : previousFlowEntrySet) {
                                                if (prevFlow.id().value() == flow.id().value()) {
                                                    logger.trace("Previous DL DataBytes for flowId(" + prevFlow.id().value()
                                                                         + ") is:" + prevFlow.bytes());
                                                    numBytesDl = flow.bytes() - prevFlow.bytes();
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            } else {
                                logger.trace("flowId:{} with trafficTreatment instruction:{} at GW is ignored",
                                             flow.id().value(), ins.type());
                            }
                            if (statsFound) {
                                logger.trace("The flowId:{} on finalNode is found", flow.id().value());
                                break;
                            }
                        }
                        if ((isDLVlanFound) && (isULVlanFound)) {
                            logger.trace("The statistics found in both UL and DL directions on finalNode");
                            break;
                        }
                    }
                    if ((isDLVlanFound) && (isULVlanFound)) {
                        logger.trace("The statistics found in both UL and DL directions on finalNode");
                        break;
                    }
                }

                boolean isGWULVlanFound = false;
                boolean isGWDLVlanFound = false;
                List<Port> gwPorts = new ArrayList<>(deviceService.getPorts(gwDeviceId));
                for (Port port : gwPorts) {
                    logger.trace("In the list of GW ports: portNumber:" + port.number());
                    ConnectPoint cp = new ConnectPoint(gwDeviceId, port.number());
                    Set<FlowEntry> currentGWFlowEntrySet = statisticStore.getCurrentStatistic(cp);
                    Set<FlowEntry> previousGWFlowEntrySet = statisticStore.getPreviousStatistic(cp);
                    if (currentGWFlowEntrySet == null) {
                        continue;
                    }

                    if ((isGWDLVlanFound) && (isGWULVlanFound)) {
                        logger.trace("The statistics found in both UL and DL directions on GW");
                        break;
                    }
                    // updating stats for vlanUl and vlanDl on GW Node
                    for (FlowEntry flow : currentGWFlowEntrySet) {
                        boolean statsFound = false;
                        for (Instruction ins : flow.treatment().allInstructions()) {
                            if (ins.type() == Instruction.Type.L2MODIFICATION) {
                                L2ModificationInstruction l2ins = (L2ModificationInstruction) ins;
                                switch (l2ins.subtype()) {
                                    case VLAN_ID:
                                        if (vlanDl == ((L2ModificationInstruction.ModVlanIdInstruction) l2ins)
                                                .vlanId().toShort()) {
                                            totalPktsSentToNodeDl = flow.packets();
                                            logger.trace("Matching VlanDl{} on GW flowId({}) totalPkts({})",
                                                         vlanDl, flow.id().value(), flow.packets());
                                            statsFound = isGWDLVlanFound = true;
                                        }
                                        break;
                                    case VLAN_POP:
                                        VlanIdCriterion criterion = (VlanIdCriterion) flow.selector().getCriterion(
                                                Criterion.Type.VLAN_VID);
                                        if ((criterion != null) && (criterion.vlanId().toShort() == vlanUl)) {

                                            // In uplink, out of total number of packets sent to gateway,
                                            // only the number of packets received successfully at gateway
                                            // are good packets ul
                                            linkStats.setNumGoodPktsUl(flow.packets());
                                            linkStats.setNumDataBytesUl(flow.bytes());
                                            totalPktsReceivedAtrGWUl = flow.packets();
                                            logger.trace("Matching VlanUL(" + vlanUl +
                                                                 ") on GW with flowId(" + flow.id().value() +
                                                                 ") NumGoodPktsUl(" + flow.packets() +
                                                                 ") DataBytesUl(" + flow.bytes() + ")");
                                            statsFound = isGWULVlanFound = true;
                                            Boolean prevFlowFound = false;

                                            // Getting previous stats entry for this flow id value to calculate throughput
                                            for (FlowEntry prevFlow : previousGWFlowEntrySet) {

                                                // Below check is for gateway prev stats. Since flow ID
                                                // on GW for all flows in UL direction is same,we need to
                                                // check for VLAN_POP id too along with flowId
                                                for (Instruction insPrev : prevFlow.treatment().allInstructions()) {
                                                    if (insPrev.type() == Instruction.Type.L2MODIFICATION) {
                                                        L2ModificationInstruction l2insPrev = (L2ModificationInstruction) insPrev;
                                                        if (l2insPrev.subtype() == L2ModificationInstruction.L2SubType.VLAN_POP) {
                                                            VlanIdCriterion criterionPrev =
                                                                    (VlanIdCriterion) prevFlow.selector().getCriterion(Criterion.Type.VLAN_VID);
                                                            if (((criterionPrev != null) && (criterionPrev.vlanId().toShort() == vlanUl)) &&
                                                                    (prevFlow.id().value() == flow.id().value())) {
                                                                logger.trace("Previous databyte UL on GW node for flowId(" + prevFlow.id().value()
                                                                                     + ") is:" + prevFlow.bytes());
                                                                numBytesUl = flow.bytes() - prevFlow.bytes();
                                                                prevFlowFound = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                                if (prevFlowFound) {
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            } else {
                                logger.trace("flowId:{} with trafficTreatment instruction:{} at GW is ignored",
                                             flow.id().value(), ins.type());
                            }
                            if (statsFound) {
                                logger.trace("The flowId {} on GW is found", flow.id().value());
                                break;
                            }
                        }
                    }
                }

                logger.trace("numbytesUL({}) currentBytes({})", numBytesUl, linkStats.getNumDataBytesUl());
                long bitsUl = numBytesUl * 8;
                double megabitsUl = bitsUl / (1024.0 * 1024.0);
                long timeDiffInSec = DEFAULT_INTERSTAT_TIME_DIFF;
                double throughputUl = megabitsUl / timeDiffInSec;
                throughputUl = (throughputUl < 0) ? 0 : throughputUl;
                linkStats.setThroughputUl(throughputUl);

                logger.trace("numBytesDl({}) currentBytes({}))  ", numBytesDl, linkStats.getNumDataBytesDl());
                long bitsDl = numBytesDl * 8;
                double megabitsDl = bitsDl / (1024.0 * 1024.0);
                double throughputDl = megabitsDl / timeDiffInSec;
                throughputDl = (throughputDl < 0) ? 0 : throughputDl;
                linkStats.setThroughputDl(throughputDl);

                // The captured sent packets can be less then received packets when traffic
                // is live during NBI call, hence using absolute values to prevent negative
                // no of dropped packets
                linkStats.setNumPktsDroppedUl(abs(totalPktsSentToGWUl - totalPktsReceivedAtrGWUl));
                linkStats.setNumPktsDroppedDl(abs(totalPktsSentToNodeDl - totalPktsReceivedAtNodeDl));
                statsReport.setLinkStats(linkStats);
                statsReportList.add(statsReport);
            }
        }

        return new MeshNBIReadStatsRsp(mapResultCode(result), (short)sliceIds.size(), statsReportList);
    }

    /**
     * NBI Get Max Available BW request handler
     * @param req
     * @return
     */
    private MeshNBIResponse processGetMaxAvailableBW(MeshNBIGetMaxAvailableBWReq req) {
        short result;
        long maxAvailBwUl = -1, maxAvailBwDl = -1;
        short sessionId = req.getSessionId();
        long nodeId = req.getNodeId();
        MeshNBIGetMaxAvailableBWRsp response = new MeshNBIGetMaxAvailableBWRsp();
        logger.info("processGetMaxAvailableBW");
        logger.info("MeshNBIGetMaxAvailableBWRsp [sessionId=" + sessionId + ", nodeId=" + nodeId + "]");

        // Validate session ID
        CustomerSession session = getSession(sessionId);
        if (session == null) {
            response.setReturnCode(mapResultCode(MeshConstants.NC_NBI_INVALID_SESSION_ID));
            return response;
        }

        // Retrieve max available UL & DL BW for requested mesh node
        MeshNode meshNode = meshDatabase.getNodeIdMacNodeMap(nodeId);
        if ((sessionId != 0) && (meshNode != null)) {
            maxAvailBwUl = meshRoutingManager.getMaxAvailableBW(meshNode, true);
            maxAvailBwDl = meshRoutingManager.getMaxAvailableBW(meshNode, false);
        }

        // Determine response to send based on available UL & DL BW
        response.setMaxBwUl((short)maxAvailBwUl);
        response.setMaxBwDl((short)maxAvailBwDl);
        if (sessionId == 0) {
            result = MeshConstants.NC_NBI_INVALID_SESSION_ID;
        } else if((maxAvailBwUl >= 0) && (maxAvailBwDl >= 0)) {
            result = MeshConstants.NC_NBI_OK;
        } else {
            result = MeshConstants.NC_NBI_INVALID_NODE_ID;
        }
        response.setReturnCode(mapResultCode(result));

        return response;
    }


    /**
     * NBI Get Node Info request handler
     * @param req
     * @return
     */
    private MeshNBIResponse processGetNodeInfo(MeshNBIGetNodeInfoReq req) {
        logger.info("cfgUpdate getNodeInfo RPC");
        Short sessionId = req.getSessionId();
        Short result;
        logger.info("getNodeInfo [sessionId=" + sessionId + "]");

        List<NodeInfo> nodeInfoList = new ArrayList<>();
        List<MeshNode> meshNodeList = null;
        result = (short)ResultCodeE.NC_NBI_OK.getValue() ;
        CustomerSession session = getSession(sessionId);
        Short numNode = 0;

        if (session == null) {
            result = MeshConstants.NC_NBI_INVALID_SESSION_ID;
        } else {
            meshNodeList = meshDatabase.getMeshNodeList();
            for (MeshNode meshNode : meshNodeList) {
                // Ignoring the gateway for node info
                if (meshNode.isGwNodeRole() == false){
                    NodeInfo nodeInfo = new NodeInfo();
                    nodeInfo.setNodeId(meshNode.getNodeId());
                    // setting default values for latitude long as of now
                    nodeInfo.setNodeLocation(meshNode.getNodeLocation());
                    List<Long> poaList = new ArrayList<>();
                    Short numPoA = (short) (meshNode.getAccessNodeList().size());
                    nodeInfo.setNumPoa(numPoA);
                    for (AccessNode accessNode : meshNode.getAccessNodeList()) {
                        poaList.add(accessNode.getOwnMac());
                    }
                    nodeInfo.setPoaList(poaList);
                    nodeInfoList.add(nodeInfo);
                }
            }
            numNode = (short) nodeInfoList.size();
        }

        return new MeshNBIGetNodeInfoRsp(mapResultCode(result),numNode,nodeInfoList);
    }

    /**
     * Process Device Event by invoking corresponding Device Event message handler
     * @param event
     */
    private void handleDeviceEvent(MeshDeviceEvent event) {
        logger.info("handleDeviceEvent");

        // Process Device Event
        if (event instanceof MeshDeviceAdded) {
            processDeviceAdded(event.getDeviceId());
        } else if (event instanceof MeshDeviceRemoved) {
            processDeviceRemoved(event.getDeviceId());
        } else {
            throw new AssertionError("Unsupported Mesh Device Event");
        }
    }

    /**
     * Handle OF device addition
     * @param deviceId
     */
    private void processDeviceAdded(DeviceId deviceId) {
        // Retrieve device MAC
        MeshNode meshNode;
        long nodeMac = convertDeviceIdToMac(deviceId);
        long nodeId = nodeMac;
        logger.debug("DEVICE_ADDED in MESH for " + HexString.toHexString(nodeMac));

        // If no mesh nodes in DB, this is the GW node
        if (meshDatabase.getMeshNodeList().isEmpty()) {
            // Create GW mesh node
            meshNode = createMeshNode(nodeMac, nodeId);

            // Set OF device ID in mesh node
            meshNode.setAssociatedDeviceId(deviceId);

            // Add GW node to list of GW nodes in DB
            meshDatabase.addGwNode(meshNode);
            meshDatabase.dumpDB(MeshConstants.TRACE);

            // Send default FLOOD rule on GW node to allow Mesh connection to be established.
            // Allows UL traffic to reach NC.
            // NOTE: Before OVS connection, normal switch behavior permits flooding, which allows
            // OF switch to connect to NC. Once connected, however, the Mesh connection will
            // not pass until a flooding rule is explicitly added on the GW switch.
            meshRoutingManager.sendDefaultFlood(meshNode, MeshFwdTblEntry.ADD);

            // Send default SELF rule on GW node to allow Mesh connection to be established.
            // Allows DL traffic to reach NC.
            meshRoutingManager.sendDefaultSelf(meshNode);

            // Apply default flows on GW node
            meshRoutingManager.sendBroadcastAsPacketIn(meshNode);
            meshRoutingManager.sendDefaultDrop(meshNode);

            // Flush all other NGW devices (remove stuck devices)
            for (Device device : deviceService.getDevices()) {
                if (!device.id().equals(deviceId)) {
                    logger.error("Device was left hanging, deleting: " + device.id());
                    flowRuleService.purgeFlowRules(device.id());
                    disconnectAssociatedDevice(device.id());
                }
            }
        } else {
            // Retrieve mesh node for new device mac
            meshNode = meshDatabase.getNodeIdMacNodeMap(nodeMac);
            if (meshNode == null) {
                throw new AssertionError("Unable to find Mesh Node for added device");
            }

            // Set OF device ID in mesh node
            meshNode.setAssociatedDeviceId(deviceId);

            // Apply default flows on new mesh node
            meshRoutingManager.sendBroadcastAsPacketIn(meshNode);
            meshRoutingManager.sendDefaultDrop(meshNode);

            // PATCH: Current implementation uses Mesh signaling responses to synchronize requests
            //        sent to Flow Rule Manager. Although the order is maintained on an intra-node
            //        basis, some cases have been observed where inter-node order is NOT
            //        maintained. The most important issue caused by this is when a POP VLAN rule is
            //        applied AFTER a PUSH VLAN rule for a specific signaling tunnel. In this case
            //        connectivity with the node is lost. To avoid this case, we need a means of
            //        verifying inter-node flow rule installation, which is not supported in the
            //        current implementation. As a patch, we send the POP VLAN rule here instead;
            //        it will simply be overwritten at a later time.
            //
            //        NOTE: inter-node order issues may still occur in other scenarios however the
            //              system should recover with minimal impact on data. Therefore, we only
            //              patch the critical use case here.
            //
            meshRoutingManager.sendDefaultPop(meshNode);

            // Update FWD TBL entries and send required OF messages
            meshRoutingManager.processFwdTblUpdates(meshNode, null, true);
        }
    }

    /**
     * Handle OF device removal
     * @param deviceId
     */
    private void processDeviceRemoved(DeviceId deviceId) {
        // Retrieve device MAC
        long mac = convertDeviceIdToMac(deviceId);
        logger.debug("Device removed: " + HexString.toHexString(mac));

        // Remove device flow rules
        flowRuleService.purgeFlowRules(deviceId);

        // Retrieve associated mesh node, if it exists
        MeshNode meshNode = meshDatabase.getNodeIdMacNodeMap(mac);
        if (meshNode == null) {
            logger.debug("meshNode == null. Ignoring.");
            return;
        }

        // Ignore event if Mesh Node has no associated device
        if (meshNode.getAssociatedDeviceId() == null) {
            logger.debug("Mesh node has no associated device. Ignoring.");
            return;
        }

        // Trigger RESET if GW node connection is lost
        // NOTE: If NC HELLO for GW node not yet received, mesh node object does not yet know
        //       that it is a GW node. Therefore, another verification is required to see if
        //       this is the only mesh node available. If so, then trigger a RESET.
        if (meshNode.isGwNodeRole() || (meshDatabase.getMeshNodeList().size() == 1)) {
            logger.debug("GW is going down. Triggering a fake NC_RESET.");
            processNcReset(new MeshNCReset((byte) 1, -1), true);
            return;
        }

        // Remove all mesh node links and serialize ASSOC CFG requests, if required
        removeLinks(meshNode);

        // Update all FWD TBL entries and send required OF & Mesh messages
        // NOTE: Mesh nodes that are no longer reachable are removed
        meshRoutingManager.processFwdTblUpdates();

        // Process serialized ASSOC messages and send mesh messages that are ready to be sent
        meshRoutingManager.processAssocUpdates();
    }

    /**
     * Parse reqSla string and process contents
     * @param reqSla
     */
    private void parseReqSla(String reqSla) {
        long nodeMac;
        int nbOfRead;
        short vlanUl, vlanDl;
        byte pcpUl, pcpDl;

        logger.trace("getting the REQSLAs from config in MeshRoutingManager");
        List<String> reqSlas = Arrays.asList(reqSla.split("\\s*,\\s*"));

        int i = 0;
        try {
            //we are supposed to have even number of entries
            nbOfRead = reqSlas.size();
            while (i < nbOfRead) {
                // Node MAC
                nodeMac = Tools.fromHex(reqSlas.get(i++));

                // UL VLAN
                vlanUl = Short.decode(reqSlas.get(i++));
                meshRoutingManager.allocateVlan(
                        (vlanUl != 0) ? vlanUl : meshRoutingManager.getAvailableVlanId());

                // UL VLAN PCP
                pcpUl = Byte.decode(reqSlas.get(i++));

                // DL VLAN
                vlanDl = Short.decode(reqSlas.get(i++));
                meshRoutingManager.allocateVlan(
                        (vlanDl != 0) ? vlanDl : meshRoutingManager.getAvailableVlanId());

                // DL VLAN PCP
                pcpDl = Byte.decode(reqSlas.get(i++));

                // Store VLAN info
                VlanInfo vlanInfo = new VlanInfo(vlanUl, pcpUl, vlanDl, pcpDl, 0, 0,
                                                 (short)0, (short)0, (short)0);
                meshRoutingManager.setVlanInfo(nodeMac, vlanInfo);

                // Read & ignore data VLAN count
                Short.decode(reqSlas.get(i++));
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid reqSla value:{}, around index({})", reqSla, i);
        }
    }

    /**
     *
     * @param meshNode
     */
    private void processConfirmedComplete(MeshNode meshNode) {
        long peerSectorMac;
        logger.trace("NODE is confirmed for all sectors");
        meshNode.setNodeStatus(MeshNode.CONNECTED_CONFIRMED_COMPLETE);

        // Process all sectors
        for (MeshSector sector : meshNode.getMeshSectList()) {
            // Make sure sector is STA
            if (sector.getSectorPersonality() != MeshSector.MESH_PERSONALITY_STA_MODE) {
                throw new AssertionError("Sector != STA");
            }

            switch (sector.getSectorState()) {
                case MeshSector.STATE_CONFIRMED_NO_ASSOC:
                    // Send ASSOC_CFG for STA/0 sector to become PCP/0 sector
                    sector.setSectorPersonality(MeshSector.MESH_PERSONALITY_PCP_MODE);
                    sector.setSectorState(MeshSector.STATE_UNCONFIRMED_NO_ASSOC);
                    sendAssocCfg(meshNode, sector);
                    break;
                case MeshSector.STATE_CONFIRMED_ASSOC:
                    // Add link to topology for confirmed association
                    peerSectorMac = sector.getFirstNeighborNode().getPeerSectMac();
                    meshRoutingManager.addDualLinks(
                            sector.getOwnSectMac(), peerSectorMac,
                            MeshRoutingManager.LINK_COST_METRIC_NBHOPS, 1, 1);
                    break;
                default:
                    throw new AssertionError("Invalid sector state: " + sector.getSectorState());
            }
        }
        meshDatabase.dumpDB(MeshConstants.TRACE);

        // Send link & buffer REPORT CFG setup message
        sendReportCfg(meshNode);

        // Update all FWD TBL entries and send required OF & Mesh messages
        meshRoutingManager.processFwdTblUpdates();


        // Add any pending access nodes for mesh node
        List<AccessNode> pendingAccessNodeList = meshSliceManager.removePendingAccessNodes(
                meshNode.getOwnBridgeMac());
        for (AccessNode accessNode : pendingAccessNodeList) {
            meshSliceManager.addSliceFromPendingAccessNode(accessNode, meshNode);
        }

    }

    /**
     *
     * @param customerId
     * @param password
     * @param customerVersion
     * @param controllerVersion
     * @param sessionId
     * @param sessionDuration
     * @return
     */
    private short customerRegistration(
            long customerId,
            String password,
            int customerVersion,
            AtomicInteger controllerVersion,
            AtomicInteger sessionId,
            AtomicInteger sessionDuration) {

        CustomerSession customerSession;
        short ret;

        // Validate customer & retrieve session duration
        ret = validateCustomer(customerId, password, sessionDuration);
        if (ret == MeshConstants.NC_NBI_OK) {
            controllerVersion.set(meshDatabase.getLowestCommonVersion((byte)customerVersion));
            // No possible errors for the moment... we use the smallest version
            // value, NC_NBI_VERSION_NOT_SUPPORTED

            // Check if session was already given for that customer. If so, we return it.
            if (customerIdCustomerSessionMap.containsKey(customerId)) {
                logger.trace("Customer session already exists");
                customerSession = customerIdCustomerSessionMap.get(customerId);
            } else {
                logger.trace("New customer session created");
                customerSession = new CustomerSession(this, customerId);
            }
            //create a new nbi session
            sessionId.set(getAvailableNbiSessionId());
            customerSession.createNewNbiSession(sessionId.shortValue(), sessionDuration.shortValue());
            customerIdCustomerSessionMap.put(customerId, customerSession);
            sessionIdCustomerSessionMap.put(sessionId.shortValue(), customerSession);
        }

        logger.info("Customer registered : " + customerId + " with sessionId " + sessionId);
        return ret;
    }

    /**
     *
     * @param customerId
     * @param password
     * @param sessionDuration
     * @return
     */
    private short validateCustomer(long customerId, String password, AtomicInteger sessionDuration) {
        short returnValue = MeshConstants.NC_NBI_INVALID_CUSTOMER_ID;

        class Customer {
            private long customerId;
            private String password;
            private short sessionDuration;
            protected Customer(long customerId, String password, short sessionDuration) {
                this.customerId = customerId;
                this.password = password;
                this.sessionDuration = sessionDuration;
            }
            protected long getCustomerId() {
                return customerId;
            }
            protected String getPassword() {
                return password;
            }
            protected short getSessionDuration() {
                return sessionDuration;
            }
        }

        // Populate customers & passwords
        List<Customer> customerList = new ArrayList<>();
        customerList.add(new Customer(1, "root", (short)1000));
        customerList.add(new Customer(2, "admin", (short)2));

        // Validate customer
        logger.trace("customer validation: customerId=" + customerId);
        for (Customer customer : customerList) {
            if (customer.getCustomerId() == customerId) {
                if (customer.getPassword().contentEquals(password)) {
                    logger.trace("customer validation succeeded");
                    sessionDuration.set((int)customer.getSessionDuration());
                    returnValue = MeshConstants.NC_NBI_OK;
                    break;
                } else {
                    returnValue = MeshConstants.NC_NBI_INVALID_PASSWORD;
                }
            }
        }

        return returnValue;
    }

    /**
     *
     * @param sessionId
     * @return
     */
    private CustomerSession getSession(short sessionId) {
        return sessionIdCustomerSessionMap.get(sessionId);
    }

    /**
     *
     * @param sessionId
     */
    public void removeNbiSession(short sessionId) {
        sessionIdCustomerSessionMap.put(sessionId, null);
    }

    /**
     *
     * @return
     */
    private short getAvailableNbiSessionId() {
        short sessionId = nextAvailableNbiSessionId;
        nextAvailableNbiSessionId++;
        return sessionId;
    }

    /**
     *
     * @param deviceId
     * @return
     */
    private long convertDeviceIdToMac(DeviceId deviceId) {
        long mac = Long.parseLong(deviceId.uri().getSchemeSpecificPart(), 16);
        return mac;
    }

    /**
     *
     * @param meshNode
     * @return
     */
    private boolean sendReportCfg(MeshNode meshNode) {
        MeshNCConfigSetup cfgSetup = createConfigSetupMsg(meshNode);
        cfgSetup.encodeLinkReportIe();
        cfgSetup.encodeBufferReportIe();
        return meshRoutingManager.sendMsgToSBI(meshNode.getMeshDeviceId(), cfgSetup);
    }

    /**
     *
     * @param meshNode
     * @param meshSector
     * @return
     */
    private boolean sendAssocCfg(MeshNode meshNode, MeshSector meshSector) {
        MeshNCConfigSetup cfgSetup = createConfigSetupMsg(meshNode);
        cfgSetup.encodeAssocIe(meshSector);
        return meshRoutingManager.sendMsgToSBI(meshNode.getMeshDeviceId(), cfgSetup);
    }

    /**
     *
     * @param meshNode
     * @return
     */
    private MeshNCConfigSetup createConfigSetupMsg(MeshNode meshNode) {
        MeshNCConfigSetup cfgSetup = new MeshNCConfigSetup();
        cfgSetup.setVersion(version);
        cfgSetup.setConfigNodeId(meshNode.getNodeId());
        cfgSetup.setDeviceId(meshNode.getMeshDeviceId());
        cfgSetup.setTid(meshNode.getNewTid());
        return cfgSetup;
    }

    /**
     *
     * @param result
     * @return
     */
    private ResultCodeE mapResultCode(short result) {
        ResultCodeE resultCode;
        switch(result) {
            case MeshConstants.NC_NBI_OK:
                resultCode = ResultCodeE.NC_NBI_OK;
                break;
            case MeshConstants.NC_NBI_INVALID_CUSTOMER_ID:
                resultCode = ResultCodeE.NC_NBI_INVALID_CUSTOMER_ID;
                break;
            case MeshConstants.NC_NBI_INVALID_PASSWORD:
                resultCode = ResultCodeE.NC_NBI_INVALID_PASSWORD;
                break;
            case MeshConstants.NC_NBI_VERSION_NOT_SUPPORTED:
                resultCode = ResultCodeE.NC_NBI_VERSION_NOT_SUPPORTED;
                break;
            case MeshConstants.NC_NBI_INVALID_SESSION_ID:
                resultCode = ResultCodeE.NC_NBI_INVALID_SESSION_ID;
                break;
            case MeshConstants.NC_NBI_INVALID_NODE_ID:
                resultCode = ResultCodeE.NC_NBI_INVALID_NODE_ID;
                break;
            case MeshConstants.NC_NBI_INVALID_POA_ID:
                resultCode = ResultCodeE.NC_NBI_INVALID_POA_ID;
                break;
            case MeshConstants.NC_NBI_CANNOT_MEET_SLA:
                resultCode = ResultCodeE.NC_NBI_CANNOT_MEET_SLA;
                break;
            case MeshConstants.NC_NBI_INVALID_SLICE_ID:
                resultCode = ResultCodeE.NC_NBI_INVALID_SLICE_ID;
                break;
            case MeshConstants.NC_NBI_INVALID_SLA:
                resultCode = ResultCodeE.NC_NBI_INVALID_SLA;
                break;
            case MeshConstants.NC_NBI_INVALID_ALERT_ID:
                resultCode = ResultCodeE.NC_NBI_INVALID_ALERT_ID;
                break;
            default:
                resultCode = ResultCodeE.NC_NBI_OTHER_ERROR;
                break;
        }
        return resultCode;
    }

    /**
     *
     * @param ownSector
     */
    private void processPotentialNeighbors(MeshSector ownSector) {
        NeighborNode bestNeighNode = null, neighToRemove = null;
        byte currentMcs, highestMcs = 0;
        logger.trace("neighborSelection for: " + ownSector.toString(1));

        // Make sure at least one potential neighbor is present
        if (ownSector.getPotentialNeighborNodeList().isEmpty()) {
            logger.trace("No potential neighbors");
            return;
        }

        // Remove potential neighbors that match an existing peer node
        removeDuplicatePotentialNeighs(ownSector);

        // Reevaluate if the list is now empty
        if (ownSector.getPotentialNeighborNodeList().isEmpty()) {
            logger.trace("No potential neighbors after clearing duplicate neighbor nodes");
            return;
        }

        // Find best neighbor based on highest MCS value
        for (NeighborNode neighNode : ownSector.getPotentialNeighborNodeList()) {
            currentMcs = neighNode.getLinkReport().getLinkMcs();
            if (currentMcs >= highestMcs) {
                bestNeighNode = neighNode;
                highestMcs = currentMcs;
            }
        }

        // Remove temporary link if potential final assoc differs from current temp assoc
        for (NeighborNode neighNode : ownSector.getNeighborNodeList()) {
            if (!neighNode.getPeerSectMac().equals(bestNeighNode.getPeerSectMac())) {
                logger.trace("Temp assoc neighbor:" + neighNode.toString() +
                                     "\ndifferent from best neighbor:" + bestNeighNode.toString());
                neighToRemove = neighNode;
                break;
            }
        }
        if (neighToRemove != null) {
            removeTmpLink(ownSector, neighToRemove);
        }

        // Add temporary link to best neighbor node
        if (ownSector.getNeighborNodeList().isEmpty()) {
            addTmpLink(ownSector, bestNeighNode);
        }

        // Remove best neighbor node from potential neighbor list
        ownSector.removePotentialNeighbor(bestNeighNode);
    }

    /**
     *
     * @param ownSector
     */
    private void removeDuplicatePotentialNeighs(MeshSector ownSector) {
        Long ownSectorMac = ownSector.getOwnSectMac();
        MeshNode ownNode = ownSector.getParentNode();
        List<NeighborNode> potentialNeighNodesToClear = new ArrayList<>();

        // Remove currently associated neighbor nodes from potential neighbor list. This prevents
        // two sectors from associating to same peer node.
        for (MeshSector sector : ownNode.getMeshSectList()) {
            // Ignore current sector
            if (sector.getOwnSectMac().equals(ownSectorMac)) {
                continue;
            }

            // Check all sector neighbors
            for (NeighborNode neigh : sector.getNeighborNodeList()) {
                MeshNode neighNode = meshDatabase.getSectorMacSectorMap(
                        neigh.getPeerSectMac()).getParentNode();

                // Look for match with current mesh sector potential neighbors
                for (NeighborNode potentialNeigh : ownSector.getPotentialNeighborNodeList()) {
                    // Verify that potential neighbor sector exists
                    MeshSector potentialNeighSector = meshDatabase.getSectorMacSectorMap(
                            potentialNeigh.getPeerSectMac());
                    if (potentialNeighSector == null) {
                        logger.warn("Potential neighbor sector no longer exists: " +
                                             HexString.toHexString(potentialNeigh.getPeerSectMac()));
                        potentialNeighNodesToClear.add(potentialNeigh);
                        continue;
                    }

                    // Verify that potential neighbor node is different from current peer nodes
                    MeshNode potentialNeighNode = potentialNeighSector.getParentNode();
                    if (potentialNeighNode.equals(neighNode)) {
                        logger.trace("Potential neighbor node matches existing neighbor node for peer: " +
                                             HexString.toHexString(neigh.getPeerSectMac()) + " potential: " +
                                             HexString.toHexString(potentialNeigh.getPeerSectMac()));
                        potentialNeighNodesToClear.add(potentialNeigh);
                    }
                }

                // Remove any matching neighbor nodes
                for (NeighborNode potentialNeigh : potentialNeighNodesToClear) {
                    logger.trace("Removing potential neighbor : " + potentialNeigh.toString());
                    ownSector.getPotentialNeighborNodeList().remove(potentialNeigh);
                }
                potentialNeighNodesToClear.clear();
            }
        }
    }

    /**
     *
     * @param ownSector
     * @param neighNode
     */
    private void addTmpLink(MeshSector ownSector, NeighborNode neighNode) {
        MeshSector peerSector;
        NeighborNode peerNeighNode;
        Long ownSectorMac = ownSector.getOwnSectMac();
        logger.trace("Adding tmp link for sector: " +
                             HexString.toHexString(ownSector.getOwnSectMac()) +
                             " neighbor:" + neighNode.toString());

        // Add neighbor node on own sector
        ownSector.addNeighbor(neighNode);

        // Add neighbor node on peer sector
        peerSector = meshDatabase.getSectorMacSectorMap(neighNode.getPeerSectMac());
        peerNeighNode = new NeighborNode(ownSectorMac, peerSector);
        peerSector.addNeighbor(peerNeighNode);

        // Add Mesh Link on peer PCP sector
        logger.trace("Adding MeshLink on peer PCP: " +
                             HexString.toHexString(peerSector.getOwnSectMac()) +
                             " from own STA: " + HexString.toHexString(ownSectorMac));
        peerSector.createMeshLink(ownSectorMac);
    }

    /**
     *
     * @param ownSector
     * @param neighNode
     */
    private void removeTmpLink(MeshSector ownSector, NeighborNode neighNode) {
        MeshSector peerSector;
        NeighborNode peerNeighNode;
        Long ownSectorMac = ownSector.getOwnSectMac();
        logger.trace("Removing tmp link for sector: " + HexString.toHexString(ownSectorMac) +
                             " neighbor:" + neighNode.toString());

        // Remove Neighbor node & Mesh Link on PCP sector
        peerSector = meshDatabase.getSectorMacSectorMap(neighNode.getPeerSectMac());
        peerNeighNode = peerSector.getNeighborNode(ownSectorMac);
        peerSector.removeNeighbor(peerNeighNode);
        peerSector.removeMeshLink(ownSectorMac);

        // Remove Neighbor node on own sector
        ownSector.removeNeighbor(neighNode);
    }

    /**
     * Remove matching neighbor node & Mesh link from provided sector
     * @param ownSector Own sector
     * @param peerMac Neighbor MAC
     */
    private void removeNeighbor(MeshSector ownSector, long peerMac) {
        NeighborNode neighNodeToRemove = null;

        // Find neighbor node
        for (NeighborNode neighNode : ownSector.getNeighborNodeList()) {
            if (neighNode.getPeerSectMac() == peerMac) {
                neighNodeToRemove = neighNode;
                break;
            }
        }

        // If neighbor node found, remove it
        if (neighNodeToRemove != null) {
            removeNeighbor(neighNodeToRemove);
        }
    }

    /**
     * Remove neighbor node & Mesh link. Release reserved BW.
     * @param neighborNode
     */
    private void removeNeighbor(NeighborNode neighborNode) {
        MeshSector ownSector = neighborNode.getOwnSector();

        // Get Mesh Link to remove (only works on PCP side)
        MeshLink link = ownSector.getMeshLink(neighborNode.getPeerSectMac());
        if (link != null) {
            // Release all BW for VLAN IDs passing through link
            meshDatabase.releaseBW(link);
            ownSector.removeMeshLink(link);
        }

        // Remove neighbor node & potential neighbor node
        ownSector.removeNeighbor(neighborNode);
        ownSector.removePotentialNeighbor(neighborNode);
    }

    /**
     *
     * @param meshNode
     */
    private void removeLinks(MeshNode meshNode) {
        Long ownSectMac;
        List<NeighborNode> neighNodeList = new ArrayList<>();

        // Remove all mesh node sector links
        // NOTE: If link was already removed by peer it will not be in the neighbor list
        for (MeshSector ownSector : meshNode.getMeshSectList()) {
            ownSectMac = ownSector.getOwnSectMac();
            neighNodeList.clear();
            neighNodeList.addAll(ownSector.getNeighborNodeList());

            for (NeighborNode neighNode : neighNodeList) {
                removeLink(ownSectMac, neighNode.getPeerSectMac());
            }
        }
    }

    /**
     *
     * @param ownSectMac
     * @param peerSectMac
     */
    private boolean removeLink(long ownSectMac, long peerSectMac) {
        MeshSector ownSector, peerSector;
        MeshNode ownNode, peerNode;

        // Retrieve link failure own sector & node
        ownSector = meshDatabase.getSectorMacSectorMap(ownSectMac);
        if (ownSector == null) {
            throw new AssertionError("ownSector == null");
        }
        ownNode = ownSector.getParentNode();

        // Retrieve link failure peer sector. If peer sector does not exist, link failure has
        // already been processed.
        peerSector = meshDatabase.getSectorMacSectorMap(peerSectMac);
        if (peerSector == null) {
            logger.debug("peerSector == null. Link removal already processed.");
            return false;
        }
        peerNode = peerSector.getParentNode();

        // Remove link. If already removed, link failure has already been processed.
        if (!meshRoutingManager.removeDualLinks(ownSectMac, peerSectMac)) {
            logger.debug("Link already removed. Link removal already processed.");
            return false;
        }
        meshRoutingManager.printGraph(MeshConstants.DEBUG);

        // Remove neighbor nodes & Mesh links for each sector
        // NOTE: Clears all BW used by all VLAN IDs passing through removed link
        removeNeighbor(ownSector, peerSectMac);
        removeNeighbor(peerSector, ownSectMac);
        meshDatabase.dumpDB(MeshConstants.DEBUG);

        // Serialize ASSOC CFG for STA side of link
        SerializedMeshMessage serMsg = new SerializedMeshMessage();
        MeshNCConfigSetup cfgSetup;

        // Create ASSOC CFG message to serialize and prerequisite REPORT_IND
        if (ownSector.getSectorPersonality() == MeshSector.MESH_PERSONALITY_STA_MODE) {
            cfgSetup = createConfigSetupMsg(ownNode);
            cfgSetup.encodeAssocIe(ownSector);
        } else {
            cfgSetup = createConfigSetupMsg(peerNode);
            cfgSetup.encodeAssocIe(peerSector);
        }

        // Create prerequisite link failure report indication
        MeshNCReportInd prereqReportInd = new MeshNCReportInd();
        prereqReportInd.setLinkFailureContent(MeshNCReportInd.REPORT_LINK_FAILURE,
                                              peerSectMac, ownSectMac);
        prereqReportInd.setConfigNodeId(peerNode.getNodeId());

        // Serialize ASSOC CFG message with prerequisite
        serMsg.addMeshMsg(cfgSetup);
        serMsg.addPrerequisite(prereqReportInd);
        meshDatabase.addSerializedAssocMsg(serMsg);

        return true;
    }


    /**
     *
     * @param nodeMac
     * @param nodeId
     */
    private MeshNode createMeshNode(long nodeMac, long nodeId) {
        logger.trace("new node (" + HexString.toHexString(nodeMac) + ") version " + version);

        // Retrieve pre-configured VLAN INFO
        VlanInfo vlanInfo = meshRoutingManager.getVlanInfo(nodeMac);
        if (vlanInfo == null) {
            throw new AssertionError("vlanInfo == null, need to update the config file");
        }

        // Create new mesh node
        MeshNode meshNode = new MeshNode(
                nodeMac, meshDatabase.getGwNode(), 0, nodeId, vlanInfo, version,
                (byte)1, (short)-1, (short)-1, MeshNode.CONNECTING, null, null);

        // Add new mesh node to DB
        meshDatabase.addMeshNode(meshNode);
        return meshNode;
    }

    /**
     *
     * @param deviceId
     * @return
     */
    private boolean disconnectMeshDevice(DeviceId deviceId) {
        if (deviceId != null) {
            MeshProvider provider = getProvider(deviceId);
            if (provider != null) {
                provider.disconnectDevice(deviceId);
            } else {
                logger.debug("Provider not found for {}", deviceId);
                return false;
            }
            return true;
        } else {
            logger.info("Device Id is null");
        }
        return false;
    }

    /**
     *
     * @param deviceId
     * @return
     */
    private boolean disconnectAssociatedDevice(DeviceId deviceId) {
        if (deviceId != null) {
            deviceAdminService.removeDevice(deviceId);
            logger.info("Device to remove: " + HexString.toHexString(convertDeviceIdToMac(deviceId)));
            return true;
        }
        logger.info("Device Id is null");
        return false;
    }


    /**
     * Creates a Mesh Link device provider
     */
    private class MeshLinkProvider extends AbstractProvider implements LinkProvider {
        public MeshLinkProvider() {
            super(new ProviderId(SCHEME, "org.onosproject.mesh.link"));
        }
    }

    /**
     *
     */
    private class MeshMessageQueue implements Runnable {
        private boolean runnable = true;

        @Override
        public void run() {
            logger.info("Mesh Message queue initiated");
            while (runnable == true) {
                try {
                    MeshMessage meshMessage = messageQ.take();
                    logger.debug("getting a message from the queue");
                    handleAllMessages(meshMessage);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        public void stop() {
           runnable = false;
        }  
    }

    /**
     * Listens to device events and processes the link additions
     */
    private class InternalDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
            // Handle ADDED or REMOVED device events
            if ((event.type() != DeviceEvent.Type.DEVICE_ADDED) &&
                    (event.type() != DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED)) {
                return;
            }

            // Retrieve Device ID
            DeviceId deviceId = event.subject().id();

            // Process device add/remove based on device availability
            MeshDeviceEvent msg;
            if (deviceService.isAvailable(deviceId)) {
                msg = new MeshDeviceAdded(deviceId);
            } else {
                msg = new MeshDeviceRemoved(deviceId);
            }
            addToMsgQ(msg);
        }
    }

    /**
     * Packet processor for incoming packets.
     */
    private class InternalPacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            // Stop processing if the packet has been handled, since we
            // can't do any more to it.

            logger.debug("receiving a message in packet processor");
            if (context.isHandled()) {
                return;
            }
            logger.debug("receiving and processing a message in packet processor");


            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();
            if (ethPkt == null) {
                return;
            }
            logger.trace(ethPkt.toString());
            ByteBuffer bb = pkt.unparsed();
            bb.position(14); //we jump over the ethernet packet
            //we jump over the parsed info(ethernet buffer)

            logger.trace("PACKETIN: handlePacketIn");
            bb.mark();

            MeshSBIMessageFactory mmfactory = new MeshSBIMessageFactory();
            MeshMessage message = mmfactory.getMeshMessage(bb);

            logger.debug("Message Decoded: " + message);
            if (message != null) {
                addToMsgQ(message);
            }
        }
    }

    /**
     * Listens to link events and processes the link additions
     */
    private class InternalLinkListener implements LinkListener {

        @Override
        public void event(LinkEvent event) {
            if (event.type() == LinkEvent.Type.LINK_ADDED) {
                logger.debug("LINK_ADDED in MESH");
                Link link = event.subject();
                LinkDescription linkDescReverse = new DefaultLinkDescription(
                        link.dst(), link.src(), Link.Type.DIRECT);
                linkProviderService.linkDetected(linkDescReverse);
            } else if (event.type() == LinkEvent.Type.LINK_REMOVED) {
                logger.debug("LINK_REMOVED in MESH : " + event.subject().toString());
            }
        }
    }

    /**
     * This class could send events up to any listeners, but I am allowing the
     * possibility to go straight to the MeshManager as we don't want all control
     * messages and their content to be manipulated by other application listeners
     */
    private class InternalMeshProviderService
            extends AbstractProviderService<MeshProvider>
            implements MeshProviderService {

        MeshService meshService;

        InternalMeshProviderService(MeshProvider provider) {
            super(provider);
        }

        InternalMeshProviderService(MeshProvider provider, MeshService meshManager) {
            super(provider);
            meshService = meshManager;
        }

        public void createMeshEvent(List<MeshEvent> reasons) {

            for (MeshEvent event : reasons) {
                //choose event based on reasons in a switch case, return the event
                if (event != null) {
                    logger.debug("Creating mesh event {}", event.subject());
                    post(event);
                }
            }
        }

        @Override
        public void handleMessage(DeviceId id, MeshSBIMessage msg) {
            // not sure if we need the devideId for something, it doesn't really matter although
            // we could do some mapping here
            meshService.addToMsgQ(msg);
        }

        @Override
        public void deviceConnected(DeviceId id) {
            logger.info("Device connected: " + id.toString());
        }

        @Override
        public void deviceDisconnected(DeviceId id) {
            logger.info("Device disconnected: " + id.toString());
        }
    }
}
