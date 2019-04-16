/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.segmentrouting;

import com.google.common.collect.Maps;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.Ethernet;
import org.onlab.packet.ICMP6;
import org.onlab.packet.IPv4;
import org.onlab.packet.IPv6;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.VlanId;
import org.onlab.util.KryoNamespace;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.event.Event;
import org.onosproject.incubator.net.config.basics.ConfigException;
import org.onosproject.incubator.net.config.basics.InterfaceConfig;
import org.onosproject.incubator.net.config.basics.McastConfig;
import org.onosproject.incubator.net.intf.Interface;
import org.onosproject.incubator.net.intf.InterfaceService;
import org.onosproject.incubator.net.routing.RouteEvent;
import org.onosproject.incubator.net.routing.RouteListener;
import org.onosproject.incubator.net.routing.RouteService;
import org.onosproject.incubator.net.neighbour.NeighbourResolutionService;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.mcast.McastEvent;
import org.onosproject.net.mcast.McastListener;
import org.onosproject.net.mcast.MulticastRouteService;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.routing.config.RouterConfig;
import org.onosproject.segmentrouting.config.DeviceConfigNotFoundException;
import org.onosproject.segmentrouting.config.DeviceConfiguration;
import org.onosproject.segmentrouting.config.PwaasConfig;
import org.onosproject.segmentrouting.config.SegmentRoutingDeviceConfig;
import org.onosproject.segmentrouting.config.SegmentRoutingAppConfig;
import org.onosproject.segmentrouting.config.XConnectConfig;
import org.onosproject.segmentrouting.grouphandler.DefaultGroupHandler;
import org.onosproject.segmentrouting.grouphandler.NeighborSet;
import org.onosproject.segmentrouting.storekey.NeighborSetNextObjectiveStoreKey;
import org.onosproject.segmentrouting.storekey.PortNextObjectiveStoreKey;
import org.onosproject.segmentrouting.storekey.SubnetAssignedVidStoreKey;
import org.onosproject.segmentrouting.storekey.SubnetNextObjectiveStoreKey;
import org.onosproject.segmentrouting.storekey.XConnectStoreKey;
import org.onosproject.segmentrouting.pwaas.L2TunnelHandler;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.EventuallyConsistentMapBuilder;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.opencord.cordconfig.CordConfigEvent;
import org.opencord.cordconfig.CordConfigListener;
import org.opencord.cordconfig.CordConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static org.onlab.packet.Ethernet.TYPE_ARP;
import static org.onlab.util.Tools.groupedThreads;

/**
 * Segment routing manager.
 */
@Service
@Component(immediate = true)
public class SegmentRoutingManager implements SegmentRoutingService {

