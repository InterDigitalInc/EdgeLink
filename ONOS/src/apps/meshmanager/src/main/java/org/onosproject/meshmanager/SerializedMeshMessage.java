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
package org.onosproject.meshmanager;

import org.onosproject.meshmanager.api.message.MeshMessage;
import org.onosproject.meshmanager.api.message.MeshNCConfigConfirm;
import org.onosproject.meshmanager.api.message.MeshNCReportInd;
import org.onosproject.meshmanager.api.message.MeshSBIMessage;
import org.onosproject.meshmanager.api.message.OpenFlowMessage;
import org.onosproject.meshmanager.api.message.ResultCode;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class SerializedMeshMessage {
    private final Logger logger = getLogger(getClass());

    private boolean isTmp;
    private List<MeshMessage> prerequisiteList = new ArrayList<>();
    private List<MeshMessage> meshMsgList = new ArrayList<>();
    private List<OpenFlowMessage> ofMsgList = new ArrayList<>();
    private List<MeshFwdTblEntry> fwdTblEntryList = new ArrayList<>();

    /**
     * Constructor
     */
    public SerializedMeshMessage() {
        isTmp = false;
    }

    /**
     * Constructor
     */
    public SerializedMeshMessage(boolean isTmp) {
        this.isTmp = isTmp;
    }

    /**
     *
     * @param msg
     * @return
     */
    public boolean addPrerequisite(MeshMessage msg) {
        logger.trace("addPrerequisite: " + msg.toString() + " to serialized message: " + toString());
        return prerequisiteList.add(msg);
    }

    /**
     *
     * @param msg
     */
    public void removePrerequisite(MeshMessage msg) {
        logger.trace("removePrerequisite: " + msg.toString() + " from list: " +
                             prerequisiteList.toString());

        Iterator<MeshMessage> iterator = prerequisiteList.iterator();
        while (iterator.hasNext()) {
            MeshSBIMessage sbiMsg = (MeshSBIMessage)msg;
            MeshSBIMessage prereqMsg = (MeshSBIMessage)iterator.next();

            if ((sbiMsg.getType() == prereqMsg.getType()) &&
                    (sbiMsg.getNodeId() == prereqMsg.getNodeId())) {

                switch (prereqMsg.getType()) {
                    case NC_CONFIG_CONFIRM:
                        MeshNCConfigConfirm cnfMsg = (MeshNCConfigConfirm)msg;
                        MeshNCConfigConfirm cnfPrereqMsg = (MeshNCConfigConfirm)prereqMsg;
                        if (cnfMsg.getTid().equals(cnfPrereqMsg.getTid())) {
                            // Remove prerequisite if met successfully
                            if ((cnfMsg.getResultCodes().size() != 1) ||
                                    (cnfMsg.getResultCodes().get(0).getResult() == ResultCode.RESULT_SUCCESS)) {
                                iterator.remove();
                            }
                            continue;
                        }
                        break;
                    case NC_REPORT_INDICATION:
                        MeshNCReportInd indMsg = (MeshNCReportInd)msg;
                        MeshNCReportInd indPrereqMsg = (MeshNCReportInd)prereqMsg;
                        if ((indMsg.getReportCode() == indPrereqMsg.getReportCode()) &&
                                (indMsg.getReportCode() == MeshNCReportInd.REPORT_LINK_FAILURE)) {
                            if (indMsg.getOwnSector().equals(indPrereqMsg.getOwnSector()) &&
                                    indMsg.getPeerSector().equals(indPrereqMsg.getPeerSector())) {
                                iterator.remove();
                                continue;
                            }
                        }
                        break;
                    default:
                        throw new AssertionError("Invalid prereq msg type: " + prereqMsg.getType());
                }
            }
        }
    }

    /**
     *
     * @param nodeId
     */
    public void removePrerequisite(long nodeId) {
        logger.trace("removePrerequisite: for node ID: " + nodeId);

        Iterator<MeshMessage> iterator = prerequisiteList.iterator();
        while (iterator.hasNext()) {
            MeshSBIMessage prereqMsg = (MeshSBIMessage)iterator.next();
            if (nodeId == prereqMsg.getNodeId()) {
                iterator.remove();
            }
        }
    }

    /**
     *
     * @return
     */
    public boolean isTmp() {
        return isTmp;
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return (ofMsgList.isEmpty() && meshMsgList.isEmpty());
    }

    /**
     *
     * @return
     */
    public boolean isPrerequisiteListEmpty() {
        return prerequisiteList.isEmpty();
    }

    /**
     *
     * @return
     */
    public List<MeshMessage> getPrereqMsgList() {
        return prerequisiteList;
    }

    /**
     *
     * @return
     */
    public List<MeshMessage> getMeshMsgList() {
        return meshMsgList;
    }

    /**
     *
     * @param msg
     * @return
     */
    public boolean addMeshMsg(MeshMessage msg) {
        if (meshMsgList.contains(msg)) {
            return false;
        }
        logger.trace("Adding new MeshMessage: " + msg.toString());
        return meshMsgList.add(msg);
    }

    /**
     *
     * @param msg
     */
    public boolean removeMeshMsg(MeshMessage msg) {
        return meshMsgList.remove(msg);
    }

    /**
     *
     * @return
     */
    public List<OpenFlowMessage> getOpenFlowMsgList() {
        return ofMsgList;
    }

    /**
     *
     * @param ofMsg
     * @return
     */
    public boolean addOpenFlowMsg(OpenFlowMessage ofMsg) {
        if (ofMsgList.contains(ofMsg)) {
            return false;
        }
        logger.trace("Adding new OF Msg: " + ofMsg.toString());
        return ofMsgList.add(ofMsg);
    }

    /**
     *
     * @param ofMsg
     * @return
     */
    public boolean removeOpenFlowMsg(OpenFlowMessage ofMsg) {
        return ofMsgList.remove(ofMsg);
    }

    /**
     *
     * @return
     */
    public List<MeshFwdTblEntry> getFwdTblEntryList() {
        return fwdTblEntryList;
    }

    /**
     *
     * @param fwdTblEntry
     * @return
     */
    public boolean addFwdTblEntry(MeshFwdTblEntry fwdTblEntry) {
        if (fwdTblEntryList.contains(fwdTblEntry)) {
            return false;
        }
        logger.trace("Adding FWD TBL Entry: " + fwdTblEntry.getVlanId());
        return fwdTblEntryList.add(fwdTblEntry);
    }

    /**
     *
     * @return
     */
    public String toString() {
        String string;
        int i = 0;
        string = "\n\t\tprerequisites:";
        for(MeshMessage msg : prerequisiteList) {
            string += " (" + (i+1) + ") " + msg.toString();
            i++;
        }
        i = 0;
        string += "\n\t\tOF messages:";
        for(OpenFlowMessage msg : ofMsgList) {
            string += " (" + (i+1) + ") " + msg.toString();
            i++;
        }
        i = 0;
        string += "\n\t\tMesh messages:";
        for(MeshMessage msg : meshMsgList) {
            string += " (" + (i+1) + ") " + msg.toString();
            i++;
        }
        return string;
    }

}


