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
package org.onosproject.provider.tl1.device.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.ChassisId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.incubator.net.config.basics.ConfigException;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.PortNumber;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DeviceAdminService;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceDescriptionDiscovery;
import org.onosproject.net.device.DeviceProvider;
import org.onosproject.net.device.DeviceProviderRegistry;
import org.onosproject.net.device.DeviceProviderService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.tl1.Tl1Controller;
import org.onosproject.tl1.Tl1Device;
import org.onosproject.tl1.Tl1Listener;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import static org.onosproject.net.config.basics.SubjectFactories.APP_SUBJECT_FACTORY;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Device provider for TL1 devices.
 *
 * Sits between ONOS provider service and the TL1 controller.
 * Relies on network config subsystem to know about devices.
 */
@Component(immediate = true)
public class Tl1DeviceProvider extends AbstractProvider implements DeviceProvider {
    private static final String APP_NAME = "org.onosproject.tl1";
    private static final String TL1 = "tl1";
    private static final String PROVIDER = "org.onosproject.provider.tl1.device";
    private static final String UNKNOWN = "unknown";
    private static final int REACHABILITY_TIMEOUT = 2000;      // in milliseconds

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigRegistry cfgRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceAdminService deviceAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected Tl1Controller controller;

    private ApplicationId appId;
    private NetworkConfigListener cfgListener = new InnerConfigListener();
    private Tl1Listener tl1Listener = new InnerTl1Listener();
    private DeviceProviderService providerService;

    private final ConfigFactory cfgFactory =
            new ConfigFactory<ApplicationId, Tl1ProviderConfig>(APP_SUBJECT_FACTORY,
                    Tl1ProviderConfig.class,
                    "devices",
                    true) {
                @Override
                public Tl1ProviderConfig createConfig() {
                    return new Tl1ProviderConfig();
                }
            };

    @Activate
    public void activate() {
        appId = coreService.registerApplication(APP_NAME);
        providerService = providerRegistry.register(this);
        cfgRegistry.addListener(cfgListener);
        controller.addListener(tl1Listener);
        cfgRegistry.registerConfigFactory(cfgFactory);
        registerDevices();
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        controller.removeListener(tl1Listener);
        cfgRegistry.removeListener(cfgListener);
        controller.getDeviceIds().forEach(deviceId -> {
            controller.removeDevice(deviceId);
            deviceAdminService.removeDevice(deviceId);
        });
        providerRegistry.unregister(this);
        cfgRegistry.unregisterConfigFactory(cfgFactory);
        providerService = null;
        log.info("Stopped");
    }

    public Tl1DeviceProvider() {
        super(new ProviderId(TL1, PROVIDER));
    }

    public void triggerProbe(DeviceId deviceId) {
        // TODO
    }

    public void roleChanged(DeviceId deviceId, MastershipRole newRole) {
        switch (newRole) {
            case MASTER:
                controller.connectDevice(deviceId);
                providerService.receivedRoleReply(deviceId, newRole, MastershipRole.MASTER);
                log.debug("Accepting mastership role change to {} for device {}", newRole, deviceId);
                break;
            case STANDBY:
                controller.disconnectDevice(deviceId);
                providerService.receivedRoleReply(deviceId, newRole, MastershipRole.STANDBY);
                break;
            case NONE:
                controller.disconnectDevice(deviceId);
                providerService.receivedRoleReply(deviceId, newRole, MastershipRole.NONE);
                break;
            default:
                log.error("Invalid mastership state: {}", newRole);
        }
    }