    private static Logger log = LoggerFactory.getLogger(SegmentRoutingManager.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ComponentConfigService compCfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NeighbourResolutionService neighbourResolutionService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    public PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    public FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    public MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    public StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    MulticastRouteService multicastRouteService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    CordConfigService cordConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    RouteService routeService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    public NetworkConfigRegistry cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    public InterfaceService interfaceService;

    ArpHandler arpHandler = null;
    IcmpHandler icmpHandler = null;
    IpHandler ipHandler = null;
    RoutingRulePopulator routingRulePopulator = null;
    public ApplicationId appId;
    public DeviceConfiguration deviceConfiguration = null;

    DefaultRoutingHandler defaultRoutingHandler = null;
    private TunnelHandler tunnelHandler = null;
    private PolicyHandler policyHandler = null;
    private InternalPacketProcessor processor = null;
    private InternalLinkListener linkListener = null;
    private InternalDeviceListener deviceListener = null;
    private AppConfigHandler appCfgHandler = null;
    XConnectHandler xConnectHandler = null;
    private McastHandler mcastHandler = null;
    HostHandler hostHandler = null;
    private CordConfigHandler cordConfigHandler = null;
    private RouteHandler routeHandler = null;
    private SegmentRoutingNeighbourDispatcher neighbourHandler = null;
    private L2TunnelHandler l2TunnelHandler = null;
    private InternalEventHandler eventHandler = new InternalEventHandler();
    private final InternalHostListener hostListener = new InternalHostListener();
    private final InternalConfigListener cfgListener = new InternalConfigListener(this);
    private final InternalMcastListener mcastListener = new InternalMcastListener();
    private final InternalCordConfigListener cordConfigListener = new InternalCordConfigListener();
    private final InternalRouteEventListener routeListener = new InternalRouteEventListener();

    private ScheduledExecutorService executorService = Executors
            .newScheduledThreadPool(1, groupedThreads("SegmentRoutingManager", "event-%d", log));

    @SuppressWarnings("unused")
    private static ScheduledFuture<?> eventHandlerFuture = null;
    @SuppressWarnings("rawtypes")
    private ConcurrentLinkedQueue<Event> eventQueue = new ConcurrentLinkedQueue<>();
    private Map<DeviceId, DefaultGroupHandler> groupHandlerMap =
            new ConcurrentHashMap<>();
    /**
     * Per device next objective ID store with (device id + neighbor set) as key.
     */
    public EventuallyConsistentMap<NeighborSetNextObjectiveStoreKey, Integer>
            nsNextObjStore = null;
    /**
     * Per device next objective ID store with (device id + subnet) as key.
     */
    public EventuallyConsistentMap<SubnetNextObjectiveStoreKey, Integer>
            subnetNextObjStore = null;
    /**
     * Per device next objective ID store with (device id + port) as key.
     */
    public EventuallyConsistentMap<PortNextObjectiveStoreKey, Integer>
            portNextObjStore = null;
    // Per device, per-subnet assigned-vlans store, with (device id + subnet
    // IPv4 prefix) as key
    private EventuallyConsistentMap<SubnetAssignedVidStoreKey, VlanId>
            subnetVidStore = null;
    private EventuallyConsistentMap<String, Tunnel> tunnelStore = null;
    private EventuallyConsistentMap<String, Policy> policyStore = null;

    private final ConfigFactory<DeviceId, SegmentRoutingDeviceConfig> deviceConfigFactory =
            new ConfigFactory<DeviceId, SegmentRoutingDeviceConfig>(
                    SubjectFactories.DEVICE_SUBJECT_FACTORY,
                    SegmentRoutingDeviceConfig.class, "segmentrouting") {
                @Override
                public SegmentRoutingDeviceConfig createConfig() {
                    return new SegmentRoutingDeviceConfig();
                }
            };

    private final ConfigFactory<ApplicationId, SegmentRoutingAppConfig> appConfigFactory =
            new ConfigFactory<ApplicationId, SegmentRoutingAppConfig>(
                    SubjectFactories.APP_SUBJECT_FACTORY,
                    SegmentRoutingAppConfig.class, "segmentrouting") {
                @Override
                public SegmentRoutingAppConfig createConfig() {
                    return new SegmentRoutingAppConfig();
                }
            };

    private final ConfigFactory<ApplicationId, XConnectConfig> xConnectConfigFactory =
            new ConfigFactory<ApplicationId, XConnectConfig>(
                    SubjectFactories.APP_SUBJECT_FACTORY,
                    XConnectConfig.class, "xconnect") {
                @Override
                public XConnectConfig createConfig() {
                    return new XConnectConfig();
                }
            };

    private ConfigFactory<ApplicationId, McastConfig> mcastConfigFactory =
            new ConfigFactory<ApplicationId, McastConfig>(
                    SubjectFactories.APP_SUBJECT_FACTORY,
                    McastConfig.class, "multicast") {
                @Override
                public McastConfig createConfig() {
                    return new McastConfig();
                }
            };

    private final ConfigFactory<ApplicationId, PwaasConfig> pwaasConfigFactory =
            new ConfigFactory<ApplicationId, PwaasConfig>(
                    SubjectFactories.APP_SUBJECT_FACTORY,
                    PwaasConfig.class, "pwaas") {
                @Override
                public PwaasConfig createConfig() {
                    return new PwaasConfig();
                }
            };

    private Object threadSchedulerLock = new Object();
    private static int numOfEventsQueued = 0;
    private static int numOfEventsExecuted = 0;
    private static int numOfHandlerExecution = 0;
    private static int numOfHandlerScheduled = 0;

    /**
     * Segment Routing App ID.
     */
    public static final String APP_NAME = "org.onosproject.segmentrouting";
    /**
     * The starting value of per-subnet VLAN ID assignment.
     */
    private static final short ASSIGNED_VLAN_START = 4093;
    /**
     * The default VLAN ID assigned to the interfaces without subnet config.
     */
    public static final short ASSIGNED_VLAN_NO_SUBNET = 4094;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(APP_NAME);

        log.debug("Creating EC map nsnextobjectivestore");
        EventuallyConsistentMapBuilder<NeighborSetNextObjectiveStoreKey, Integer>
                nsNextObjMapBuilder = storageService.eventuallyConsistentMapBuilder();
        nsNextObjStore = nsNextObjMapBuilder
                .withName("nsnextobjectivestore")
                .withSerializer(createSerializer())
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();
        log.trace("Current size {}", nsNextObjStore.size());

        log.debug("Creating EC map subnetnextobjectivestore");
        EventuallyConsistentMapBuilder<SubnetNextObjectiveStoreKey, Integer>
                subnetNextObjMapBuilder = storageService.eventuallyConsistentMapBuilder();
        subnetNextObjStore = subnetNextObjMapBuilder
                .withName("subnetnextobjectivestore")
                .withSerializer(createSerializer())
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();

        log.debug("Creating EC map subnetnextobjectivestore");
        EventuallyConsistentMapBuilder<PortNextObjectiveStoreKey, Integer>
                portNextObjMapBuilder = storageService.eventuallyConsistentMapBuilder();
        portNextObjStore = portNextObjMapBuilder
                .withName("portnextobjectivestore")
                .withSerializer(createSerializer())
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();

        EventuallyConsistentMapBuilder<String, Tunnel> tunnelMapBuilder =
                storageService.eventuallyConsistentMapBuilder();
        tunnelStore = tunnelMapBuilder
                .withName("tunnelstore")
                .withSerializer(createSerializer())
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();

        EventuallyConsistentMapBuilder<String, Policy> policyMapBuilder =
                storageService.eventuallyConsistentMapBuilder();
        policyStore = policyMapBuilder
                .withName("policystore")
                .withSerializer(createSerializer())
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();

        EventuallyConsistentMapBuilder<SubnetAssignedVidStoreKey, VlanId>
            subnetVidStoreMapBuilder = storageService.eventuallyConsistentMapBuilder();
        subnetVidStore = subnetVidStoreMapBuilder
                .withName("subnetvidstore")
                .withSerializer(createSerializer())
                .withTimestampProvider((k, v) -> new WallClockTimestamp())
                .build();

        compCfgService.preSetProperty("org.onosproject.net.group.impl.GroupManager",
                                      "purgeOnDisconnection", "true");
        compCfgService.preSetProperty("org.onosproject.net.flow.impl.FlowRuleManager",
                                      "purgeOnDisconnection", "true");
        compCfgService.preSetProperty("org.onosproject.provider.host.impl.HostLocationProvider",
                                      "requestInterceptsEnabled", "false");
        compCfgService.preSetProperty("org.onosproject.incubator.net.neighbour.impl.NeighbourResolutionManager",
                                      "requestInterceptsEnabled", "false");
        compCfgService.preSetProperty("org.onosproject.dhcprelay.DhcpRelay",
                                      "arpEnabled", "false");
        compCfgService.preSetProperty("org.onosproject.net.host.impl.HostManager",
                                      "greedyLearningIpv6", "true");


        processor = new InternalPacketProcessor();
        linkListener = new InternalLinkListener();
        deviceListener = new InternalDeviceListener();
        appCfgHandler = new AppConfigHandler(this);
        xConnectHandler = new XConnectHandler(this);
        mcastHandler = new McastHandler(this);
        hostHandler = new HostHandler(this);
        cordConfigHandler = new CordConfigHandler(this);
        routeHandler = new RouteHandler(this);
        neighbourHandler = new SegmentRoutingNeighbourDispatcher(this);
        l2TunnelHandler = new L2TunnelHandler(this);

        cfgService.addListener(cfgListener);
        cfgService.registerConfigFactory(deviceConfigFactory);
        cfgService.registerConfigFactory(appConfigFactory);
        cfgService.registerConfigFactory(xConnectConfigFactory);
        cfgService.registerConfigFactory(mcastConfigFactory);
        cfgService.registerConfigFactory(pwaasConfigFactory);
        hostService.addListener(hostListener);
        packetService.addProcessor(processor, PacketProcessor.director(2));
        linkService.addListener(linkListener);
        deviceService.addListener(deviceListener);
        multicastRouteService.addListener(mcastListener);
        cordConfigService.addListener(cordConfigListener);

        cfgListener.configureNetwork();

        routeService.addListener(routeListener);

        log.info("Started");
    }

