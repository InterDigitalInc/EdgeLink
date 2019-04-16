/*
 * Copyright 2017-present Open Networking Laboratory
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

package org.onosproject.routing.cpr;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.EthType;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip6Address;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.app.ApplicationService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.incubator.net.intf.Interface;
import org.onosproject.incubator.net.intf.InterfaceService;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.PortNumber;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.DefaultNextObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.NextObjective;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.host.InterfaceIpAddress;
import org.onosproject.routing.AsyncDeviceFetcher;
import org.onosproject.routing.RouterInterfaceManager;
import org.onosproject.routing.RoutingService;
import org.onosproject.routing.config.RouterConfig;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static org.onlab.packet.Ethernet.TYPE_ARP;
import static org.onlab.packet.Ethernet.TYPE_IPV4;
import static org.onlab.packet.Ethernet.TYPE_IPV6;
import static org.onlab.packet.ICMP6.NEIGHBOR_ADVERTISEMENT;
import static org.onlab.packet.ICMP6.NEIGHBOR_SOLICITATION;
import static org.onlab.packet.IPv6.PROTOCOL_ICMP6;
import static org.onlab.packet.IPv6.getLinkLocalAddress;
import static org.onlab.packet.IPv6.getSolicitNodeAddress;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Manages connectivity between peers redirecting control traffic to a routing
 * control plane available on the dataplane.
 */
@Component(immediate = true)
public class ControlPlaneRedirectManager {

    private final Logger log = getLogger(getClass());

    public static final short ASSIGNED_VLAN = 4094;

    private static final int MIN_IP_PRIORITY = 10;
    private static final int IPV4_PRIORITY = 2000;
    private static final int IPV6_PRIORITY = 500;
    static final int ACL_PRIORITY = 40001;
    private static final int OSPF_IP_PROTO = 0x59;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected InterfaceService interfaceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigService networkConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ApplicationService applicationService;

    private static final String APP_NAME = "org.onosproject.cpr";
    private ApplicationId appId;

    private ConnectPoint controlPlaneConnectPoint;
    private boolean ospfEnabled = false;
    private Map<Host, Set<Integer>> peerNextId = Maps.newConcurrentMap();

    private RouterInterfaceManager interfaceManager;
    private AsyncDeviceFetcher asyncDeviceFetcher;

    private final InternalNetworkConfigListener networkConfigListener =
            new InternalNetworkConfigListener();
    private final InternalHostListener hostListener = new InternalHostListener();

    @Activate
    protected void activate() {
        this.appId = coreService.registerApplication(APP_NAME);

        networkConfigService.addListener(networkConfigListener);
        hostService.addListener(hostListener);

        asyncDeviceFetcher = AsyncDeviceFetcher.create(deviceService);

        processRouterConfig();

        applicationService.registerDeactivateHook(this.appId, () -> {
            if (interfaceManager != null) {
                interfaceManager.cleanup();
            }
        });
    }

    @Deactivate
    protected void deactivate() {
        networkConfigService.removeListener(networkConfigListener);
        hostService.removeListener(hostListener);
        asyncDeviceFetcher.shutdown();
    }

    /**
     * Sets up the router interfaces if router config is available.
     */
    private void processRouterConfig() {
        ApplicationId routingAppId =
                coreService.registerApplication(RoutingService.ROUTER_APP_ID);

        RouterConfig config = networkConfigService.getConfig(
                routingAppId, RoutingService.ROUTER_CONFIG_CLASS);

        if (config == null) {
            log.warn("Router config not available");
            return;
        }

        if (interfaceManager == null) {
            controlPlaneConnectPoint = config.getControlPlaneConnectPoint();
            ospfEnabled = config.getOspfEnabled();

            DeviceId deviceId = config.getControlPlaneConnectPoint().deviceId();

            asyncDeviceFetcher.getDevice(deviceId)
                    .thenAccept(deviceId1 ->
                            interfaceManager = createRouter(deviceId,
                                    Sets.newHashSet(config.getInterfaces())));

        } else {
            interfaceManager.changeConfiguredInterfaces(Sets.newHashSet(config.getInterfaces()));
        }
    }

    /**
     * Cleans up after router config was removed.
     */
    private void removeRouterConfig() {
        if (interfaceManager != null) {
            interfaceManager.cleanup();
        }
    }

    private RouterInterfaceManager createRouter(DeviceId deviceId, Set<String> configuredInterfaces) {
        return new RouterInterfaceManager(deviceId,
                configuredInterfaces,
                interfaceService,
                this::provisionInterface,
                this::unprovisionInterface);
    }