    // Assumes device is registered in TL1 controller.
    public boolean isReachable(DeviceId deviceId) {
        try {
            // First check if device is already connected.
            // If not, try to open a socket.
            Tl1Device device = controller.getDevice(deviceId).get();
            if (device.isConnected()) {
                return true;
            }

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(device.ip().toInetAddress(), device.port()), REACHABILITY_TIMEOUT);
            socket.close();
            return true;
        } catch (NoSuchElementException | IOException | IllegalArgumentException e) {
            log.error("Cannot reach device {}", deviceId, e);
            return false;
        }
    }

    public void changePortState(DeviceId deviceId, PortNumber portNumber, boolean enable) {
        // TODO
    }

    // Register all devices in the core and in the TL1 controller
    void registerDevices() {
        Tl1ProviderConfig cfg = cfgRegistry.getConfig(appId, Tl1ProviderConfig.class);

        if (cfg == null) {
            return;
        }

        try {
            cfg.readDevices().forEach(device -> {
                try {
                    // Add device to TL1 controller
                    DeviceId deviceId = DeviceId.deviceId(
                            new URI(TL1, device.ip() + ":" + device.port(), null));

                    if (controller.addDevice(deviceId, device)) {
                        SparseAnnotations ann = DefaultAnnotations.builder()
                                .set(AnnotationKeys.PROTOCOL, TL1.toUpperCase())
                                .build();
                        // Register device in the core with default parameters and mark it as unavailable
                        DeviceDescription dd = new DefaultDeviceDescription(deviceId.uri(), Device.Type.SWITCH, UNKNOWN,
                                UNKNOWN, UNKNOWN, UNKNOWN, new ChassisId(), false, ann);
                        providerService.deviceConnected(deviceId, dd);
                    }
                } catch (URISyntaxException e) {
                    log.error("Skipping device {}", device, e);
                }
            });
        } catch (ConfigException e) {
            log.error("Cannot parse network configuration", e);
        }
    }

    /**
     * Tries to update the device and port descriptions through the {@code DeviceDescriptionDiscovery} behaviour.
     *
     * @param deviceId the device
     */
    void updateDevice(DeviceId deviceId) {
        Device device = deviceService.getDevice(deviceId);

        if (!device.is(DeviceDescriptionDiscovery.class)) {
            return;
        }

        try {
            // Update device description
            DeviceDescriptionDiscovery discovery = device.as(DeviceDescriptionDiscovery.class);
            DeviceDescription dd = discovery.discoverDeviceDetails();
            if (dd == null) {
                return;
            }
            providerService.deviceConnected(deviceId,
                    new DefaultDeviceDescription(dd, true, dd.annotations()));
            // Update ports
            providerService.updatePorts(deviceId, discovery.discoverPortDetails());
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("Cannot update device description {}", deviceId, e);
        }
    }

    /**
     * Listener for network configuration events.
     */
    private class InnerConfigListener implements NetworkConfigListener {
        public void event(NetworkConfigEvent event) {
            if (event.type() == NetworkConfigEvent.Type.CONFIG_ADDED) {
                registerDevices();
            } else if (event.type() == NetworkConfigEvent.Type.CONFIG_UPDATED) {
                // TODO: calculate delta
                registerDevices();
            } else if (event.type() == NetworkConfigEvent.Type.CONFIG_REMOVED) {
                controller.getDeviceIds().forEach(deviceId -> {
                    controller.removeDevice(deviceId);
                    deviceAdminService.removeDevice(deviceId);
                });
            }
        }

        public boolean isRelevant(NetworkConfigEvent event) {
            return event.configClass().equals(Tl1ProviderConfig.class) &&
                    (event.type() == NetworkConfigEvent.Type.CONFIG_ADDED ||
                            event.type() == NetworkConfigEvent.Type.CONFIG_UPDATED ||
                            event.type() == NetworkConfigEvent.Type.CONFIG_REMOVED);
        }
    }

    /**
     * Listener for TL1 events.
     */
    private class InnerTl1Listener implements Tl1Listener {
        @Override
        public void deviceConnected(DeviceId deviceId) {
            updateDevice(deviceId);
        }

        @Override
        public void deviceDisconnected(DeviceId deviceId) {
            providerService.deviceDisconnected(deviceId);
        }
    }
}