    private KryoNamespace.Builder createSerializer() {
        return new KryoNamespace.Builder()
                .register(KryoNamespaces.API)
                .register(NeighborSetNextObjectiveStoreKey.class,
                        SubnetNextObjectiveStoreKey.class,
                        SubnetAssignedVidStoreKey.class,
                        NeighborSet.class,
                        Tunnel.class,
                        DefaultTunnel.class,
                        Policy.class,
                        TunnelPolicy.class,
                        Policy.Type.class,
                        PortNextObjectiveStoreKey.class,
                        XConnectStoreKey.class
                );
    }

    @Deactivate
    protected void deactivate() {
        cfgService.removeListener(cfgListener);
        cfgService.unregisterConfigFactory(deviceConfigFactory);
        cfgService.unregisterConfigFactory(appConfigFactory);
        cfgService.unregisterConfigFactory(xConnectConfigFactory);
        cfgService.unregisterConfigFactory(mcastConfigFactory);
        cfgService.unregisterConfigFactory(pwaasConfigFactory);

        packetService.removeProcessor(processor);
        linkService.removeListener(linkListener);
        deviceService.removeListener(deviceListener);
        multicastRouteService.removeListener(mcastListener);
        cordConfigService.removeListener(cordConfigListener);
        routeService.removeListener(routeListener);

        processor = null;
        linkListener = null;
        deviceListener = null;
        groupHandlerMap.clear();

        nsNextObjStore.destroy();
        subnetNextObjStore.destroy();
        portNextObjStore.destroy();
        tunnelStore.destroy();
        policyStore.destroy();
        subnetVidStore.destroy();
        log.info("Stopped");
    }

    @Override
    public List<Tunnel> getTunnels() {
        return tunnelHandler.getTunnels();
    }

    @Override
    public TunnelHandler.Result createTunnel(Tunnel tunnel) {
        return tunnelHandler.createTunnel(tunnel);
    }

    @Override
    public TunnelHandler.Result removeTunnel(Tunnel tunnel) {
        for (Policy policy: policyHandler.getPolicies()) {
            if (policy.type() == Policy.Type.TUNNEL_FLOW) {
                TunnelPolicy tunnelPolicy = (TunnelPolicy) policy;
                if (tunnelPolicy.tunnelId().equals(tunnel.id())) {
                    log.warn("Cannot remove the tunnel used by a policy");
                    return TunnelHandler.Result.TUNNEL_IN_USE;
                }
            }
        }
        return tunnelHandler.removeTunnel(tunnel);
    }

    @Override
    public PolicyHandler.Result removePolicy(Policy policy) {
        return policyHandler.removePolicy(policy);
    }

    @Override
    public PolicyHandler.Result createPolicy(Policy policy) {
        return policyHandler.createPolicy(policy);
    }

    @Override
    public List<Policy> getPolicies() {
        return policyHandler.getPolicies();
    }

    @Override
    public void rerouteNetwork() {
        cfgListener.configureNetwork();
        for (Device device : deviceService.getDevices()) {
            defaultRoutingHandler.populatePortAddressingRules(device.id());
        }
        defaultRoutingHandler.startPopulationProcess();
    }

    @Override
    public Map<DeviceId, Set<IpPrefix>> getDeviceSubnetMap() {
        Map<DeviceId, Set<IpPrefix>> deviceSubnetMap = Maps.newHashMap();
        deviceService.getAvailableDevices().forEach(device -> {
            deviceSubnetMap.put(device.id(), deviceConfiguration.getSubnets(device.id()));
        });
        return deviceSubnetMap;
    }

    /**
     * Returns the MPLS-ECMP configuration.
     *
     * @return MPLS-ECMP value
     */
    public boolean getMplsEcmp() {
        SegmentRoutingAppConfig segmentRoutingAppConfig = cfgService
                .getConfig(this.appId, SegmentRoutingAppConfig.class);
        return segmentRoutingAppConfig != null && segmentRoutingAppConfig.mplsEcmp();
    }

    /**
     * Returns the tunnel object with the tunnel ID.
     *
     * @param tunnelId Tunnel ID
     * @return Tunnel reference
     */
    public Tunnel getTunnel(String tunnelId) {
        return tunnelHandler.getTunnel(tunnelId);
    }