    private void provisionInterface(Interface intf) {
        updateInterfaceObjectives(intf, true);
    }

    private void unprovisionInterface(Interface intf) {
        updateInterfaceObjectives(intf, false);
    }

    /**
     * Installs or removes flow objectives relating to a give interface.
     *
     * @param intf interface to change objectives for
     * @param install true to install the objectives, false to remove them
     */
    private void updateInterfaceObjectives(Interface intf, boolean install) {
        updateInterfaceForwarding(intf, install);
        updateOspfForwarding(intf, install);
    }

    /**
     * Installs or removes the basic forwarding flows for each interface.
     *
     * @param intf the Interface on which event is received
     * @param install true to install the objectives, false to remove them
     */
    private void updateInterfaceForwarding(Interface intf, boolean install) {
        log.debug("{} interface objectives for {}", operation(install), intf);

        DeviceId deviceId = intf.connectPoint().deviceId();
        PortNumber controlPlanePort = controlPlaneConnectPoint.port();
        for (InterfaceIpAddress ip : intf.ipAddresses()) {
            // create nextObjectives for forwarding to this interface and the
            // controlPlaneConnectPoint
            int cpNextId, intfNextId;
            if (intf.vlan() == VlanId.NONE) {
                cpNextId = modifyNextObjective(deviceId, controlPlanePort,
                               VlanId.vlanId(ASSIGNED_VLAN),
                               true, install);
                intfNextId = modifyNextObjective(deviceId, intf.connectPoint().port(),
                               VlanId.vlanId(ASSIGNED_VLAN),
                               true, install);
            } else {
                cpNextId = modifyNextObjective(deviceId, controlPlanePort,
                                               intf.vlan(), false, install);
                intfNextId = modifyNextObjective(deviceId, intf.connectPoint().port(),
                                                 intf.vlan(), false, install);
            }
            List<ForwardingObjective> fwdToSend = Lists.newArrayList();
            TrafficSelector selector;
            // IP traffic toward the router.
            selector = buildIPDstSelector(
                    ip.ipAddress().toIpPrefix(),
                    intf.connectPoint().port(),
                    null,
                    intf.mac(),
                    intf.vlan()
            );
            fwdToSend.add(buildForwardingObjective(selector, null, cpNextId, install, ACL_PRIORITY));
            // IP traffic from the router.
            selector = buildIPSrcSelector(
                    ip.ipAddress().toIpPrefix(),
                    controlPlanePort,
                    intf.mac(),
                    null,
                    intf.vlan()
            );
            fwdToSend.add(buildForwardingObjective(selector, null, intfNextId, install, ACL_PRIORITY));
            // We build the punt treatment.
            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .punt()
                    .build();
            // Handling of neighbour discovery protocols.
            // IPv4 traffic - we have to deal with the ARP protocol.
            // IPv6 traffic - we have to deal with the NDP protocol.
            if (ip.ipAddress().isIp4()) {
                 // ARP traffic towards the router.
                selector = buildArpSelector(
                        intf.connectPoint().port(),
                        intf.vlan(),
                        null,
                        null
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, cpNextId, install, ACL_PRIORITY + 1));
                // ARP traffic from the router.
                selector = buildArpSelector(
                        controlPlanePort,
                        intf.vlan(),
                        ip.ipAddress().getIp4Address(),
                        intf.mac()
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, intfNextId, install, ACL_PRIORITY + 1));
            } else {
                // Neighbour solicitation traffic towards the router.
                // This flow is for the global unicast address.
                selector = buildNdpSelector(
                        intf.connectPoint().port(),
                        intf.vlan(),
                        null,
                        ip.ipAddress().toIpPrefix(),
                        NEIGHBOR_SOLICITATION,
                        null
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, cpNextId, install, ACL_PRIORITY + 1));
                // Neighbour solicitation traffic towards the router.
                // This flow is for the link local address.
                selector = buildNdpSelector(
                        intf.connectPoint().port(),
                        intf.vlan(),
                        null,
                        Ip6Address.valueOf(getLinkLocalAddress(intf.mac().toBytes())).toIpPrefix(),
                        NEIGHBOR_SOLICITATION,
                        null
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, cpNextId, install, ACL_PRIORITY + 1));
                // Neighbour solicitation traffic towards the router.
                // This flow is for the solicitation node address of
                // the global unicast address.
                selector = buildNdpSelector(
                        intf.connectPoint().port(),
                        intf.vlan(),
                        null,
                        Ip6Address.valueOf(getSolicitNodeAddress(ip.ipAddress().toOctets())).toIpPrefix(),
                        NEIGHBOR_SOLICITATION,
                        null
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, cpNextId, install, ACL_PRIORITY + 1));
                // Neighbour solicitation traffic towards the router.
                // This flow is for the solicitation node address of
                // the link local address.
                selector = buildNdpSelector(
                        intf.connectPoint().port(),
                        intf.vlan(),
                        null,
                        Ip6Address.valueOf(
                                getSolicitNodeAddress(getLinkLocalAddress(intf.mac().toBytes()))
                        ).toIpPrefix(),
                        NEIGHBOR_SOLICITATION,
                        null
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, cpNextId, install, ACL_PRIORITY + 1));
                // Neighbour solicitation traffic from the router.
                // This flow is for the global unicast address.
                selector = buildNdpSelector(
                        controlPlanePort,
                        intf.vlan(),
                        ip.ipAddress().toIpPrefix(),
                        null,
                        NEIGHBOR_SOLICITATION,
                        intf.mac()
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, intfNextId, install, ACL_PRIORITY + 1));
                // Neighbour solicitation traffic from the router.
                // This flow is for the link local address.
                selector = buildNdpSelector(
                        controlPlanePort,
                        intf.vlan(),
                        Ip6Address.valueOf(getLinkLocalAddress(intf.mac().toBytes())).toIpPrefix(),
                        null,
                        NEIGHBOR_SOLICITATION,
                        intf.mac()
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, intfNextId, install, ACL_PRIORITY + 1));
                // Neighbour advertisement traffic towards the router.
                // This flow is for the global unicast address
                selector = buildNdpSelector(
                        intf.connectPoint().port(),
                        intf.vlan(),
                        null,
                        ip.ipAddress().toIpPrefix(),
                        NEIGHBOR_ADVERTISEMENT,
                        null
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, cpNextId, install, ACL_PRIORITY + 1));
                // Neighbour advertisement traffic towards the router.
                // This flow is for the link local address
                selector = buildNdpSelector(
                        intf.connectPoint().port(),
                        intf.vlan(),
                        null,
                        Ip6Address.valueOf(getLinkLocalAddress(intf.mac().toBytes())).toIpPrefix(),
                        NEIGHBOR_ADVERTISEMENT,
                        null
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, cpNextId, install, ACL_PRIORITY + 1));
                // Neighbour advertisement traffic from the router.
                // This flow is for the global unicast address
                selector = buildNdpSelector(
                        controlPlanePort,
                        intf.vlan(),
                        ip.ipAddress().toIpPrefix(),
                        null,
                        NEIGHBOR_ADVERTISEMENT,
                        intf.mac()
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, intfNextId, install, ACL_PRIORITY + 1));
                // Neighbour advertisement traffic from the router.
                // This flow is for the link local address
                selector = buildNdpSelector(
                        controlPlanePort,
                        intf.vlan(),
                        Ip6Address.valueOf(getLinkLocalAddress(intf.mac().toBytes())).toIpPrefix(),
                        null,
                        NEIGHBOR_ADVERTISEMENT,
                        intf.mac()
                );
                fwdToSend.add(buildForwardingObjective(selector, treatment, intfNextId, install, ACL_PRIORITY + 1));
            }
            // Finally we push the fwd objectives through the flow objective service.
            fwdToSend.stream().forEach(forwardingObjective ->
                flowObjectiveService.forward(deviceId, forwardingObjective)
            );
        }
    }

    /**
     * Installs or removes OSPF forwarding rules.
     *
     * @param intf the interface on which event is received
     * @param install true to create an add objective, false to create a remove
     *            objective
     */
    private void updateOspfForwarding(Interface intf, boolean install) {
        // TODO IPv6 support has not been implemented yet

        log.debug("{} OSPF flows for {}", operation(install), intf);

        // OSPF to router
        TrafficSelector toSelector = DefaultTrafficSelector.builder()
                .matchInPort(intf.connectPoint().port())
                .matchEthType(EthType.EtherType.IPV4.ethType().toShort())
                .matchVlanId(intf.vlan())
                .matchIPProtocol((byte) OSPF_IP_PROTO)
                .build();

        // create nextObjectives for forwarding to the controlPlaneConnectPoint
        DeviceId deviceId = intf.connectPoint().deviceId();
        PortNumber controlPlanePort = controlPlaneConnectPoint.port();
        int cpNextId;
        if (intf.vlan() == VlanId.NONE) {
            cpNextId = modifyNextObjective(deviceId, controlPlanePort,
                           VlanId.vlanId(ASSIGNED_VLAN),
                           true, install);
        } else {
            cpNextId = modifyNextObjective(deviceId, controlPlanePort,
                                           intf.vlan(), false, install);
        }
        flowObjectiveService.forward(intf.connectPoint().deviceId(),
                buildForwardingObjective(toSelector, null, cpNextId,
                        install ? ospfEnabled : install, ACL_PRIORITY));
    }

    /**
     * Creates a next objective for forwarding to a port. Handles metadata for
     * some pipelines that require vlan information for egress port.
     *
     * @param deviceId the device on which the next objective is being created
     * @param portNumber the egress port
     * @param vlanId vlan information for egress port
     * @param popVlan if vlan tag should be popped or not
     * @param install true to create an add next objective, false to create a remove
     *            next objective
     * @return nextId of the next objective created
     */
    private int modifyNextObjective(DeviceId deviceId, PortNumber portNumber,
                                    VlanId vlanId, boolean popVlan, boolean install) {
        int nextId = flowObjectiveService.allocateNextId();
        NextObjective.Builder nextObjBuilder = DefaultNextObjective
                .builder().withId(nextId)
                .withType(NextObjective.Type.SIMPLE)
                .fromApp(appId);

        TrafficTreatment.Builder ttBuilder = DefaultTrafficTreatment.builder();
        if (popVlan) {
            ttBuilder.popVlan();
        }
        ttBuilder.setOutput(portNumber);

        // setup metadata to pass to nextObjective - indicate the vlan on egress
        // if needed by the switch pipeline.
        TrafficSelector.Builder metabuilder = DefaultTrafficSelector.builder();
        metabuilder.matchVlanId(vlanId);

        nextObjBuilder.withMeta(metabuilder.build());
        nextObjBuilder.addTreatment(ttBuilder.build());
        log.debug("Submitted next objective {} in device {} for port/vlan {}/{}",
                nextId, deviceId, portNumber, vlanId);
        if (install) {
             flowObjectiveService.next(deviceId, nextObjBuilder.add());
        } else {
             flowObjectiveService.next(deviceId, nextObjBuilder.remove());
        }
        return nextId;
    }

    /**
     * Builds a forwarding objective from the given selector, treatment and nextId.
     *
     * @param selector selector
     * @param treatment treatment to apply to packet, can be null
     * @param nextId next objective to point to for forwarding packet
     * @param add true to create an add objective, false to create a remove
     *            objective
     * @return forwarding objective
     */
    private ForwardingObjective buildForwardingObjective(TrafficSelector selector,
                                                         TrafficTreatment treatment,
                                                         int nextId,
                                                         boolean add,
                                                         int priority) {
        DefaultForwardingObjective.Builder fobBuilder = DefaultForwardingObjective.builder();
        fobBuilder.withSelector(selector);
        if (treatment != null) {
            fobBuilder.withTreatment(treatment);
        }
        if (nextId != -1) {
            fobBuilder.nextStep(nextId);
        }
        fobBuilder.fromApp(appId)
            .withPriority(priority)
            .withFlag(ForwardingObjective.Flag.VERSATILE);

        return add ? fobBuilder.add() : fobBuilder.remove();
    }

    static TrafficSelector.Builder buildBaseSelectorBuilder(PortNumber inPort,
                                                            MacAddress srcMac,
                                                            MacAddress dstMac,
                                                            VlanId vlanId) {
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        if (inPort != null) {
            selectorBuilder.matchInPort(inPort);
        }
        if (srcMac != null) {
            selectorBuilder.matchEthSrc(srcMac);
        }
        if (dstMac != null) {
            selectorBuilder.matchEthDst(dstMac);
        }
        if (vlanId != null) {
            selectorBuilder.matchVlanId(vlanId);
        }
        return selectorBuilder;
    }

    static TrafficSelector buildIPDstSelector(IpPrefix dstIp,
                                              PortNumber inPort,
                                              MacAddress srcMac,
                                              MacAddress dstMac,
                                              VlanId vlanId) {
        TrafficSelector.Builder selector = buildBaseSelectorBuilder(inPort, srcMac, dstMac, vlanId);
        if (dstIp.isIp4()) {
            selector.matchEthType(TYPE_IPV4);
            selector.matchIPDst(dstIp);
        } else {
            selector.matchEthType(TYPE_IPV6);
            selector.matchIPv6Dst(dstIp);
        }
        return selector.build();
    }

    static TrafficSelector buildIPSrcSelector(IpPrefix srcIp,
                                              PortNumber inPort,
                                              MacAddress srcMac,
                                              MacAddress dstMac,
                                              VlanId vlanId) {
        TrafficSelector.Builder selector = buildBaseSelectorBuilder(inPort, srcMac, dstMac, vlanId);
        if (srcIp.isIp4()) {
            selector.matchEthType(TYPE_IPV4);
            selector.matchIPSrc(srcIp);
        } else {
            selector.matchEthType(TYPE_IPV6);
            selector.matchIPv6Src(srcIp);
        }
        return selector.build();
    }

    static TrafficSelector buildArpSelector(PortNumber inPort,
                                            VlanId vlanId,
                                            Ip4Address arpSpa,
                                            MacAddress srcMac) {
        TrafficSelector.Builder selector = buildBaseSelectorBuilder(inPort, null, null, vlanId);
        selector.matchEthType(TYPE_ARP);
        if (arpSpa != null) {
            selector.matchArpSpa(arpSpa);
        }
        if (srcMac != null) {
            selector.matchEthSrc(srcMac);
        }
        return selector.build();
    }

    static TrafficSelector buildNdpSelector(PortNumber inPort,
                                            VlanId vlanId,
                                            IpPrefix srcIp,
                                            IpPrefix dstIp,
                                            byte subProto,
                                            MacAddress srcMac) {
        TrafficSelector.Builder selector = buildBaseSelectorBuilder(inPort, null, null, vlanId);
        selector.matchEthType(TYPE_IPV6)
                .matchIPProtocol(PROTOCOL_ICMP6)
                .matchIcmpv6Type(subProto);
        if (srcIp != null) {
            selector.matchIPv6Src(srcIp);
        }
        if (dstIp != null) {
            selector.matchIPv6Dst(dstIp);
        }
        if (srcMac != null) {
            selector.matchEthSrc(srcMac);
        }
        return selector.build();
    }

    private int getPriorityFromPrefix(IpPrefix prefix) {
        return (prefix.isIp4()) ?
               IPV4_PRIORITY * prefix.prefixLength() + MIN_IP_PRIORITY :
               IPV6_PRIORITY * prefix.prefixLength() + MIN_IP_PRIORITY;
    }

    private String operation(boolean install) {
        return install ? "Installing" : "Removing";
    }


    /**
     * Listener for network config events.
     */
    private class InternalNetworkConfigListener implements NetworkConfigListener {

        @Override
        public void event(NetworkConfigEvent event) {
            if (event.configClass().equals(RoutingService.ROUTER_CONFIG_CLASS)) {
                switch (event.type()) {
                    case CONFIG_ADDED:
                    case CONFIG_UPDATED:
                        processRouterConfig();
                        break;
                    case CONFIG_REGISTERED:
                        break;
                    case CONFIG_UNREGISTERED:
                        break;
                    case CONFIG_REMOVED:
                        removeRouterConfig();
                        break;
                default:
                    break;
                }
            }
        }
    }

    /**
     * Listener for host events.
     */
    private class InternalHostListener implements HostListener {

        private Optional<Interface> getPeerInterface(Host peer) {
            return interfaceService.getInterfacesByPort(peer.location()).stream()
                    .filter(intf -> interfaceManager.configuredInterfaces().isEmpty()
                            || interfaceManager.configuredInterfaces().contains(intf.name()))
                    .filter(intf -> peer.vlan().equals(intf.vlan()))
                    .findFirst();
        }

        private void peerAdded(HostEvent event) {
            Host peer = event.subject();
            if (interfaceManager == null) {
                return;
            }

            Optional<Interface> peerIntf = getPeerInterface(peer);
            if (!peerIntf.isPresent()) {
                log.debug("Adding peer {}/{} on {} but the interface is not configured",
                        peer.mac(), peer.vlan(), peer.location());
                return;
            }

            // Generate L3 Unicast group for the traffic towards vRouter
            // XXX This approach will change with the HA design
            int toRouterL3Unicast = createPeerGroup(peer.mac(), peerIntf.get().mac(),
                    peer.vlan(), peer.location().deviceId(), controlPlaneConnectPoint.port());
            // Generate L3 Unicast group for the traffic towards the upStream
            // XXX This approach will change with the HA design
            int toPeerL3Unicast = createPeerGroup(peerIntf.get().mac(), peer.mac(),
                    peer.vlan(), peer.location().deviceId(), peer.location().port());
            // Store the next objectives in the map
            peerNextId.put(peer, ImmutableSortedSet.of(toRouterL3Unicast, toPeerL3Unicast));

            // From peer to router
            peerIntf.get().ipAddresses().forEach(routerIp -> {
                flowObjectiveService.forward(peer.location().deviceId(),
                        createPeerObjBuilder(toRouterL3Unicast, routerIp.ipAddress().toIpPrefix()).add());
            });

            // From router to peer
            peer.ipAddresses().forEach(peerIp -> {
                flowObjectiveService.forward(peer.location().deviceId(),
                        createPeerObjBuilder(toPeerL3Unicast, peerIp.toIpPrefix()).add());
            });
        }

        private void peerRemoved(HostEvent event) {
            Host peer = event.subject();
            Optional<Interface> peerIntf = getPeerInterface(peer);
            if (!peerIntf.isPresent()) {
                log.debug("Removing peer {}/{} on {} but the interface is not configured",
                        peer.mac(), peer.vlan(), peer.location());
                return;
            }

            checkState(peerNextId.get(peer) != null,
                    "Peer nextId should not be null");
            checkState(peerNextId.get(peer).size() == 2,
                    "Wrong nextId associated with the peer");
            Iterator<Integer> iter = peerNextId.get(peer).iterator();
            int toRouterL3Unicast = iter.next();
            int toPeerL3Unicast = iter.next();

            // From peer to router
            peerIntf.get().ipAddresses().forEach(routerIp -> {
                flowObjectiveService.forward(peer.location().deviceId(),
                        createPeerObjBuilder(toRouterL3Unicast, routerIp.ipAddress().toIpPrefix()).remove());
            });

            // From router to peer
            peer.ipAddresses().forEach(peerIp -> {
                flowObjectiveService.forward(peer.location().deviceId(),
                        createPeerObjBuilder(toPeerL3Unicast, peerIp.toIpPrefix()).remove());
            });
        }

        private ForwardingObjective.Builder createPeerObjBuilder(
                int nextId, IpPrefix ipAddresses) {
            TrafficSelector selector = buildIPDstSelector(ipAddresses, null, null, null, null);
            DefaultForwardingObjective.Builder builder =
                    DefaultForwardingObjective.builder()
                    .withSelector(selector)
                    .fromApp(appId)
                    .withPriority(getPriorityFromPrefix(ipAddresses))
                    .withFlag(ForwardingObjective.Flag.SPECIFIC);
            if (nextId != -1) {
                builder.nextStep(nextId);
            }
            return builder;
        }

        private int createPeerGroup(MacAddress srcMac, MacAddress dstMac,
                VlanId vlanId, DeviceId deviceId, PortNumber port) {
            int nextId = flowObjectiveService.allocateNextId();
            NextObjective.Builder nextObjBuilder = DefaultNextObjective.builder()
                    .withId(nextId)
                    .withType(NextObjective.Type.SIMPLE)
                    .fromApp(appId);

            TrafficTreatment.Builder ttBuilder = DefaultTrafficTreatment.builder();
            ttBuilder.setEthSrc(srcMac);
            ttBuilder.setEthDst(dstMac);
            ttBuilder.setOutput(port);
            nextObjBuilder.addTreatment(ttBuilder.build());

            TrafficSelector.Builder metabuilder = DefaultTrafficSelector.builder();
            VlanId matchVlanId = (vlanId.equals(VlanId.NONE)) ?
                    VlanId.vlanId(ASSIGNED_VLAN) :
                    vlanId;
            metabuilder.matchVlanId(matchVlanId);
            nextObjBuilder.withMeta(metabuilder.build());

            flowObjectiveService.next(deviceId, nextObjBuilder.add());
            return nextId;
        }

        @Override
        public void event(HostEvent event) {
            DeviceId deviceId = event.subject().location().deviceId();
            if (!mastershipService.isLocalMaster(deviceId)) {
                return;
            }
            switch (event.type()) {
                case HOST_ADDED:
                    peerAdded(event);
                    break;
                case HOST_MOVED:
                    //TODO We assume BGP peer does not move for now
                    break;
                case HOST_REMOVED:
                    peerRemoved(event);
                    break;
                case HOST_UPDATED:
                    //FIXME We assume BGP peer does not change IP for now
                    // but we can discover new address.
                    break;
                default:
                    break;
            }
        }
    }

}

