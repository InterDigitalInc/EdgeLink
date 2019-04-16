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

import org.onlab.packet.Ethernet;
import org.onlab.packet.ICMP;
import org.onlab.packet.ICMP6;
import org.onlab.packet.IPv4;
import org.onlab.packet.IPv6;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip6Address;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MPLS;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onlab.packet.ndp.NeighborSolicitation;
import org.onosproject.incubator.net.neighbour.NeighbourMessageContext;
import org.onosproject.incubator.net.neighbour.NeighbourMessageType;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostService;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.segmentrouting.config.DeviceConfigNotFoundException;
import org.onosproject.segmentrouting.config.SegmentRoutingAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Set;

/**
 * Handler of ICMP packets that responses or forwards ICMP packets that
 * are sent to the controller.
 */
public class IcmpHandler extends SegmentRoutingNeighbourHandler {

    private static Logger log = LoggerFactory.getLogger(IcmpHandler.class);

    /**
     * Creates an IcmpHandler object.
     *
     * @param srManager SegmentRoutingManager object
     */
    public IcmpHandler(SegmentRoutingManager srManager) {
        super(srManager);
    }

    /**
     * Utility function to send packet out.
     *
     * @param outport the output port
     * @param payload the packet to send
     * @param sid the segment id
     * @param destIpAddress the destination ip address
     * @param allowedHops the hop limit/ttl
     */
    private void sendPacketOut(ConnectPoint outport,
                               Ethernet payload,
                               int sid,
                               IpAddress destIpAddress,
                               byte allowedHops) {
        int destSid;
        if (destIpAddress.isIp4()) {
            destSid = config.getIPv4SegmentId(payload.getDestinationMAC());
        } else {
            destSid = config.getIPv6SegmentId(payload.getDestinationMAC());
        }

        if (sid == -1 || destSid == sid ||
                config.inSameSubnet(outport.deviceId(), destIpAddress)) {
            TrafficTreatment treatment = DefaultTrafficTreatment.builder().
                    setOutput(outport.port()).build();
            OutboundPacket packet = new DefaultOutboundPacket(outport.deviceId(),
                                                              treatment, ByteBuffer.wrap(payload.serialize()));
            srManager.packetService.emit(packet);
        } else {
            log.debug("Send a MPLS packet as a ICMP response");
            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .setOutput(outport.port())
                    .build();

            payload.setEtherType(Ethernet.MPLS_UNICAST);
            MPLS mplsPkt = new MPLS();
            mplsPkt.setLabel(sid);
            mplsPkt.setTtl(allowedHops);
            mplsPkt.setPayload(payload.getPayload());
            payload.setPayload(mplsPkt);

            OutboundPacket packet = new DefaultOutboundPacket(outport.deviceId(),
                                                              treatment, ByteBuffer.wrap(payload.serialize()));

            srManager.packetService.emit(packet);
        }
    }

    //////////////////////////////////////
    //     ICMP Echo/Reply Protocol     //
    //////////////////////////////////////

    /**
     * Process incoming ICMP packet.
     * If it is an ICMP request to router or known host, then sends an ICMP response.
     * If it is an ICMP packet to known host and forward the packet to the host.
     * If it is an ICMP packet to unknown host in a subnet, then sends an ARP request
     * to the subnet.
     *
     * @param eth inbound ICMP packet
     * @param inPort the input port
     */
    public void processIcmp(Ethernet eth, ConnectPoint inPort) {
        DeviceId deviceId = inPort.deviceId();
        IPv4 ipv4Packet = (IPv4) eth.getPayload();
        Ip4Address destinationAddress = Ip4Address.valueOf(ipv4Packet.getDestinationAddress());
        Set<IpAddress> gatewayIpAddresses = config.getPortIPs(deviceId);
        IpAddress routerIp;
        try {
            routerIp = config.getRouterIpv4(deviceId);
        } catch (DeviceConfigNotFoundException e) {
            log.warn(e.getMessage() + " Aborting processPacketIn.");
            return;
        }
        // ICMP to the router IP or gateway IP
        if (((ICMP) ipv4Packet.getPayload()).getIcmpType() == ICMP.TYPE_ECHO_REQUEST &&
                (destinationAddress.equals(routerIp.getIp4Address()) ||
                        gatewayIpAddresses.contains(destinationAddress))) {
            sendIcmpResponse(eth, inPort);
            // We remove the packet from the queue
            srManager.ipHandler.dequeuePacket(ipv4Packet, destinationAddress);

        // ICMP for any known host
        } else if (!srManager.hostService.getHostsByIp(destinationAddress).isEmpty()) {
            // TODO: known host packet should not be coming to controller - resend flows?
            srManager.ipHandler.forwardPackets(deviceId, destinationAddress);

        // ICMP for an unknown host in the subnet of the router
        } else if (config.inSameSubnet(deviceId, destinationAddress)) {
            srManager.arpHandler.sendArpRequest(deviceId, destinationAddress, inPort);

        // ICMP for an unknown host
        } else {
            log.debug("ICMP request for unknown host {} ", destinationAddress);
            // We remove the packet from the queue
            srManager.ipHandler.dequeuePacket(ipv4Packet, destinationAddress);
        }
    }

