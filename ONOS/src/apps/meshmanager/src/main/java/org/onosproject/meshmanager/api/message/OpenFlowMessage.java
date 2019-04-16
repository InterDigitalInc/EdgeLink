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
package org.onosproject.meshmanager.api.message;

import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onlab.util.HexString;
import org.onosproject.meshmanager.MeshFwdTblEntry;
import org.onosproject.meshmanager.MeshNode;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class OpenFlowMessage {
    private final Logger logger = getLogger(getClass());

    public static final byte ADD_FLOW = 0;
    public static final byte MODIFY_FLOW = 1;
    public static final byte DELETE_FLOW = 2;

    private FlowRule flowRule;
    byte command;


    /**
     *
     */
    public OpenFlowMessage() {

    }

    /**
     *
     * @return
     */
    public byte getCommand() {
        return command;
    }

    /**
     *
     * @return
     */
    public FlowRule getFlowRule() {
        return flowRule;
    }

    /**
     *
     * @param meshNode
     * @param fwdTblEntry
     * @param isTmp
     * @return
     */
    public boolean createFlowRule(MeshNode meshNode, MeshFwdTblEntry fwdTblEntry, boolean isTmp) {
        MeshFwdTblEntry.FwdTblEntry entry;
        logger.trace("createFlowRule: meshNode: " + HexString.toHexString(meshNode.getNodeId()));

        // Make sure mesh node & fwd tbl entry are valid
        if ((meshNode == null) || (fwdTblEntry == null)) {
            return false;
        }

        // Make sure device is present
        if (meshNode.getAssociatedDeviceId() == null) {
            logger.warn("No associated device. Ignoring OF Action[{}]", fwdTblEntry.getOfAction());
            return false;
        }

        // Determine required action & retrieve respective FWD TBL entry values to use
        if (isTmp) {
            command = DELETE_FLOW;
            entry = fwdTblEntry.getTmpFwdTblEntry();
        } else {
            switch (fwdTblEntry.getOfAction()) {
                case MeshFwdTblEntry.ADD:
                    command = ADD_FLOW;
                    break;
                case MeshFwdTblEntry.MODIFY:
                    command = MODIFY_FLOW;
                    break;
                case MeshFwdTblEntry.DELETE:
                    command = DELETE_FLOW;
                    break;
                default:
                    logger.error("Trying to encode a FWD TBL entry where no update is required");
                    return false;
            }
            entry = (command == DELETE_FLOW) ?
                    fwdTblEntry.getAppliedFwdTblEntry() :
                    fwdTblEntry.getFwdTblEntry();
        }

        // Set matching rules
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        if (entry.matchVlanId > 0) {
            selectorBuilder.matchVlanId(VlanId.vlanId(entry.matchVlanId));
        } else if (entry.matchSrcMac > 0) {
            selectorBuilder.matchEthSrc(MacAddress.valueOf(entry.matchSrcMac));
        } else if (entry.matchDstMac > 0) {
            selectorBuilder.matchEthDst(MacAddress.valueOf(entry.matchDstMac));
        }

        // Set action rules
        TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder();
        treatmentBuilder.setEthDst(MacAddress.valueOf(entry.actionPrimDstSectMac));
        if (entry.actionVlanId > 0) {
            treatmentBuilder.pushVlan()
                    .setVlanId(VlanId.vlanId(entry.actionVlanId))
                    .setVlanPcp(entry.actionPcp);
        } else if (entry.actionVlanId < 0) {
            treatmentBuilder.popVlan();
        }
        if (entry.actionPrimPort >= 0) {
            treatmentBuilder.setOutput(PortNumber.portNumber(entry.actionPrimPort));
        } else {
            treatmentBuilder.setOutput(PortNumber.LOCAL);
        }

        // Create flow rule
        flowRule = DefaultFlowRule.builder()
                .forDevice(meshNode.getAssociatedDeviceId())
                .withSelector(selectorBuilder.build())
                .withTreatment(treatmentBuilder.build())
                .withPriority(entry.priority)
                .withCookie(entry.entryId)
                .makePermanent()
                .build();

        return true;
    }
}
