/*
 * Copyright 2016-present Open Networking Laboratory
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

package org.onosproject.incubator.net.virtual.impl.provider;

import com.google.common.collect.Maps;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.incubator.net.virtual.NetworkId;
import org.onosproject.incubator.net.virtual.TenantId;
import org.onosproject.incubator.net.virtual.VirtualDevice;
import org.onosproject.incubator.net.virtual.VirtualNetwork;
import org.onosproject.incubator.net.virtual.VirtualNetworkAdminService;
import org.onosproject.incubator.net.virtual.VirtualPort;
import org.onosproject.incubator.net.virtual.provider.AbstractVirtualProvider;
import org.onosproject.incubator.net.virtual.provider.VirtualPacketProvider;
import org.onosproject.incubator.net.virtual.provider.VirtualPacketProviderService;
import org.onosproject.incubator.net.virtual.provider.VirtualProviderRegistryService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.packet.DefaultInboundPacket;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.provider.ProviderId;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
public class DefaultVirtualPacketProvider extends AbstractVirtualProvider
        implements VirtualPacketProvider {

    private static final int PACKET_PROCESSOR_PRIORITY = 1;

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VirtualNetworkAdminService virtualNetworkAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VirtualProviderRegistryService providerRegistryService;

    ApplicationId appId;
    InternalPacketProcessor processor;

    Map<VirtualPacketContext, PacketContext> contextMap;

    /**
     * Creates a provider with the supplied identifier.
     *
     */
    public DefaultVirtualPacketProvider() {
        super(new ProviderId("virtual-packet",
                             "org.onosproject.virtual.virtual-packet"));
    }

    @Activate
    public void activate() {
        appId = coreService.registerApplication(
                "org.onosproject.virtual.virtual-packet");
        providerRegistryService.registerProvider(this);

        contextMap = Maps.newConcurrentMap();

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        if (processor != null) {
            packetService.removeProcessor(processor);
        }
    }

    @Modified
    protected void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();
    }


    @Override
    public void emit(NetworkId networkId, OutboundPacket packet) {
       packetService.emit(devirtualize(networkId, packet));
    }

    @Override
    public void startPacketHandling() {
        processor = new InternalPacketProcessor();
        packetService.addProcessor(processor, PACKET_PROCESSOR_PRIORITY);
    }

    /**
     * Translate the requested physical PacketContext into a virtual PacketContext.
     * See {@link org.onosproject.net.packet.OutboundPacket}
     *
     * @param context A physical PacketContext be translated
     * @return A translated virtual PacketContext
     */
    private Set<VirtualPacketContext> virtualize(PacketContext context) {
        Set<VirtualPacketContext> outContext = new HashSet<>();

        Set<TenantId> tIds = virtualNetworkAdminService.getTenantIds();

        Set<VirtualNetwork> vNetworks = new HashSet<>();
        tIds.stream()
                .map(tid -> virtualNetworkAdminService
                        .getVirtualNetworks(tid))
                .forEach(vNetworks::addAll);

        Set<VirtualDevice> vDevices = new HashSet<>();
        vNetworks.stream()
                .map(network -> virtualNetworkAdminService
                        .getVirtualDevices(network.id()))
                .forEach(vDevices::addAll);

        Set<VirtualPort> vPorts = new HashSet<>();
        vDevices.stream()
                .map(dev -> virtualNetworkAdminService
                        .getVirtualPorts(dev.networkId(), dev.id()))
                .forEach(vPorts::addAll);

        ConnectPoint inCp = context.inPacket().receivedFrom();

        Set<VirtualPort> inVports = vPorts.stream()
                .filter(vp -> vp.realizedBy().equals(inCp))
                .collect(Collectors.toSet());

        for (VirtualPort vPort : inVports) {
            ConnectPoint cp = new ConnectPoint(vPort.element().id(),
                                               vPort.number());

            Ethernet eth = context.inPacket().parsed();
            eth.setVlanID(Ethernet.VLAN_UNTAGGED);

            InboundPacket inPacket =
                    new DefaultInboundPacket(cp, eth,
                                             ByteBuffer.wrap(eth.serialize()));

            VirtualPacketContext vContext =
                    new VirtualPacketContext(context.time(), inPacket, null,
                                             false, vPort.networkId(),
                                             this);

            contextMap.put(vContext, context);
            outContext.add(vContext);
        }

        return outContext;
    }

    /**
     * Translate the requested a virtual outbound packet into
     * a physical OutboundPacket.
     * See {@link org.onosproject.net.packet.PacketContext}
     *
     * @param packet A OutboundPacket to be translated
     * @return de-virtualized (physical) OutboundPacket
     */
    private OutboundPacket devirtualize(NetworkId networkId, OutboundPacket packet) {
        Set<VirtualPort> vPorts = virtualNetworkAdminService
                .getVirtualPorts(networkId, packet.sendThrough());

        PortNumber vOutPortNum = packet.treatment().allInstructions().stream()
                .filter(i -> i.type() == Instruction.Type.OUTPUT)
                .map(i -> ((Instructions.OutputInstruction) i).port())
                .findFirst().get();

        Optional<ConnectPoint> optionalCpOut = vPorts.stream()
                .filter(v -> v.number().equals(vOutPortNum))
                .map(v -> v.realizedBy())
                .findFirst();
        if (!optionalCpOut.isPresent()) {
            log.info("Port {} is not realized yet, in Network {}, Device {}",
                     vOutPortNum, networkId, packet.sendThrough());
            return null;
        }
        ConnectPoint egressPoint = optionalCpOut.get();

        TrafficTreatment.Builder commonTreatmentBuilder
                = DefaultTrafficTreatment.builder();
        packet.treatment().allInstructions().stream()
                .filter(i -> i.type() != Instruction.Type.OUTPUT)
                .forEach(i -> commonTreatmentBuilder.add(i));
        TrafficTreatment commonTreatment = commonTreatmentBuilder.build();

        TrafficTreatment treatment = DefaultTrafficTreatment
                .builder(commonTreatment)
                .setOutput(egressPoint.port()).build();

        OutboundPacket outboundPacket = new DefaultOutboundPacket(
                egressPoint.deviceId(), treatment, packet.data());
        return outboundPacket;
    }

    /**
     * Translate the requested a virtual Packet Context into
     * a physical Packet Context.
     * This method is designed to support Context's send() method that invoked
     * by applications.
     * See {@link org.onosproject.net.packet.PacketContext}
     *
     * @param context A handled packet context
     * @return de-virtualized (physical) PacketContext
     */
    public PacketContext devirtualizeContext(VirtualPacketContext context) {
        NetworkId networkId = context.getNetworkId();

        OutboundPacket op = devirtualize(networkId, context.outPacket());

        PacketContext packetContext = contextMap.get(context);

        TrafficTreatment.Builder treatmentBuilder
                = packetContext.treatmentBuilder();
        if (op.treatment() != null) {
            op.treatment().allInstructions().forEach(treatmentBuilder::add);
        }

        return packetContext;
    }

    private final class InternalPacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext context) {
            Set<VirtualPacketContext> vContexts = virtualize(context);

            vContexts.forEach(vpc -> {
                                  VirtualPacketProviderService service =
                                          (VirtualPacketProviderService) providerRegistryService
                                                  .getProviderService(vpc.getNetworkId(),
                                                                      VirtualPacketProvider.class);
                                  service.processPacket(vpc);
                              });
        }
    }
}