    /**
     * Sends an ICMP reply message.
     *
     * Note: we assume that packets sending from the edge switches to the hosts
     * have untagged VLAN.
     * @param icmpRequest the original ICMP request
     * @param outport the output port where the ICMP reply should be sent to
     */
    private void sendIcmpResponse(Ethernet icmpRequest, ConnectPoint outport) {
        // Note: We assume that packets arrive at the edge switches have
        // untagged VLAN.
        Ethernet icmpReplyEth = ICMP.buildIcmpReply(icmpRequest);
        IPv4 icmpRequestIpv4 = (IPv4) icmpRequest.getPayload();
        IPv4 icmpReplyIpv4 = (IPv4) icmpReplyEth.getPayload();
        Ip4Address destIpAddress = Ip4Address.valueOf(icmpRequestIpv4.getSourceAddress());
        Ip4Address destRouterAddress = config.getRouterIpAddressForASubnetHost(destIpAddress);
        int destSid = config.getIPv4SegmentId(destRouterAddress);
        if (destSid < 0) {
            log.warn("Cannot find the Segment ID for {}", destIpAddress);
            return;
        }
        sendPacketOut(outport, icmpReplyEth, destSid, destIpAddress, icmpReplyIpv4.getTtl());
    }

    ///////////////////////////////////////////
    //      ICMPv6 Echo/Reply Protocol       //
    ///////////////////////////////////////////

    /**
     * Process incoming ICMPv6 packet.
     * If it is an ICMP request to router or known host, then sends an ICMP response.
     * If it is an ICMP packet to known host and forward the packet to the host.
     * If it is an ICMP packet to unknown host in a subnet, then sends an ARP request
     * to the subnet.
     *
     * @param eth the incoming ICMPv6 packet
     * @param inPort the input port
     */
    public void processIcmpv6(Ethernet eth, ConnectPoint inPort) {
        DeviceId deviceId = inPort.deviceId();
        IPv6 ipv6Packet = (IPv6) eth.getPayload();
        Ip6Address destinationAddress = Ip6Address.valueOf(ipv6Packet.getDestinationAddress());
        Set<IpAddress> gatewayIpAddresses = config.getPortIPs(deviceId);
        IpAddress routerIp;
        try {
            routerIp = config.getRouterIpv6(deviceId);
        } catch (DeviceConfigNotFoundException e) {
            log.warn(e.getMessage() + " Aborting processPacketIn.");
            return;
        }
        ICMP6 icmp6 = (ICMP6) ipv6Packet.getPayload();
        // ICMP to the router IP or gateway IP
        if (icmp6.getIcmpType()  == ICMP6.ECHO_REQUEST &&
                (destinationAddress.equals(routerIp.getIp6Address()) ||
                        gatewayIpAddresses.contains(destinationAddress))) {
            sendIcmpv6Response(eth, inPort);
            // We remove the packet from the queue
            srManager.ipHandler.dequeuePacket(ipv6Packet, destinationAddress);
            // ICMP for any known host
        } else if (!srManager.hostService.getHostsByIp(destinationAddress).isEmpty()) {
            // TODO: known host packet should not be coming to controller - resend flows?
            srManager.ipHandler.forwardPackets(deviceId, destinationAddress);
            // ICMP for an unknown host in the subnet of the router
        } else if (config.inSameSubnet(deviceId, destinationAddress)) {
            sendNdpRequest(deviceId, destinationAddress, inPort);
            // ICMP for an unknown host or not configured host
        } else {
            log.debug("ICMPv6 request for unknown host or not configured host {} ", destinationAddress);
            // We remove the packet from the queue
            srManager.ipHandler.dequeuePacket(ipv6Packet, destinationAddress);
        }
    }

