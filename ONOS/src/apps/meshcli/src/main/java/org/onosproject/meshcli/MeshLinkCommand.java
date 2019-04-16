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
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.PortNumber;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.link.LinkProvider;
import org.onosproject.net.link.LinkProviderRegistry;
import org.onosproject.net.link.LinkProviderService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static org.onlab.util.Tools.toHex;
import static org.onosproject.net.DeviceId.deviceId;


/**
 * Sample Apache Karaf CLI command
 */
@Command(scope = "onos", name = "mesh-link",
        description = "CLI commands to add/remove mesh links via link provider interface")
public class MeshLinkCommand extends AbstractShellCommand {
    @Argument(required = true, name = "commandParam", description = "The command to execute (add|remove)")
    String commandParam = null;
    @Argument(index = 1,  required = true, name = "srcIdParam", description = "The source device ID")
    String srcIdParam = null;
    @Argument(index = 2,  required = true, name = "srcPortParam", description = "The source device port")
    String srcPortParam = null;
    @Argument(index = 3,  required = true, name = "dstIdParam", description = "The destination device ID")
    String dstIdParam = null;
    @Argument(index = 4,  required = true, name = "dstPortParam", description = "The destination device port")
    String dstPortParam = null;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkProviderRegistry providerRegistry;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String MESH_APP_ID = "org.onosproject.mesh";
    private static final String ADD = "add";
    private static final String REMOVE = "remove";
    private static final String SCHEME = "mesh";

    private ApplicationId appId;
    private static ProviderId providerId = null;
    private static LinkProviderService providerService = null;
    private static MeshLinkProvider meshLinkProvider = null;

    @Override
    protected void execute() {
        // Register provider & obtain provider service instance
        if (providerService == null) {
            print("register mesh link provider");
            meshLinkProvider = new MeshLinkProvider();
            providerRegistry = get(LinkProviderRegistry.class);
            providerService = providerRegistry.register(meshLinkProvider);
        }

        // Retrieve link service instance
        linkService = get(LinkService.class);

        // Process command
        DeviceId srcDeviceId;
        DeviceId dstDeviceId;
        Long srcId = Long.parseLong(srcIdParam);
        Long dstId = Long.parseLong(dstIdParam);
        try {
            srcDeviceId = deviceId(new URI(SCHEME, toHex(srcId), null));
            dstDeviceId = deviceId(new URI(SCHEME, toHex(dstId), null));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        ConnectPoint src = new ConnectPoint(srcDeviceId, PortNumber.portNumber(srcPortParam));
        ConnectPoint dst = new ConnectPoint(dstDeviceId, PortNumber.portNumber(dstPortParam));
        LinkDescription ld = new DefaultLinkDescription(src, dst, Link.Type.DIRECT);
        LinkDescription ldReverse = new DefaultLinkDescription(dst, src, Link.Type.DIRECT);

        if (commandParam.equals(ADD)) {
            print("Add link: srcId[%s] srcPort[%s] dstId[%s] dstPort[%s]",
                  srcIdParam, srcPortParam, dstIdParam, dstPortParam);
            providerService.linkDetected(ld);
            providerService.linkDetected(ldReverse);
        }
        else if (commandParam.equals(REMOVE)) {
            print("Remove link: srcId[%s] srcPort[%s] dstId[%s] dstPort[%s]",
                  srcIdParam, srcPortParam, dstIdParam, dstPortParam);
            providerService.linkVanished(ld);
            providerService.linkVanished(ldReverse);
        }
        else {
            print("Unsupported command: " + commandParam);
        }
    }


    private class MeshLinkProvider extends AbstractProvider implements LinkProvider {
        /**
         * Creates an OpenFlow device provider.
         */
        public MeshLinkProvider() {
            super(new ProviderId(SCHEME, "org.onosproject.mesh.link"));
        }
    }

}
