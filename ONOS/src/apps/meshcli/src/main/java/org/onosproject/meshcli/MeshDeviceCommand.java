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
package org.onosproject.meshcli;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.ChassisId;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceProvider;
import org.onosproject.net.device.DeviceProviderRegistry;
import org.onosproject.net.device.DeviceProviderService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.onlab.util.Tools.toHex;
import static org.onosproject.net.DeviceId.deviceId;


/**
 * Sample Apache Karaf CLI command
 */
@Command(scope = "onos", name = "mesh-device",
        description = "CLI commands to add/remove mesh devices via device provider interface")
public class MeshDeviceCommand extends AbstractShellCommand {
    @Argument(required = true, name = "commandParam", description = "The command to execute (add|remove)")
    String commandParam = null;
    @Argument(index = 1,  required = true, name = "deviceIdParam", description = "The device ID")
    String deviceIdParam = null;
    @Argument(index = 2, name = "numPortsParam", description = "Number of ports to create")
    String numPortsParam = null;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceProviderRegistry providerRegistry;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String MESH_APP_ID = "org.onosproject.mesh";
    private static final String ADD = "add";
    private static final String REMOVE = "remove";
    private static final String SCHEME = "mesh";

    private ApplicationId appId;
    private static ProviderId providerId = null;
    private static DeviceProviderService providerService = null;
    private static MeshDeviceProvider meshDeviceProvider = null;

    @Override
    protected void execute() {
        // Register provider & obtain provider service instance
        if (providerService == null) {
            print("register mesh device provider");
            meshDeviceProvider = new MeshDeviceProvider();
            providerRegistry = get(DeviceProviderRegistry.class);
            providerService = providerRegistry.register(meshDeviceProvider);
        }

        // Retrieve device service instance
        deviceService = get(DeviceService.class);

        // Process command
        if (commandParam.equals(ADD)) {
            print("Add device: " + deviceIdParam);
            Long id = Long.parseLong(deviceIdParam);
            DeviceId did;
            try {
                did = deviceId(new URI(SCHEME, toHex(id), null));
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }
            DeviceDescription description =
                    new DefaultDeviceDescription(
                            did.uri(), Device.Type.SWITCH,
                            "manufacturer",
                            "hardwareDescription",
                            "softwareDescription",
                            "serialNumber",
                            new ChassisId(id), DefaultAnnotations.EMPTY);
            providerService.deviceConnected(did, description);

            List<PortDescription> pdList = new ArrayList<>();
            for (int i = 0; i < Integer.decode(numPortsParam); i++) {
                PortNumber portNum = PortNumber.portNumber(i);
                DefaultAnnotations.Builder builder = DefaultAnnotations.builder();
                builder.set(AnnotationKeys.PORT_NAME, "PORT" + i);
                builder.set(AnnotationKeys.PORT_MAC, "MAC" + i);
                SparseAnnotations annotations = builder.build();
                PortDescription pd = new DefaultPortDescription(portNum,
                                                                true,
                                                                Port.Type.PACKET,
                                                                1_000,
                                                                annotations);
                pdList.add(pd);
            }
            providerService.updatePorts(did, pdList);
        }
        else if (commandParam.equals(REMOVE)) {
            print("Remove device: " + deviceIdParam);

            // Retrieve URI
            Long id = Long.parseLong(deviceIdParam);
            URI uri;
            try {
                uri = new URI(SCHEME, toHex(id), null);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }

            // Search for device and remove it
            for (Device device : deviceService.getDevices()) {
                if (device.id().uri().compareTo(uri) == 0) {
                    print("Device found and being removed");
                    providerService.deviceDisconnected(device.id());
                }
            }
        }
        else {
            print("Unsupported command: " + commandParam);
        }
    }


    private class MeshDeviceProvider extends AbstractProvider implements DeviceProvider {
        /**
         * Creates an OpenFlow device provider.
         */
        public MeshDeviceProvider() {
            super(new ProviderId(SCHEME, "org.onosproject.mesh.device"));
        }

        @Override
        public boolean isReachable(DeviceId deviceId) {
            return true;
        }

        @Override
        public void triggerProbe(DeviceId deviceId) {
        }

        @Override
        public void roleChanged(DeviceId deviceId, MastershipRole newRole) {
        }

        @Override
        public void changePortState(DeviceId deviceId, PortNumber portNumber,
                                    boolean enable) {
        }
    }

}
