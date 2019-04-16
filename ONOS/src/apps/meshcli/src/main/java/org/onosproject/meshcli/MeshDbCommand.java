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
import org.onosproject.meshmanager.MeshService;
import org.onosproject.meshmanager.api.message.CapabilityInfo;
import org.onosproject.meshmanager.api.message.MeshNCHello;
import org.onosproject.meshmanager.api.message.NeighborList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample Apache Karaf CLI command
 */
@Command(scope = "onos", name = "mesh-db",
        description = "CLI commands to print states of the mesh database")
public class MeshDbCommand extends AbstractShellCommand {
    @Argument(required = true, name = "dbgFct", description = "The command to display the info we need")
    String dbgFct = null;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MeshService meshService;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String MESH_APP_ID = "org.onosproject.mesh";
    private static final String HELP = "help";
    private static final String GETDB = "db";
    private static final String GETROUTINGMGR = "routingMgr";
    private static final String GETSLICEMGR = "sliceMgr";

    private ApplicationId appId;

    @Override
    protected void execute() {

        // Retrieve device service instance
        meshService = get(MeshService.class);

        // Process command
        if (dbgFct.equals(HELP)) {
            print("Debug information available using those commands:\n" +
                  "\tdb\n" +
                  "\troutingMgr\n" +
                  "\tsliceMgr");
        } else if (dbgFct.equals(GETDB)) {
            print(meshService.getMeshDB().printDB());
        } else if (dbgFct.equals("1")) {
            //creation of NCHELLO for GW
            MeshNCHello msg = new MeshNCHello();
            msg.version = (byte)1;
            msg.bridgeMac = (long)0x1111;
            NeighborList nl = new NeighborList((long)0x1111, (short)3);
            msg.neighborList.add(nl);
            CapabilityInfo capInfo = new CapabilityInfo((byte)1,(byte)2,(byte)3,4);
            msg.setCapabilityInfo(capInfo);
            meshService.addToMsgQ(msg);
        } else {
            print("Unsupported function call: " + dbgFct);
        }
    }
}
