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
package org.onosproject.roadm;

import com.google.common.collect.Range;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ChannelSpacing;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Direction;
import org.onosproject.net.OchSignal;
import org.onosproject.net.OchSignalType;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.behaviour.LambdaQuery;
import org.onosproject.net.behaviour.PowerConfig;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowId;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.flow.instructions.Instructions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Application for monitoring and configuring ROADM devices.
 */
@Component(immediate = true)
@Service
public class RoadmManager implements RoadmService {

    private static final String APP_NAME = "org.onosproject.roadm";
    private ApplicationId appId;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private DeviceListener deviceListener = new InternalDeviceListener();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected RoadmStore roadmStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(APP_NAME);
        deviceService.addListener(deviceListener);
        initDevices();

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        deviceService.removeListener(deviceListener);

        log.info("Stopped");
    }

    private PowerConfig<Object> getPowerConfig(DeviceId deviceId) {
        Device device = deviceService.getDevice(deviceId);
        if (device != null && device.is(PowerConfig.class)) {
            return device.as(PowerConfig.class);
        }
        log.warn("Unable to load PowerConfig for {}", deviceId);
        return null;
    }

    private LambdaQuery getLambdaQuery(DeviceId deviceId) {
        Device device = deviceService.getDevice(deviceId);
        if (device != null && device.is(LambdaQuery.class)) {
            return device.as(LambdaQuery.class);
        }
        return null;
    }

    private void initDevices() {
        for (Device device : deviceService.getDevices(Device.Type.ROADM)) {
            initDevice(device.id());
            setAllInitialTargetPortPowers(device.id());
        }
    }

    // Initialize RoadmStore for a device to support target power
    private void initDevice(DeviceId deviceId) {
        if (!roadmStore.deviceAvailable(deviceId)) {
            roadmStore.addDevice(deviceId);
        }
        log.info("Initialized device {}", deviceId);
    }

    // Sets the target port powers for a port on a device
    // Attempts to read target powers from store. If no value is found then
    // default value is used instead.
    private void setInitialTargetPortPower(DeviceId deviceId, PortNumber portNumber) {
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig == null) {
            log.warn("Unable to set default initial powers for port {} on device {}",
                     portNumber, deviceId);
            return;
        }

        Optional<Range<Long>> range =
                powerConfig.getTargetPowerRange(portNumber, Direction.ALL);
        if (!range.isPresent()) {
            log.warn("No target power range found for port {} on device {}",
                     portNumber, deviceId);
            return;
        }

        Long power = roadmStore.getTargetPower(deviceId, portNumber);
        if (power == null) {
            // Set default to middle of the range
            power = (range.get().lowerEndpoint() + range.get().upperEndpoint()) / 2;
            roadmStore.setTargetPower(deviceId, portNumber, power);
        }
        powerConfig.setTargetPower(portNumber, Direction.ALL, power);
    }

    // Sets the target port powers for each each port on a device
    // Attempts to read target powers from store. If no value is found then
    // default value is used instead
    private void setAllInitialTargetPortPowers(DeviceId deviceId) {
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig == null) {
            log.warn("Unable to set default initial powers for device {}",
                     deviceId);
            return;
        }

        List<Port> ports = deviceService.getPorts(deviceId);
        for (Port port : ports) {
            Optional<Range<Long>> range =
                    powerConfig.getTargetPowerRange(port.number(), Direction.ALL);
            if (range.isPresent()) {
                Long power = roadmStore.getTargetPower(deviceId, port.number());
                if (power == null) {
                    // Set default to middle of the range
                    power = (range.get().lowerEndpoint() + range.get().upperEndpoint()) / 2;
                    roadmStore.setTargetPower(deviceId, port.number(), power);
                }
                powerConfig.setTargetPower(port.number(), Direction.ALL, power);
            } else {
                log.warn("No target power range found for port {} on device {}",
                         port.number(), deviceId);
            }
        }
    }

    @Override
    public void setTargetPortPower(DeviceId deviceId, PortNumber portNumber, long power) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            roadmStore.setTargetPower(deviceId, portNumber, power);
            powerConfig.setTargetPower(portNumber, Direction.ALL, power);
        } else {
            log.warn("Unable to set target port power for device {}", deviceId);
        }
    }

    @Override
    public Long getTargetPortPower(DeviceId deviceId, PortNumber portNumber) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        // getTargetPortPower is not yet implemented in PowerConfig so we
        // access store instead
        return roadmStore.getTargetPower(deviceId, portNumber);
    }

    @Override
    public void setAttenuation(DeviceId deviceId, PortNumber portNumber,
                               OchSignal ochSignal, long attenuation) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        checkNotNull(ochSignal);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            powerConfig.setTargetPower(portNumber, ochSignal, attenuation);
        } else {
            log.warn("Cannot set attenuation for channel index {} on device {}",
                     ochSignal.spacingMultiplier(), deviceId);
        }
    }

    @Override
    public Long getAttenuation(DeviceId deviceId, PortNumber portNumber,
                               OchSignal ochSignal) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        checkNotNull(ochSignal);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            Optional<Long> attenuation =
                    powerConfig.getTargetPower(portNumber, ochSignal);
            if (attenuation.isPresent()) {
                return attenuation.get();
            }
        }
        return null;
    }

    @Override
    public Long getCurrentPortPower(DeviceId deviceId, PortNumber portNumber) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            Optional<Long> currentPower =
                    powerConfig.currentPower(portNumber, Direction.ALL);
            if (currentPower.isPresent()) {
                return currentPower.get();
            }
        }
        return null;
    }

    @Override
    public Long getCurrentChannelPower(DeviceId deviceId, PortNumber portNumber,
                                       OchSignal ochSignal) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        checkNotNull(ochSignal);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            Optional<Long> currentPower =
                    powerConfig.currentPower(portNumber, ochSignal);
            if (currentPower.isPresent()) {
                return currentPower.get();
            }
        }
        return null;
    }

    @Override
    public Set<OchSignal> queryLambdas(DeviceId deviceId, PortNumber portNumber) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        LambdaQuery lambdaQuery = getLambdaQuery(deviceId);
        if (lambdaQuery != null) {
            return lambdaQuery.queryLambdas(portNumber);
        }
        return Collections.emptySet();
    }

    @Override
    public FlowId createConnection(DeviceId deviceId, int priority, boolean isPermanent,
                                 int timeout, PortNumber inPort, PortNumber outPort,
                                 OchSignal ochSignal) {
        checkNotNull(deviceId);
        checkNotNull(inPort);
        checkNotNull(outPort);

        FlowRule.Builder flowBuilder = new DefaultFlowRule.Builder();
        flowBuilder.fromApp(appId);
        flowBuilder.withPriority(priority);
        if (isPermanent) {
            flowBuilder.makePermanent();
        } else {
            flowBuilder.makeTemporary(timeout);
        }
        flowBuilder.forDevice(deviceId);

        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        selectorBuilder.add(Criteria.matchInPort(inPort));
        selectorBuilder.add(Criteria.matchOchSignalType(OchSignalType.FIXED_GRID));
        selectorBuilder.add(Criteria.matchLambda(ochSignal));
        flowBuilder.withSelector(selectorBuilder.build());

        TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder();
        treatmentBuilder.add(Instructions.createOutput(outPort));
        flowBuilder.withTreatment(treatmentBuilder.build());

        FlowRule flowRule = flowBuilder.build();
        flowRuleService.applyFlowRules(flowRule);

        log.info("Created connection from input port {} to output port {}",
                 inPort.toLong(), outPort.toLong());

        return flowRule.id();
    }

    @Override
    public FlowId createConnection(DeviceId deviceId, int priority, boolean isPermanent,
                                 int timeout, PortNumber inPort, PortNumber outPort,
                                 OchSignal ochSignal, long attenuation) {
        checkNotNull(deviceId);
        checkNotNull(inPort);
        checkNotNull(outPort);
        FlowId flowId = createConnection(deviceId, priority, isPermanent,
                                         timeout, inPort, outPort, ochSignal);
        delayedSetAttenuation(deviceId, outPort, ochSignal, attenuation);
        return flowId;
    }

    // Delay the call to setTargetPower because the flow may not be in the store yet
    private void delayedSetAttenuation(DeviceId deviceId, PortNumber outPort,
                                       OchSignal ochSignal, long attenuation) {
        Runnable setAtt = () -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.warn("Thread interrupted. Setting attenuation early.");
            }
            setAttenuation(deviceId, outPort, ochSignal, attenuation);
        };
        new Thread(setAtt).start();
    }

    @Override
    public void removeConnection(DeviceId deviceId, FlowId flowId) {
        checkNotNull(deviceId);
        checkNotNull(flowId);
        for (FlowEntry entry : flowRuleService.getFlowEntries(deviceId)) {
            if (entry.id().equals(flowId)) {
                flowRuleService.removeFlowRules(entry);
                log.info("Deleted connection {}", entry.id());
                break;
            }
        }
    }

    @Override
    public boolean hasPortTargetPower(DeviceId deviceId, PortNumber portNumber) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            Optional<Range<Long>> range =
                    powerConfig.getTargetPowerRange(portNumber, Direction.ALL);
            return range.isPresent();
        }
        return false;
    }

    @Override
    public boolean portTargetPowerInRange(DeviceId deviceId, PortNumber portNumber,
                                          long power) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            Optional<Range<Long>> range =
                    powerConfig.getTargetPowerRange(portNumber, Direction.ALL);
            return range.isPresent() && range.get().contains(power);
        }
        return false;
    }

    @Override
    public boolean attenuationInRange(DeviceId deviceId, PortNumber outPort,
                                      long att) {
        checkNotNull(deviceId);
        checkNotNull(outPort);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            OchSignal stubOch = OchSignal.newDwdmSlot(ChannelSpacing.CHL_50GHZ, 0);
            Optional<Range<Long>> range =
                    powerConfig.getTargetPowerRange(outPort, stubOch);
            return range.isPresent() && range.get().contains(att);
        }
        return false;
    }

    @Override
    public boolean validInputPort(DeviceId deviceId, PortNumber portNumber) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            Optional<Range<Long>> range =
                    powerConfig.getInputPowerRange(portNumber, Direction.ALL);
            return range.isPresent();
        }
        return false;
    }

    @Override
    public boolean validOutputPort(DeviceId deviceId, PortNumber portNumber) {
        return hasPortTargetPower(deviceId, portNumber);
    }

    @Override
    public boolean validChannel(DeviceId deviceId, PortNumber portNumber,
                                OchSignal ochSignal) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        LambdaQuery lambdaQuery = getLambdaQuery(deviceId);
        if (lambdaQuery != null) {
            Set<OchSignal> channels = lambdaQuery.queryLambdas(portNumber);
            return channels.contains(ochSignal);
        }
        return false;
    }

    @Override
    public boolean channelAvailable(DeviceId deviceId, OchSignal ochSignal) {
        checkNotNull(deviceId);
        checkNotNull(ochSignal);
        for (FlowEntry entry : flowRuleService.getFlowEntries(deviceId)) {
            if (ChannelData.fromFlow(entry).ochSignal().equals(ochSignal)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validConnection(DeviceId deviceId, PortNumber inPort,
                                   PortNumber outPort) {
        checkNotNull(deviceId);
        checkNotNull(inPort);
        checkNotNull(outPort);
        return validInputPort(deviceId, inPort) && validOutputPort(deviceId, outPort);
    }

    @Override
    public Range<Long> targetPortPowerRange(DeviceId deviceId, PortNumber portNumber) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            Optional<Range<Long>> range =
                    powerConfig.getTargetPowerRange(portNumber, Direction.ALL);
            if (range.isPresent()) {
                return range.get();
            }
        }
        return null;
    }

    @Override
    public Range<Long> attenuationRange(DeviceId deviceId, PortNumber portNumber,
                                        OchSignal ochSignal) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        checkNotNull(ochSignal);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            Optional<Range<Long>> range =
                    powerConfig.getTargetPowerRange(portNumber, ochSignal);
            if (range.isPresent()) {
                return range.get();
            }
        }
        return null;
    }

    @Override
    public Range<Long> inputPortPowerRange(DeviceId deviceId, PortNumber portNumber) {
        checkNotNull(deviceId);
        checkNotNull(portNumber);
        PowerConfig<Object> powerConfig = getPowerConfig(deviceId);
        if (powerConfig != null) {
            Optional<Range<Long>> range =
                    powerConfig.getInputPowerRange(portNumber, Direction.ALL);
            if (range.isPresent()) {
                return range.get();
            }
        }
        return null;
    }

    // Listens to device events.
    private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent deviceEvent) {
            Device device = deviceEvent.subject();

            switch (deviceEvent.type()) {
                case DEVICE_ADDED:
                case DEVICE_UPDATED:
                    initDevice(device.id());
                    break;
                case PORT_ADDED:
                case PORT_UPDATED:
                    setInitialTargetPortPower(device.id(), deviceEvent.port().number());
                    break;
                default:
                    break;

            }
        }
    }
}