    /**
     * Returns the vlan-id assigned to the subnet configured for a device.
     * If no vlan-id has been assigned, a new one is assigned out of a pool of ids,
     * if and only if this controller instance is the master for the device.
     * <p>
     * USAGE: The assigned vlans are meant to be applied to untagged packets on those
     * switches/pipelines that need this functionality. These vids are meant
     * to be used internally within a switch, and thus need to be unique only
     * on a switch level. Note that packets never go out on the wire with these
     * vlans. Currently, vlan ids are assigned from value 4093 down.
     * Vlan id 4094 expected to be used for all ports that are not assigned subnets.
     * Vlan id 4095 is reserved and unused. Only a single vlan id is assigned
     * per subnet.
     *
     * @param deviceId switch dpid
     * @param subnet IP prefix for which assigned vlan is desired
     * @return VlanId assigned for the subnet on the device, or
     *         null if no vlan assignment was found and this instance is not
     *         the master for the device.
     */
    // TODO: We should avoid assigning VLAN IDs that are used by VLAN cross-connection.
    public VlanId getSubnetAssignedVlanId(DeviceId deviceId, IpPrefix subnet) {
        VlanId assignedVid = subnetVidStore.get(new SubnetAssignedVidStoreKey(
                                                        deviceId, subnet));
        if (assignedVid != null) {
            log.debug("Query for subnet:{} on device:{} returned assigned-vlan "
                    + "{}", subnet, deviceId, assignedVid);
            return assignedVid;
        }
        //check mastership for the right to assign a vlan
        if (!mastershipService.isLocalMaster(deviceId)) {
            log.warn("This controller instance is not the master for device {}. "
                    + "Cannot assign vlan-id for subnet {}", deviceId, subnet);
            return null;
        }
        // vlan assignment is expensive but done only once
        // FIXME for now we will do assignment considering only the ipv4 subnet.
        Set<Ip4Prefix> configuredSubnets = deviceConfiguration.getSubnets(deviceId)
                .stream()
                .filter(IpPrefix::isIp4)
                .map(IpPrefix::getIp4Prefix)
                .collect(Collectors.toSet());
        Set<Short> assignedVlans = new HashSet<>();
        Set<Ip4Prefix> unassignedSubnets = new HashSet<>();
        for (Ip4Prefix sub : configuredSubnets) {
            VlanId v = subnetVidStore.get(new SubnetAssignedVidStoreKey(deviceId,
                                                                        sub));
            if (v != null) {
                assignedVlans.add(v.toShort());
            } else {
                unassignedSubnets.add(sub);
            }
        }
        short nextAssignedVlan = ASSIGNED_VLAN_START;
        if (!assignedVlans.isEmpty()) {
            nextAssignedVlan = (short) (Collections.min(assignedVlans) - 1);
        }
        for (Ip4Prefix unsub : unassignedSubnets) {
            subnetVidStore.put(new SubnetAssignedVidStoreKey(deviceId, unsub),
                    VlanId.vlanId(nextAssignedVlan--));
            log.info("Assigned vlan: {} to subnet: {} on device: {}",
                    nextAssignedVlan + 1, unsub, deviceId);
        }
        return subnetVidStore.get(new SubnetAssignedVidStoreKey(deviceId, subnet));
    }

    /**
     * Returns the next objective ID for the given NeighborSet.
     * If the nextObjective does not exist, a new one is created and
     * its id is returned.
     *
     * @param deviceId Device ID
     * @param ns NegighborSet
     * @param meta metadata passed into the creation of a Next Objective
     * @param isBos indicates if it is BoS or not
     * @return next objective ID or -1 if an error was encountered during the
     *         creation of the nextObjective
     */
    public int getNextObjectiveId(DeviceId deviceId, NeighborSet ns,
                                  TrafficSelector meta, boolean isBos) {
        if (groupHandlerMap.get(deviceId) != null) {
            log.trace("getNextObjectiveId query in device {}", deviceId);
            return groupHandlerMap
                    .get(deviceId).getNextObjectiveId(ns, meta, isBos);
        } else {
            log.warn("getNextObjectiveId query - groupHandler for device {} "
                    + "not found", deviceId);
            return -1;
        }
    }

    /**
     * Returns the next objective ID for the given NeighborSet.
     * If the nextObjective does not exist, a new one is created and
     * its id is returned.
     *
     * @param deviceId Device ID
     * @param ns NegighborSet
     * @param meta metadata passed into the creation of a Next Objective
     * @return next objective ID or -1 if an error was encountered during the
     *         creation of the nextObjective
     */
    public int getNextObjectiveId(DeviceId deviceId,
                                  NeighborSet ns,
                                  TrafficSelector meta) {
        return this.getNextObjectiveId(deviceId, ns, meta, true);
    }

    /**
     * Returns the next objective ID for the given subnet prefix. It is expected
     * that the next-objective has been pre-created from configuration.
     *
     * @param deviceId Device ID
     * @param prefix Subnet
     * @return next objective ID or -1 if it was not found
     */
    public int getSubnetNextObjectiveId(DeviceId deviceId, IpPrefix prefix) {
        if (groupHandlerMap.get(deviceId) != null) {
            log.trace("getSubnetNextObjectiveId query in device {}", deviceId);
            return groupHandlerMap
                    .get(deviceId).getSubnetNextObjectiveId(prefix);
        } else {
            log.warn("getSubnetNextObjectiveId query - groupHandler for "
                    + "device {} not found", deviceId);
            return -1;
        }
    }

    /**
     * Returns the next objective ID for the given portNumber, given the treatment.
     * There could be multiple different treatments to the same outport, which
     * would result in different objectives. If the next object
     * does not exist, a new one is created and its id is returned.
     *
     * @param deviceId Device ID
     * @param portNum port number on device for which NextObjective is queried
     * @param treatment the actions to apply on the packets (should include outport)
     * @param meta metadata passed into the creation of a Next Objective if necessary
     * @return next objective ID or -1 if an error occurred during retrieval or creation
     */
    public int getPortNextObjectiveId(DeviceId deviceId, PortNumber portNum,
                                      TrafficTreatment treatment,
                                      TrafficSelector meta) {
        DefaultGroupHandler ghdlr = groupHandlerMap.get(deviceId);
        if (ghdlr != null) {
            return ghdlr.getPortNextObjectiveId(portNum, treatment, meta);
        } else {
            log.warn("getPortNextObjectiveId query - groupHandler for device {}"
                    + " not found", deviceId);
            return -1;
        }
    }

