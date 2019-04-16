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
import org.onlab.graph.MutableAdjacencyListsGraph;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.common.DefaultTopology;
import org.onosproject.common.DefaultTopologyGraph;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.topology.Topology;
import org.onosproject.net.topology.TopologyEdge;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static org.onlab.util.Tools.toHex;
import static org.onosproject.net.DeviceId.deviceId;


/**
 * Sample Apache Karaf CLI command
 */
@Command(scope = "onos", name = "mesh-topology",
        description = "CLI commands to use TopologyService interface")
public class MeshTopologyCommand extends AbstractShellCommand {
    @Argument(required = true, name = "commandParam", description = "The command to execute (get-paths|TBD)")
    String commandParam = null;
    @Argument(index = 1,  required = true, name = "srcIdParam", description = "The source device ID")
    String srcIdParam = null;
    @Argument(index = 2,  required = true, name = "dstIdParam", description = "The destination device ID")
    String dstIdParam = null;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String MESH_APP_ID = "org.onosproject.mesh";
    private static final String GET_PATHS = "get-paths";
    private static final String SCHEME = "of";

    @Override
    protected void execute() {
        // Retrieve topology service instance
        topologyService = get(TopologyService.class);

        // Process command
        if (commandParam.equals(GET_PATHS)) {
            print("Get Paths: srcId[%s] dstId[%s]", srcIdParam, dstIdParam);
            DeviceId srcDeviceId;
            DeviceId dstDeviceId;
            Long srcId = Long.parseLong(srcIdParam);
            Long dstId = Long.parseLong(dstIdParam);
            try {
                srcDeviceId = deviceId(new URI(SCHEME, toHex(srcId), null));
                dstDeviceId = deviceId(new URI(SCHEME, toHex(dstId), null));
                print("Src = %s", srcDeviceId.toString());
                print("Dst = %s", dstDeviceId.toString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }

            // Get latest topology
            DefaultTopology topology = (DefaultTopology)topologyService.currentTopology();
            DefaultTopologyGraph currentGraph = (DefaultTopologyGraph)topologyService.getGraph(topology);

            // Retrieve paths
            Set<Path> paths = topologyService.getPaths(topology, srcDeviceId, dstDeviceId);
            for (Path path : paths) {
                print("PRIMARY PATH:");
                print(path.toString());

                MutableAdjacencyListsGraph mutableGraph =
                        new MutableAdjacencyListsGraph<>(currentGraph.getVertexes(),
                                                         currentGraph.getEdges());

                // Remove links one at a time and recalculate paths
                for (Link link : path.links()) {
                    for (TopologyEdge edge : (Set<TopologyEdge>)mutableGraph.getEdges()) {
                        if (edge.link().equals(link)) {
                            mutableGraph.removeEdge(edge);

                            Topology modifiedTopology = new DefaultTopology(ProviderId.NONE, mutableGraph);
                            Set<Path> modifiedPaths = topologyService.getPaths(modifiedTopology, edge.src().deviceId(), dstDeviceId);

                            for (Path modifiedPath : modifiedPaths) {
                                print("ALTERNATE PATH:");
                                print(modifiedPath.toString());

                                // Only use first path as alternate path
                                break;
                            }
                            break;
                        }
                    }
                    // Only handle first link
                    break;
                }
                // Only use first path as primary path
                break;
            }
        }
        else {
            print("Unsupported command: " + commandParam);
        }
    }
}