    /**
     * Sends an ICMPv6 reply message.
     *
     * Note: we assume that packets sending from the edge switches to the hosts
     * have untagged VLAN.
     * @param ethRequest the original ICMP request
     * @param outport the output port where the ICMP reply should be sent to
     */
    private void sendIcmpv6Response(Ethernet ethRequest, ConnectPoint outport) {
        // Note: We assume that packets arrive at the edge switches have
        // untagged VLAN.
        Ethernet ethReply = ICMP6.buildIcmp6Reply(ethRequest);
        IPv6 icmpRequestIpv6 = (IPv6) ethRequest.getPayload();
        IPv6 icmpReplyIpv6 = (IPv6) ethRequest.getPayload();
        Ip6Address destIpAddress = Ip6Address.valueOf(icmpRequestIpv6.getSourceAddress());
        Ip6Address destRouterAddress = config.getRouterIpAddressForASubnetHost(destIpAddress);
        int sid = config.getIPv6SegmentId(destRouterAddress);
        if (sid < 0) {
            log.warn("Cannot find the Segment ID for {}", destIpAddress);
            return;
        }
        sendPacketOut(outport, ethReply, sid, destIpAddress, icmpReplyIpv6.getHopLimit());
    }

    ///////////////////////////////////////////
    //  ICMPv6 Neighbour Discovery Protocol  //
    ///////////////////////////////////////////

    /**
     * Process incoming NDP packet.
     *
     * If it is an NDP request for the router or for the gateway, then sends a NDP reply.
     * If it is an NDP request to unknown host flood in the subnet.
     * If it is an NDP packet to known host forward the packet to the host.
     *
     * FIXME If the NDP packets use link local addresses we fail.
     *
     * @param pkt inbound packet
     * @param hostService the host service
     */
    public void processPacketIn(NeighbourMessageContext pkt, HostService hostService) {
        /*
         * First we validate the ndp packet
         */
        SegmentRoutingAppConfig appConfig = srManager.cfgService
                .getConfig(srManager.appId, SegmentRoutingAppConfig.class);
        if (appConfig != null && appConfig.suppressSubnet().contains(pkt.inPort())) {
            // Ignore NDP packets come from suppressed ports
            pkt.drop();
            return;
        }
        if (!validateSrcIp(pkt)) {
            log.debug("Ignore NDP packet discovered on {} with unexpected src ip address {}.",
                      pkt.inPort(), pkt.sender());
            pkt.drop();
            return;
        }

        if (pkt.type() == NeighbourMessageType.REQUEST) {
            handleNdpRequest(pkt, hostService);
        } else {
            handleNdpReply(pkt, hostService);
        }

    }

    /**
     * Utility function to verify if the src ip belongs to the same
     * subnet configured on the port it is seen.
     *
     * @param pkt the ndp packet and context information
     * @return true if the src ip is a valid address for the subnet configured
     * for the connect point
     */
    private boolean validateSrcIp(NeighbourMessageContext pkt) {
        ConnectPoint connectPoint = pkt.inPort();
        IpPrefix subnet = config.getPortIPv6Subnet(
                connectPoint.deviceId(),
                connectPoint.port()
        );
        return subnet != null && subnet.contains(pkt.sender());
    }

    /**
     * Helper method to handle the ndp requests.
     *
     * @param pkt the ndp packet request and context information
     * @param hostService the host service
     */
    private void handleNdpRequest(NeighbourMessageContext pkt, HostService hostService) {
        /*
         * ND request for the gateway. We have to reply on behalf
         * of the gateway.
         */
        if (isNdpForGateway(pkt)) {
            log.debug("Sending NDP reply on behalf of the router");
            sendResponse(pkt, config.getRouterMacForAGatewayIp(pkt.target()), hostService);
        } else {
            /*
             * ND request for an host. We do a search by Ip.
             */
            Set<Host> hosts = hostService.getHostsByIp(pkt.target());
            /*
             * Possible misconfiguration ? In future this case
             * should be handled we can have same hosts in different
             * vlans.
             */
            if (hosts.size() > 1) {
                log.warn("More than one host with IP {}", pkt.target());
            }
            Host targetHost = hosts.stream().findFirst().orElse(null);
            /*
             * If we know the host forward to its attachment
             * point.
             */
            if (targetHost != null) {
                log.debug("Forward NDP request to the target host");
                pkt.forward(targetHost.location());
            } else {
                /*
                 * Flood otherwise.
                 */
                log.debug("Flood NDP request to the target subnet");
                flood(pkt);
            }
        }
    }