    private class InternalPacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {

            if (context.isHandled()) {
                return;
            }

            InboundPacket pkt = context.inPacket();
            Ethernet ethernet = pkt.parsed();

            if (ethernet == null) {
                return;
            }

            log.trace("Rcvd pktin: {}", ethernet);
            if (ethernet.getEtherType() == TYPE_ARP) {
                log.warn("{} - we are still receiving ARP packets from {}",
                         context.inPacket().receivedFrom());
                log.debug("{}", ethernet);
                return;
            } else if (ethernet.getEtherType() == Ethernet.TYPE_IPV4) {
                IPv4 ipv4Packet = (IPv4) ethernet.getPayload();
                //ipHandler.addToPacketBuffer(ipv4Packet);
                if (ipv4Packet.getProtocol() == IPv4.PROTOCOL_ICMP) {
                    icmpHandler.processIcmp(ethernet, pkt.receivedFrom());
                } else {
                    // NOTE: We don't support IP learning at this moment so this
                    //       is not necessary. Also it causes duplication of DHCP packets.
                    // ipHandler.processPacketIn(ipv4Packet, pkt.receivedFrom());
                }
            } else if (ethernet.getEtherType() == Ethernet.TYPE_IPV6) {
                IPv6 ipv6Packet = (IPv6) ethernet.getPayload();
                //ipHandler.addToPacketBuffer(ipv6Packet);
                // We deal with the packet only if the packet is a ICMP6 ECHO/REPLY
                if (ipv6Packet.getNextHeader() == IPv6.PROTOCOL_ICMP6) {
                    ICMP6 icmp6Packet = (ICMP6) ipv6Packet.getPayload();
                    if (icmp6Packet.getIcmpType() == ICMP6.ECHO_REQUEST ||
                            icmp6Packet.getIcmpType() == ICMP6.ECHO_REPLY) {
                        icmpHandler.processIcmpv6(ethernet, pkt.receivedFrom());
                    } else {
                        // XXX Neigbour hacking, to handle the ICMPv6 packet
                        // not under our control
                        if (icmpHandler.handleUPstreamPackets(context)) {
                            log.debug("Rcvd pktin from UpStream: {}", ipv6Packet);
                        } else {
                            log.debug("Received ICMPv6 0x{} - not handled",
                                     Integer.toHexString(icmp6Packet.getIcmpType() & 0xff));
                        }
                    }
                } else {
                   // NOTE: We don't support IP learning at this moment so this
                   //       is not necessary. Also it causes duplication of DHCPv6 packets.
                   // ipHandler.processPacketIn(ipv6Packet, pkt.receivedFrom());
                }
            }
        }
    }

    private class InternalLinkListener implements LinkListener {
        @Override
        public void event(LinkEvent event) {
            if (event.type() == LinkEvent.Type.LINK_ADDED
                    || event.type() == LinkEvent.Type.LINK_REMOVED) {
                log.debug("Event {} received from Link Service", event.type());
                scheduleEventHandlerIfNotScheduled(event);
            }
        }
    }

    private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            switch (event.type()) {
            case DEVICE_ADDED:
            case PORT_UPDATED:
            case PORT_ADDED:
            case DEVICE_UPDATED:
            case DEVICE_AVAILABILITY_CHANGED:
                log.debug("Event {} received from Device Service", event.type());
                scheduleEventHandlerIfNotScheduled(event);
                break;
            default:
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void scheduleEventHandlerIfNotScheduled(Event event) {
        synchronized (threadSchedulerLock) {
            eventQueue.add(event);
            numOfEventsQueued++;

            if ((numOfHandlerScheduled - numOfHandlerExecution) == 0) {
                //No pending scheduled event handling threads. So start a new one.
                eventHandlerFuture = executorService
                        .schedule(eventHandler, 100, TimeUnit.MILLISECONDS);
                numOfHandlerScheduled++;
            }
            log.trace("numOfEventsQueued {}, numOfEventHandlerScheduled {}",
                      numOfEventsQueued,
                      numOfHandlerScheduled);
        }
    }

    private class InternalEventHandler implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    @SuppressWarnings("rawtypes")
                    Event event = null;
                    synchronized (threadSchedulerLock) {
                        if (!eventQueue.isEmpty()) {
                            event = eventQueue.poll();
                            numOfEventsExecuted++;
                        } else {
                            numOfHandlerExecution++;
                            log.debug("numOfHandlerExecution {} numOfEventsExecuted {}",
                                      numOfHandlerExecution, numOfEventsExecuted);
                            break;
                        }
                    }
                    if (event.type() == LinkEvent.Type.LINK_ADDED) {
                        processLinkAdded((Link) event.subject());
                    } else if (event.type() == LinkEvent.Type.LINK_REMOVED) {
                        Link linkRemoved = (Link) event.subject();
                        if (linkRemoved.src().elementId() instanceof DeviceId &&
                                !deviceService.isAvailable(linkRemoved.src().deviceId())) {
                            continue;
                        }
                        if (linkRemoved.dst().elementId() instanceof DeviceId &&
                                !deviceService.isAvailable(linkRemoved.dst().deviceId())) {
                            continue;
                        }
                        processLinkRemoved((Link) event.subject());
                    } else if (event.type() == DeviceEvent.Type.DEVICE_ADDED ||
                            event.type() == DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED ||
                            event.type() == DeviceEvent.Type.DEVICE_UPDATED) {
                        DeviceId deviceId = ((Device) event.subject()).id();
                        if (deviceService.isAvailable(deviceId)) {
                            log.info("Processing device event {} for available device {}",
                                     event.type(), ((Device) event.subject()).id());
                            processDeviceAdded((Device) event.subject());
                        } else {
                            log.info("Processing device event {} for unavailable device {}",
                                    event.type(), ((Device) event.subject()).id());
                            processDeviceRemoved((Device) event.subject());
                        }
                    } else if (event.type() == DeviceEvent.Type.PORT_ADDED) {
                        // typically these calls come when device is added first time
                        // so port filtering rules are handled at the device_added event.
                        // port added calls represent all ports on the device,
                        // enabled or not.
                        log.debug("** PORT ADDED {}/{} -> {}",
                                  ((DeviceEvent) event).subject().id(),
                                  ((DeviceEvent) event).port().number(),
                                  event.type());
                    } else if (event.type() == DeviceEvent.Type.PORT_UPDATED) {
                        // these calls happen for every subsequent event
                        // ports enabled, disabled, switch goes away, comes back
                        log.info("** PORT UPDATED {}/{} -> {}",
                                 event.subject(),
                                 ((DeviceEvent) event).port(),
                                 event.type());
                        processPortUpdated(((Device) event.subject()),
                                           ((DeviceEvent) event).port());
                    } else {
                        log.warn("Unhandled event type: {}", event.type());
                    }
                }
            } catch (Exception e) {
                log.error("SegmentRouting event handler "
                        + "thread thrown an exception: {}", e);
            }
        }
    }

    private void processLinkAdded(Link link) {
        log.info("** LINK ADDED {}", link.toString());
        if (!deviceConfiguration.isConfigured(link.src().deviceId())) {
            log.warn("Source device of this link is not configured.");
            return;
        }
        //Irrespective whether the local is a MASTER or not for this device,
        //create group handler instance and push default TTP flow rules.
        //Because in a multi-instance setup, instances can initiate
        //groups for any devices. Also the default TTP rules are needed
        //to be pushed before inserting any IP table entries for any device
        DefaultGroupHandler groupHandler = groupHandlerMap.get(link.src()
                .deviceId());
        if (groupHandler != null) {
            groupHandler.linkUp(link, mastershipService.isLocalMaster(
                                           link.src().deviceId()));
        } else {
            Device device = deviceService.getDevice(link.src().deviceId());
            if (device != null) {
                log.warn("processLinkAdded: Link Added "
                        + "Notification without Device Added "
                        + "event, still handling it");
                processDeviceAdded(device);
                groupHandler = groupHandlerMap.get(link.src()
                                                   .deviceId());
                groupHandler.linkUp(link, mastershipService.isLocalMaster(device.id()));
            }
        }

        log.trace("Starting optimized route population process");
        defaultRoutingHandler.populateRoutingRulesForLinkStatusChange(null);
        //log.trace("processLinkAdded: re-starting route population process");
        //defaultRoutingHandler.startPopulationProcess();

        mcastHandler.init();
    }

    private void processLinkRemoved(Link link) {
        log.info("** LINK REMOVED {}", link.toString());
        DefaultGroupHandler groupHandler = groupHandlerMap.get(link.src().deviceId());
        if (groupHandler != null) {
            groupHandler.portDown(link.src().port(),
                                  mastershipService.isLocalMaster(link.src().deviceId()));
        }
        log.trace("Starting optimized route population process");
        defaultRoutingHandler.populateRoutingRulesForLinkStatusChange(link);
        //log.trace("processLinkRemoved: re-starting route population process");
        //defaultRoutingHandler.startPopulationProcess();

        mcastHandler.processLinkDown(link);
    }

    private void processDeviceAdded(Device device) {
        log.info("** DEVICE ADDED with ID {}", device.id());
        if (deviceConfiguration == null || !deviceConfiguration.isConfigured(device.id())) {
            log.warn("Device configuration uploading. Device {} will be "
                    + "processed after config completes.", device.id());
            return;
        }
        processDeviceAddedInternal(device.id());
    }

    private void processDeviceAddedInternal(DeviceId deviceId) {
        // Irrespective of whether the local is a MASTER or not for this device,
        // we need to create a SR-group-handler instance. This is because in a
        // multi-instance setup, any instance can initiate forwarding/next-objectives
        // for any switch (even if this instance is a SLAVE or not even connected
        // to the switch). To handle this, a default-group-handler instance is necessary
        // per switch.
        log.debug("Current groupHandlerMap devs: {}", groupHandlerMap.keySet());
        if (groupHandlerMap.get(deviceId) == null) {
            DefaultGroupHandler groupHandler;
            try {
                groupHandler = DefaultGroupHandler.
                        createGroupHandler(deviceId,
                                appId,
                                deviceConfiguration,
                                linkService,
                                flowObjectiveService,
                                this);
            } catch (DeviceConfigNotFoundException e) {
                log.warn(e.getMessage() + " Aborting processDeviceAdded.");
                return;
            }
            log.debug("updating groupHandlerMap with new config for device: {}",
                    deviceId);
            groupHandlerMap.put(deviceId, groupHandler);
        }
        // Also, in some cases, drivers may need extra
        // information to process rules (eg. Router IP/MAC); and so, we send
        // port addressing rules to the driver as well irrespective of whether
        // this instance is the master or not.
        defaultRoutingHandler.populatePortAddressingRules(deviceId);

        if (mastershipService.isLocalMaster(deviceId)) {
            hostHandler.init(deviceId);
            xConnectHandler.init(deviceId);
            cordConfigHandler.init(deviceId);
            DefaultGroupHandler groupHandler = groupHandlerMap.get(deviceId);
            groupHandler.createGroupsFromSubnetConfig();
            routingRulePopulator.populateSubnetBroadcastRule(deviceId);
        }

        appCfgHandler.init(deviceId);
        routeHandler.init(deviceId);
    }

    private void processDeviceRemoved(Device device) {
        nsNextObjStore.entrySet().stream()
                .filter(entry -> entry.getKey().deviceId().equals(device.id()))
                .forEach(entry -> {
                    nsNextObjStore.remove(entry.getKey());
                });
        subnetNextObjStore.entrySet().stream()
                .filter(entry -> entry.getKey().deviceId().equals(device.id()))
                .forEach(entry -> {
                    subnetNextObjStore.remove(entry.getKey());
                });
        portNextObjStore.entrySet().stream()
                .filter(entry -> entry.getKey().deviceId().equals(device.id()))
                .forEach(entry -> {
                    portNextObjStore.remove(entry.getKey());
                });
        subnetVidStore.entrySet().stream()
                .filter(entry -> entry.getKey().deviceId().equals(device.id()))
                .forEach(entry -> {
                    subnetVidStore.remove(entry.getKey());
                });
        groupHandlerMap.remove(device.id());
        defaultRoutingHandler.purgeEcmpGraph(device.id());
        mcastHandler.removeDevice(device.id());
        xConnectHandler.removeDevice(device.id());
    }

    private void processPortUpdated(Device device, Port port) {
        if (deviceConfiguration == null || !deviceConfiguration.isConfigured(device.id())) {
            log.warn("Device configuration uploading. Not handling port event for"
                    + "dev: {} port: {}", device.id(), port.number());
            return;
        }
        /* XXX create method for single port filtering rules which are needed
           for both switch-to-switch ports and edge ports
        if (defaultRoutingHandler != null) {
            defaultRoutingHandler.populatePortAddressingRules(
                ((Device) event.subject()).id());
        }*/

        // portUpdated calls are for ports that have gone down or up. For switch
        // to switch ports, link-events should take care of any re-routing or
        // group editing necessary for port up/down. Here we only process edge ports
        // that are already configured.
         Ip4Prefix configuredSubnet = deviceConfiguration.getPortIPv4Subnet(device.id(),
                                                                        port.number());
        if (configuredSubnet == null) {
            log.debug("Not handling port updated event for unconfigured port "
                    + "dev/port: {}/{}", device.id(), port.number());
            return;
        }
        processEdgePort(device, port, configuredSubnet);
    }

    private void processEdgePort(Device device, Port port, Ip4Prefix subnet) {
        boolean portUp = port.isEnabled();
        if (portUp) {
            log.info("Device:EdgePort {}:{} is enabled in subnet: {}", device.id(),
                     port.number(), subnet);
        } else {
            log.info("Device:EdgePort {}:{} is disabled in subnet: {}", device.id(),
                     port.number(), subnet);
        }

        DefaultGroupHandler groupHandler = groupHandlerMap.get(device.id());
        if (groupHandler != null) {
            groupHandler.processEdgePort(port.number(), subnet, portUp,
                                  mastershipService.isLocalMaster(device.id()));
        } else {
            log.warn("Group handler not found for dev:{}. Not handling edge port"
                    + " {} event for port:{}", device.id(),
                    (portUp) ? "UP" : "DOWN", port.number());
        }
    }

    /**
     * Registers the given connect point with the NRS, this is necessary
     * to receive the NDP and ARP packets from the NRS.
     *
     * @param portToRegister connect point to register
     */
    public void registerConnectPoint(ConnectPoint portToRegister) {
        this.neighbourResolutionService.registerNeighbourHandler(
                portToRegister,
                neighbourHandler,
                appId
        );
    }

    private class InternalConfigListener implements NetworkConfigListener {
        SegmentRoutingManager srManager;

        /**
         * Constructs the internal network config listener.
         *
         * @param srManager segment routing manager
         */
        public InternalConfigListener(SegmentRoutingManager srManager) {
            this.srManager = srManager;
        }

        /**
         * Reads network config and initializes related data structure accordingly.
         */
        public void configureNetwork() {

            deviceConfiguration = new DeviceConfiguration(srManager);

            arpHandler = new ArpHandler(srManager);
            icmpHandler = new IcmpHandler(srManager);
            ipHandler = new IpHandler(srManager);
            routingRulePopulator = new RoutingRulePopulator(srManager);
            defaultRoutingHandler = new DefaultRoutingHandler(srManager);

            tunnelHandler = new TunnelHandler(linkService, deviceConfiguration,
                                              groupHandlerMap, tunnelStore);
            policyHandler = new PolicyHandler(appId, deviceConfiguration,
                                              flowObjectiveService,
                                              tunnelHandler, policyStore);

            for (Device device : deviceService.getDevices()) {
                processDeviceAddedInternal(device.id());
            }

            defaultRoutingHandler.startPopulationProcess();
            mcastHandler.init();
        }

        @Override
        public void event(NetworkConfigEvent event) {
            // TODO move this part to NetworkConfigEventHandler
            if (event.configClass().equals(SegmentRoutingDeviceConfig.class)) {
                switch (event.type()) {
                    case CONFIG_ADDED:
                        log.info("Segment Routing Config added.");
                        configureNetwork();
                        break;
                    case CONFIG_UPDATED:
                        log.info("Segment Routing Config updated.");
                        // TODO support dynamic configuration
                        break;
                    default:
                        break;
                }
            } else if (event.configClass().equals(SegmentRoutingAppConfig.class)) {
                checkState(appCfgHandler != null, "NetworkConfigEventHandler is not initialized");
                switch (event.type()) {
                    case CONFIG_ADDED:
                        appCfgHandler.processAppConfigAdded(event);
                        break;
                    case CONFIG_UPDATED:
                        appCfgHandler.processAppConfigUpdated(event);
                        break;
                    case CONFIG_REMOVED:
                        appCfgHandler.processAppConfigRemoved(event);
                        break;
                    default:
                        break;
                }
                configureNetwork();
            } else if (event.configClass().equals(XConnectConfig.class)) {
                checkState(xConnectHandler != null, "XConnectHandler is not initialized");
                switch (event.type()) {
                    case CONFIG_ADDED:
                        xConnectHandler.processXConnectConfigAdded(event);
                        break;
                    case CONFIG_UPDATED:
                        xConnectHandler.processXConnectConfigUpdated(event);
                        break;
                    case CONFIG_REMOVED:
                        xConnectHandler.processXConnectConfigRemoved(event);
                        break;
                    default:
                        break;
                }
                // XXX Neighbour hacking. This method is looking for
                // the Internet-Router interface. In order to retrieve
                // the upstream port.
            } else if (event.configClass().equals(InterfaceConfig.class)) {
                switch (event.type()) {
                    case CONFIG_ADDED:
                    case CONFIG_UPDATED:
                        updateUPstreamCP();
                    case CONFIG_REGISTERED:
                    case CONFIG_UNREGISTERED:
                    case CONFIG_REMOVED:
                        break;
                    default:
                        break;
                }
                // XXX Neighbour hacking. This method is looking for
                // the vrouter port.
            } else if (event.configClass().equals(RouterConfig.class)) {
                switch (event.type()) {
                    case CONFIG_ADDED:
                    case CONFIG_UPDATED:
                        updateVRouterCP(event);
                    case CONFIG_REGISTERED:
                    case CONFIG_UNREGISTERED:
                    case CONFIG_REMOVED:
                        break;
                    default:
                        break;
                }
            } else if (event.configClass().equals(PwaasConfig.class)) {
                checkState(l2TunnelHandler != null, "L2TunnelHandler is not initialized");
                switch (event.type()) {
                    case CONFIG_ADDED:
                        l2TunnelHandler.processPwaasConfigAdded(event);
                        break;
                    case CONFIG_UPDATED:
                        l2TunnelHandler.processPwaasConfigUpdated(event);
                        break;
                    case CONFIG_REMOVED:
                        l2TunnelHandler.processPwaasConfigRemoved(event);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    //    XXX Neighbour hacking, temporary workaround will be      //
    //    removed as soon as possible, when the bridging will      //
    //    be implemented. For now, it's fine to leave this         //
    /////////////////////////////////////////////////////////////////

    // XXX Neighbour hacking. To store upstream connect
    // point and vRouter connect point
    ConnectPoint upstreamCP = null;
    ConnectPoint vRouterCP = null;

    // XXX Neighbour hacking. To update the Upstream CP
    public void updateUPstreamCP() {
        Set<ConnectPoint> portSubjects = cfgService.getSubjects(ConnectPoint.class, InterfaceConfig.class);
        upstreamCP = null;
        portSubjects.stream().forEach(subject -> {
            InterfaceConfig config = cfgService.getConfig(subject, InterfaceConfig.class);
            Set<Interface> networkInterfaces;
            try {
                networkInterfaces = config.getInterfaces();
            } catch (ConfigException e) {
                log.error("Error loading port configuration");
                return;
            }
            networkInterfaces.forEach(networkInterface -> {
                if (networkInterface.name().equals("internet-router")) {
                    upstreamCP = subject;
                }
            });
        });

    }

    // XXX Neighbour hacking. To update the Upstream CP
    public void updateVRouterCP(NetworkConfigEvent event) {
        RouterConfig config = (RouterConfig) event.config().get();
        if (config == null) {
            log.warn("Router config not available");
            vRouterCP = null;
            return;
        }
        vRouterCP = config.getControlPlaneConnectPoint();
    }

    private class InternalHostListener implements HostListener {
        @Override
        public void event(HostEvent event) {
            // Do not proceed without mastership
            DeviceId deviceId = event.subject().location().deviceId();
            if (!mastershipService.isLocalMaster(deviceId)) {
                return;
            }

            switch (event.type()) {
                case HOST_ADDED:
                    hostHandler.processHostAddedEvent(event);
                    break;
                case HOST_MOVED:
                    hostHandler.processHostMovedEvent(event);
                    break;
                case HOST_REMOVED:
                    hostHandler.processHostRemoveEvent(event);
                    break;
                case HOST_UPDATED:
                    hostHandler.processHostUpdatedEvent(event);
                    break;
                default:
                    log.warn("Unsupported host event type: {}", event.type());
                    break;
            }
        }
    }

    private class InternalMcastListener implements McastListener {
        @Override
        public void event(McastEvent event) {
            switch (event.type()) {
                case SOURCE_ADDED:
                    mcastHandler.processSourceAdded(event);
                    break;
                case SINK_ADDED:
                    mcastHandler.processSinkAdded(event);
                    break;
                case SINK_REMOVED:
                    mcastHandler.processSinkRemoved(event);
                    break;
                case ROUTE_ADDED:
                case ROUTE_REMOVED:
                default:
                    break;
            }
        }
    }

    private class InternalCordConfigListener implements CordConfigListener {
        @Override
        public void event(CordConfigEvent event) {
            switch (event.type()) {
                case ACCESS_AGENT_ADDED:
                    cordConfigHandler.processAccessAgentAddedEvent(event);
                    break;
                case ACCESS_AGENT_UPDATED:
                    cordConfigHandler.processAccessAgentUpdatedEvent(event);
                    break;
                case ACCESS_AGENT_REMOVED:
                    cordConfigHandler.processAccessAgentRemovedEvent(event);
                    break;
                case ACCESS_DEVICE_ADDED:
                case ACCESS_DEVICE_UPDATED:
                case ACCESS_DEVICE_REMOVED:
                default:
                    break;
            }
        }
    }

    private class InternalRouteEventListener implements RouteListener {
        @Override
        public void event(RouteEvent event) {
            switch (event.type()) {
                case ROUTE_ADDED:
                    routeHandler.processRouteAdded(event);
                    break;
                case ROUTE_UPDATED:
                    routeHandler.processRouteUpdated(event);
                    break;
                case ROUTE_REMOVED:
                    routeHandler.processRouteRemoved(event);
                    break;
                default:
                    break;
            }
        }
    }
}