    /**
     * Helper method to handle the ndp replies.
     *
     * @param pkt the ndp packet reply and context information
     * @param hostService the host service
     */
    private void handleNdpReply(NeighbourMessageContext pkt, HostService hostService) {
        if (isNdpForGateway(pkt)) {
            log.debug("Forwarding all the ip packets we stored");
            Ip6Address hostIpAddress = pkt.sender().getIp6Address();
            srManager.ipHandler.forwardPackets(pkt.inPort().deviceId(), hostIpAddress);
        } else {
            HostId hostId = HostId.hostId(pkt.dstMac(), pkt.vlan());
            Host targetHost = hostService.getHost(hostId);
            if (targetHost != null) {
                log.debug("Forwarding the reply to the host");
                pkt.forward(targetHost.location());
            } else {
                /*
                 * We don't have to flood towards spine facing ports.
                 */
                if (pkt.vlan().equals(VlanId.vlanId(SegmentRoutingManager.ASSIGNED_VLAN_NO_SUBNET))) {
                    return;
                }
                log.debug("Flooding the reply to the subnet");
                flood(pkt);
            }
        }
    }

    /**
     * Utility to verify if the ND are for the gateway.
     *
     * @param pkt the ndp packet
     * @return true if the ndp is for the gateway. False otherwise
     */
    private boolean isNdpForGateway(NeighbourMessageContext pkt) {
        DeviceId deviceId = pkt.inPort().deviceId();
        Set<IpAddress> gatewayIpAddresses = null;
        try {
            if (pkt.target().equals(config.getRouterIpv6(deviceId))) {
                return true;
            }
            gatewayIpAddresses = config.getPortIPs(deviceId);
        } catch (DeviceConfigNotFoundException e) {
            log.warn(e.getMessage() + " Aborting check for router IP in processing ndp");
        }
        if (gatewayIpAddresses != null &&
                gatewayIpAddresses.contains(pkt.target())) {
            return true;
        }
        return false;
    }

    /**
     * Sends a NDP request for the target IP address to all ports except in-port.
     *
     * @param deviceId Switch device ID
     * @param targetAddress target IP address for ARP
     * @param inPort in-port
     */
    public void sendNdpRequest(DeviceId deviceId, IpAddress targetAddress, ConnectPoint inPort) {
        byte[] senderMacAddress = new byte[MacAddress.MAC_ADDRESS_LENGTH];
        byte[] senderIpAddress = new byte[Ip6Address.BYTE_LENGTH];
        /*
         * Retrieves device info.
         */
        if (!getSenderInfo(senderMacAddress, senderIpAddress, deviceId, targetAddress)) {
            log.warn("Aborting sendNdpRequest, we cannot get all the information needed");
            return;
        }
        /*
         * We have to compute the dst mac address and dst
         * ip address.
         */
        byte[] dstIp = IPv6.getSolicitNodeAddress(targetAddress.toOctets());
        byte[] dstMac = IPv6.getMCastMacAddress(dstIp);
        /*
         * Creates the request.
         */
        Ethernet ndpRequest = NeighborSolicitation.buildNdpSolicit(
                targetAddress.toOctets(),
                senderIpAddress,
                dstIp,
                senderMacAddress,
                dstMac,
                VlanId.NONE
        );
        flood(ndpRequest, inPort, targetAddress);
    }

    /////////////////////////////////////////////////////////////////
    //    XXX Neighbour hacking, temporary workaround will be      //
    //    removed as soon as possible, when the bridging will      //
    //    be implemented. For now, it's fine to leave this         //
    /////////////////////////////////////////////////////////////////

    // XXX Neighbour hacking, this method is used to handle
    // the ICMPv6 protocols for the upstream port
    public boolean handleUPstreamPackets(PacketContext packetContext) {
        InboundPacket pkt = packetContext.inPacket();
        Ethernet ethernet = pkt.parsed();
        if (srManager.vRouterCP == null || srManager.upstreamCP == null) {
            return false;
        }
        if (pkt.receivedFrom().equals(srManager.upstreamCP)) {
            sendTo(ByteBuffer.wrap(ethernet.serialize()), srManager.vRouterCP);
            return true;
        }
        return false;
    }

    // XXX Neigbour hack. To send out a packet
    private void sendTo(ByteBuffer packet, ConnectPoint outPort) {
        TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder();
        builder.setOutput(outPort.port());
        srManager.packetService.emit(new DefaultOutboundPacket(outPort.deviceId(),
                                                     builder.build(), packet));
    }

}
