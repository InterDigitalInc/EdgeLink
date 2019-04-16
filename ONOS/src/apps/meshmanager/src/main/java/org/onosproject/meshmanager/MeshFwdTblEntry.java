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

import org.onlab.util.HexString;
import org.onosproject.meshmanager.api.message.MeshConstants;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class MeshFwdTblEntry {
    private final Logger logger = getLogger(getClass());

    public static final byte STATUS_NEW = 0;
    public static final byte STATUS_MODIFY = 1;
    public static final byte STATUS_DELETE = 2;
    public static final byte STATUS_APPLIED = 3;

    public static final byte STATE_DISCONNECTED = 0;
    public static final byte STATE_UNCONFIRMED = 1;
    public static final byte STATE_CONFIRMED = 2;
    public static final byte STATE_COMPLETE = 3;

    public static final byte ACTION_FORWARD = 0;
    public static final byte ACTION_DROP = 1;
    public static final byte ACTION_CONTROLLER = 2;

    private static final short PRIO_UL_SIG = 1000;
    private static final short PRIO_UL_SIG_TMP = PRIO_UL_SIG - 1;
    private static final short PRIO_UL_DATA = PRIO_UL_SIG - 1;
    public static final short PRIO_DL_SIG = 12000;
    private static final short PRIO_DL_SIG_TMP = PRIO_DL_SIG - 1;
    private static final short PRIO_DL_DATA = PRIO_DL_SIG - 1;

    protected static final short VLAN_POP_MASK = (short) 0x8000;
    protected static final short VLAN_PUSH_MASK = (short) 0x7FFF;

    public static final byte MODE_GW = 0;
    public static final byte MODE_TRANSIT = 1;
    public static final byte MODE_PEER = 2;
    public static final byte MODE_LEAF = 3;

    public static final byte NONE = 0;
    public static final byte ADD = 1;
    public static final byte MODIFY = 1 << 1;
    public static final byte DELETE = 1 << 2;

    public static final short FLAG_ROUTE_TO_GW = 1;
    public static final short FLAG_MULTIHOP = 1 << 1;
    public static final short FLAG_DATA = 1 << 2;
    public static final short FLAG_OF_SENT = 1 << 3;
    public static final short FLAG_MESH_SENT = 1 << 4;
    public static final short FLAG_SERIALIZED = 1 << 5;

    private MeshDB meshDatabase;
    private MeshNode parentNode;
    private short id;
    private byte mode;
    private byte state;
    private byte status;
    private byte ofAction;
    private byte meshAction;
    private byte tmpAction;
    private short vlanId;
    private byte pcp;
    private short flags;
    private FwdTblEntry appliedFwdTblEntry;
    private FwdTblEntry currentFwdTblEntry;
    private FwdTblEntry tmpFwdTblEntry;

    /**
     *
     */
    public class FwdTblEntry {
        public short entryId;
        public short priority;
        public short inactivityTimer;
        public long primSrcSectMac;
        public long altSrcSectMac;
        public long primDstSectMac;
        public long altDstSectMac;

        // Matching criteria
        public int matchDstIp;
        public long matchSrcMac;
        public long matchDstMac;
        public byte matchQos;
        public short matchVlanId;
        public byte matchPcp;

        // Actions to be applied
        public byte actionCommand;
        public short actionPrimPort;
        public short actionAltPort;
        public long actionPrimSrcSectMac;
        public long actionAltSrcSectMac;
        public long actionPrimDstSectMac;
        public long actionAltDstSectMac;
        public long actionPrimDstNodeMac;
        public long actionAltDstNodeMac;
        public short actionVlanId;
        public byte actionPcp;
    }


    /**
     *
     * @param meshDatabase
     * @param parentNode
     * @param id
     * @param mode
     * @param state
     * @param isRouteToGw
     * @param isMultihop
     * @param isData
     * @param srcNodeMac
     * @param dstNodeMac
     * @param vlanId
     * @param pcp
     * @param port
     * @param linkSrcSectMac
     * @param linkDstSectMac
     * @param linkDstNodeMac
     */
    public MeshFwdTblEntry(
            MeshDB meshDatabase,
            MeshNode parentNode,
            short id,
            byte mode,
            byte state,
            boolean isRouteToGw,
            boolean isMultihop,
            boolean isData,
            long srcNodeMac,
            long dstNodeMac,
            short vlanId,
            byte pcp,
            short port,
            long linkSrcSectMac,
            long linkDstSectMac,
            long linkDstNodeMac) {

        this.meshDatabase = meshDatabase;
        this.parentNode = parentNode;
        this.id = id;
        this.mode = mode;
        this.state = state;
        this.vlanId = vlanId;
        this.pcp = pcp;

        // Set flags
        flags = 0;
        flags |= ((isRouteToGw) ? FLAG_ROUTE_TO_GW : 0);
        flags |= ((isMultihop) ? FLAG_MULTIHOP : 0);
        flags |= ((isData) ? FLAG_DATA : 0);

        // Fill FWD TBL entries with provided & default values
        tmpFwdTblEntry = null;
        appliedFwdTblEntry = null;
        currentFwdTblEntry = new FwdTblEntry();

        currentFwdTblEntry.entryId = -1;
        currentFwdTblEntry.priority = -1;
        currentFwdTblEntry.inactivityTimer = 0;
        currentFwdTblEntry.primSrcSectMac = linkSrcSectMac;
        currentFwdTblEntry.altSrcSectMac = -1;
        currentFwdTblEntry.primDstSectMac = linkDstSectMac;
        currentFwdTblEntry.altDstSectMac = -1;

        currentFwdTblEntry.matchDstIp = -1;
        currentFwdTblEntry.matchSrcMac = srcNodeMac;
        currentFwdTblEntry.matchDstMac = dstNodeMac;
        currentFwdTblEntry.matchQos = -1;
        currentFwdTblEntry.matchVlanId = -1;
        currentFwdTblEntry.matchPcp = -1;

        currentFwdTblEntry.actionCommand = ACTION_FORWARD;
        currentFwdTblEntry.actionPrimPort = port;
        currentFwdTblEntry.actionAltPort = -1;
        currentFwdTblEntry.actionPrimSrcSectMac = linkSrcSectMac;
        currentFwdTblEntry.actionAltSrcSectMac = -1;
        currentFwdTblEntry.actionPrimDstSectMac = linkDstSectMac;
        currentFwdTblEntry.actionAltDstSectMac = -1;
        currentFwdTblEntry.actionPrimDstNodeMac = linkDstNodeMac;
        currentFwdTblEntry.actionAltDstNodeMac = -1;
        currentFwdTblEntry.actionVlanId = 0;
        currentFwdTblEntry.actionPcp = 0;

        processChanges();
    }

    /**
     *
     * @param mode
     * @param state
     * @param isRouteToGw
     * @param isMultihop
     * @param isData
     * @param srcMac
     * @param dstMac
     * @param vlanId
     * @param pcp
     * @param port
     * @param linkSrcSectMac
     * @param linkDstSectMac
     * @param linkDstNodeMac
     */
    public void setPrimInfo(
            byte mode,
            byte state,
            boolean isRouteToGw,
            boolean isMultihop,
            boolean isData,
            long srcMac,
            long dstMac,
            short vlanId,
            byte pcp,
            short port,
            long linkSrcSectMac,
            long linkDstSectMac,
            long linkDstNodeMac) {

        this.mode = mode;
        this.state = state;
        this.vlanId = vlanId;
        this.pcp = pcp;

        // Set flags
        flags &= ~(FLAG_ROUTE_TO_GW | FLAG_MULTIHOP | FLAG_DATA);
        flags |= ((isRouteToGw) ? FLAG_ROUTE_TO_GW : 0);
        flags |= ((isMultihop) ? FLAG_MULTIHOP : 0);
        flags |= ((isData) ? FLAG_DATA : 0);

        // Create & fill new pending FWD TBL entry with provided & default values
        currentFwdTblEntry = new FwdTblEntry();

        currentFwdTblEntry.entryId = -1;
        currentFwdTblEntry.priority = -1;
        currentFwdTblEntry.inactivityTimer = 0;
        currentFwdTblEntry.primSrcSectMac = linkSrcSectMac;
        currentFwdTblEntry.altSrcSectMac = -1;
        currentFwdTblEntry.primDstSectMac = linkDstSectMac;
        currentFwdTblEntry.altDstSectMac = -1;

        currentFwdTblEntry.matchDstIp = -1;
        currentFwdTblEntry.matchSrcMac = srcMac;
        currentFwdTblEntry.matchDstMac = dstMac;
        currentFwdTblEntry.matchQos = -1;
        currentFwdTblEntry.matchVlanId = -1;
        currentFwdTblEntry.matchPcp = -1;

        currentFwdTblEntry.actionCommand = ACTION_FORWARD;
        currentFwdTblEntry.actionPrimPort = port;
        currentFwdTblEntry.actionAltPort = -1;
        currentFwdTblEntry.actionPrimSrcSectMac = linkSrcSectMac;
        currentFwdTblEntry.actionAltSrcSectMac = -1;
        currentFwdTblEntry.actionPrimDstSectMac = linkDstSectMac;
        currentFwdTblEntry.actionAltDstSectMac = -1;
        currentFwdTblEntry.actionPrimDstNodeMac = linkDstNodeMac;
        currentFwdTblEntry.actionAltDstNodeMac = -1;
        currentFwdTblEntry.actionVlanId = 0;
        currentFwdTblEntry.actionPcp = 0;

        processChanges();
    }

    /**
     *
     * @param port
     * @param linkSrcSectMac
     * @param linkDstSectMac
     * @param linkDstNodeMac
     */
    public void setAltInfo(short port, long linkSrcSectMac, long linkDstSectMac,
                           long linkDstNodeMac) {
        logger.debug("Setting alt port on entry " + currentFwdTblEntry.entryId +
                             " : from sector " + HexString.toHexString(linkSrcSectMac) +
                             " to " + HexString.toHexString(linkDstSectMac) +
                             " through " + port);

        // Fill pending FWD TBL entry with provided alternate path information
        currentFwdTblEntry.altSrcSectMac = linkSrcSectMac;
        currentFwdTblEntry.altDstSectMac = linkDstSectMac;

        currentFwdTblEntry.actionAltPort = port;
        currentFwdTblEntry.actionAltSrcSectMac = linkSrcSectMac;
        currentFwdTblEntry.actionAltDstSectMac = linkDstSectMac;
        currentFwdTblEntry.actionAltDstNodeMac = linkDstNodeMac;

        processChanges();
    }

    /**
     *
     */
    public void reset() {
        // Reset current FWD TBL Entry
        currentFwdTblEntry = null;

        // Update status
        checkForUpdates();
    }

    /**
     *
     * @return
     */
    public MeshNode getParentNode() {
        return parentNode;
    }

    /**
     *
     * @return
     */
    public byte getStatus() {
        return status;
    }

    /**
     *
     * @param newStatus
     */
    public void setStatus(byte newStatus) {
        logger.trace("FWD TBL Entry: " + id + " on node: " +
                             HexString.toHexString(parentNode.getOwnBridgeMac()) +
                             " set from " + getStatusString(status) +
                             " to " + getStatusString(newStatus));
        status = newStatus;

        // Store applied FWD TBL entry & reset action flags
        if (newStatus == STATUS_APPLIED) {
            appliedFwdTblEntry = currentFwdTblEntry;

            // Raise flag indicating OF or Mesh message was sent at least once
            flags |= ((ofAction != NONE) ? FLAG_OF_SENT : 0);
            flags |= ((meshAction != NONE) ? FLAG_MESH_SENT : 0);

            // Clear OF & MESH action flags
            ofAction = NONE;
            meshAction = NONE;
        }
    }

    /**
     *
     * @return
     */
    public boolean isOfUpdateRequired() {
        return (ofAction != NONE);
    }

    /**
     *
     * @return
     */
    public boolean isMeshUpdateRequired() {
        return (meshAction != NONE);
    }

    /**
     *
     * @return
     */
    public boolean isTmpUpdateRequired() {
        return (tmpAction != NONE);
    }

    /**
     *
     * @return
     */
    public short getEntryId() {
        return currentFwdTblEntry.entryId;
    }

    /**
     *
     * @return
     */
    public boolean isMultihop() {
        return ((flags & FLAG_MULTIHOP) != 0);
    }

    /**
     *
     * @return
     */
    public byte getMode() {
        return mode;
    }

    /**
     *
     * @param mode
     */
    public void setMode(byte mode) {
        this.mode = mode;
    }

    /**
     *
     * @return
     */
    public byte getState() {
        return mode;
    }

    /**
     *
     * @param state
     */
    public void setState(byte state) {
        this.state = state;
    }

    /**
     *
     * @return
     */
    public byte getOfAction() {
        return ofAction;
    }

    /**
     *
     * @return
     */
    public byte getMeshAction() {
        return meshAction;
    }

    /**
     *
     * @return
     */
    public FwdTblEntry getFwdTblEntry() {
        return currentFwdTblEntry;
    }

    /**
     *
     * @return
     */
    public FwdTblEntry getAppliedFwdTblEntry() {
        return appliedFwdTblEntry;
    }

    /**
     *
     * @return
     */
    public FwdTblEntry getTmpFwdTblEntry() {
        return tmpFwdTblEntry;
    }

    /**
     *
     * @param entry
     */
    public void setTmpFwdTblEntry(FwdTblEntry entry) {
        tmpFwdTblEntry = entry;

        // Reset TMP Action flag if entry set to null
        if (tmpFwdTblEntry == null) {
            tmpAction = NONE;
        }
    }

    /**
     *
     * @return
     */
    public boolean isRouteToGw() {
        return ((flags & FLAG_ROUTE_TO_GW) != 0);
    }

    /**
     *
     * @param value
     */
    public void setRouteToGw(boolean value) {
        if (value) {
            flags |= FLAG_ROUTE_TO_GW;
        } else {
            flags &= ~FLAG_ROUTE_TO_GW;
        }
    }

    /**
     *
     * @return
     */
    public boolean isSerialized() {
        return ((flags & FLAG_SERIALIZED) != 0);
    }

    /**
     *
     * @param value
     */
    public void setSerialized(boolean value) {
        if (value) {
            flags |= FLAG_SERIALIZED;
        } else {
            flags &= ~FLAG_SERIALIZED;
        }
    }

    /**
     *
     * @return
     */
    public short getVlanId() {
        return vlanId;
    }

    /**
     *
     * @return
     */
    public long getPrimDstNodeMac() {
        return (currentFwdTblEntry != null) ? currentFwdTblEntry.actionPrimDstNodeMac : -1;
    }

    /**
     * Returns a summary of the sector data
     */
    public String toString() {
        FwdTblEntry entry = (currentFwdTblEntry != null) ? currentFwdTblEntry : appliedFwdTblEntry;
        if (entry == null) {
            return "\n\tMESH FWD TBL ENTRY: NULL";
        }
        return "\n\t***** MESH FWD TBL ENTRY *****" +
                "\n\tparentNode=" + HexString.toHexString(parentNode.getOwnBridgeMac()) +
                "\n\tid=" + id +
                "\n\tmode=" + getModeString(mode) + " : " + mode +
                "\n\tstate=" + getStateString(state) + " : " + state +
                "\n\tstatus=" + getStatusString(status) + " : " + status +
                "\n\tofAction=" + getActionString(ofAction) +
                "\n\tmeshAction=" + getActionString(meshAction) +
                "\n\ttmpAction=" + getActionString(tmpAction) +
                "\n\tvlanId=" + vlanId +
                "\n\tpcp=" + pcp +
                "\n\tflags=" + Integer.toHexString(0xFFFF & flags) +

                "\n\tentryId=" + entry.entryId +
                "\n\tpriority=" + entry.priority +
                "\n\tinactivityTimer=" + entry.inactivityTimer +
                "\n\tprimSrcSectMac=" + HexString.toHexString(entry.primSrcSectMac) +
                "\n\taltSrcSectMac=" + HexString.toHexString(entry.altSrcSectMac) +
                "\n\tprimDstSectMac=" + HexString.toHexString(entry.primDstSectMac) +
                "\n\taltDstSectMac=" + HexString.toHexString(entry.altDstSectMac) +

                "\n\tmatchDstIp=" + entry.matchDstIp +
                "\n\tmatchSrcMac=" + HexString.toHexString(entry.matchSrcMac) +
                "\n\tmatchDstMac=" + HexString.toHexString(entry.matchDstMac) +
                "\n\tmatchQos=" + entry.matchQos +
                "\n\tmatchVlanId=" + entry.matchVlanId +
                "\n\tmatchPcp=" + entry.matchPcp +

                "\n\tactionCommand=" + entry.actionCommand +
                "\n\tactionPrimPort=" + entry.actionPrimPort +
                "\n\tactionAltPort=" + entry.actionAltPort +
                "\n\tactionPrimSrcSectMac=" + HexString.toHexString(entry.actionPrimSrcSectMac) +
                "\n\tactionAltSrcSectMac=" + HexString.toHexString(entry.actionAltSrcSectMac) +
                "\n\tactionPrimDstSectMac=" + HexString.toHexString(entry.actionPrimDstSectMac) +
                "\n\tactionAltDstSectMac=" + HexString.toHexString(entry.actionAltDstSectMac) +
                "\n\tactionPrimDstNodeMac=" + HexString.toHexString(entry.actionPrimDstNodeMac) +
                "\n\tactionAltDstNodeMac=" + HexString.toHexString(entry.actionAltDstNodeMac) +
                "\n\tactionVlanId=" + entry.actionVlanId +
                "\n\tactionPcp=" + entry.actionPcp + "\n";
    }

    /**
     *
     */
    private void processChanges() {
        boolean isRouteToGw = ((flags & FLAG_ROUTE_TO_GW) != 0);
        boolean isMultihop = ((flags & FLAG_MULTIHOP) != 0);
        boolean isData = ((flags & FLAG_DATA) != 0);

        // Make sure state is COMPLETE if data path
        if (isData && !(state == STATE_COMPLETE)) {
            throw new AssertionError("DATA Path while leaf node not in STATE_COMPLETE");
        }

        // Set default entry ID & priority based on flow direction and type
        currentFwdTblEntry.entryId = meshDatabase.getEntryId(id, isRouteToGw, isData);
        currentFwdTblEntry.priority = getPriority(isRouteToGw, isData);

        // Update matching & action rules. Also update priority for temporary rules.
        switch (mode) {
            case MODE_GW: {
                if (isRouteToGw) {
                    if (isMultihop || (state == STATE_COMPLETE)) {
                        // UL SIGNALING & DATA TUNNEL END POINT
                        matchVlan();
                        popVlan();
                    } else {
                        // UL SIGNALING SINGLE HOP AWAY
                        currentFwdTblEntry.priority = PRIO_UL_SIG_TMP;
                        matchSrcMac();

                        // Store as temporary FWD TBL entry to be removed on COMPLETE
                        tmpFwdTblEntry = currentFwdTblEntry;
                    }
                } else {
                    if (isMultihop || (state == STATE_COMPLETE)) {
                        // DL SIGNALING & DATA TUNNEL START POINT
                        matchDstMac();
                        pushVlan();
                    } else {
                        // DL SIGNALING SINGLE HOP AWAY
                        matchDstMac();
                    }
                }
                break;
            }
            case MODE_TRANSIT: {
                // TRANSIT nodes should only exist in multihop scenarios
                if (!isMultihop) {
                    throw new AssertionError("TRANSIT node in single hop scenario");
                }

                // UL & DL, SIGNALING & DATA PATH
                matchVlan();
                break;
            }
            case MODE_PEER: {
                // PEER nodes should only exist in multihop scenarios
                if (!isMultihop) {
                    throw new AssertionError("PEER node in single hop scenario");
                }

                if (isRouteToGw) {
                    if (state == STATE_COMPLETE) {
                        // UL SIGNALING & DATA PATH
                        matchVlan();
                    } else {
                        // UL SIGNALING TUNNEL START POINT
                        currentFwdTblEntry.priority = PRIO_UL_SIG_TMP;
                        matchSrcMac();
                        pushVlan();

                        // Store as temporary FWD TBL entry to be removed on COMPLETE
                        tmpFwdTblEntry = currentFwdTblEntry;
                    }
                } else {
                    if (state == STATE_COMPLETE) {
                        // DL SIGNALING & DATA PATH
                        matchVlan();
                    } else {
                        // DL SIGNALING TUNNEL END POINT
                        matchVlan();
                        popVlan();
                    }
                }
                break;
            }
            case MODE_LEAF: {
                if (isRouteToGw) {
                    // Hardcode Entry ID for UL signaling path only
                    if (!isData) {
                        currentFwdTblEntry.entryId = MeshConstants.CATCH_ALL_TO_NC_ENTRY_ID;
                    }

                    if (state == STATE_COMPLETE) {
                        // UL SIGNALING & DATA TUNNEL START POINT
                        matchSrcMac();
                        pushVlan();
                    } else {
                        // UL SIGNALING
                        matchSrcMac();
                    }
                } else {
                    if (state == STATE_COMPLETE) {
                        // DL SIGNALING & DATA TUNNEL END POINT
                        matchVlan();
                        popVlan();
                    } else {
                        // DL SIGNALING
                        currentFwdTblEntry.priority = PRIO_DL_SIG_TMP;
                        matchDstMac();

                        // Store as temporary FWD TBL entry to be removed on COMPLETE
                        tmpFwdTblEntry = currentFwdTblEntry;
                    }
                }
                break;
            }
            default:
                throw new AssertionError("Unsupported mode: " + mode);
        }

        // Check for OF & Mesh updates
        checkForUpdates();
    }

    /**
     *
     */
    private void checkForUpdates() {
        // Clear OF & MESH action flags
        ofAction = NONE;
        meshAction = NONE;
        tmpAction = NONE;

        // DELETE LEAF ENTRY if final different from temp assoc. This occurs when leaf node
        // is connected but associated device has been set to null.
        if ((mode == MODE_LEAF) && (state == STATE_DISCONNECTED) && parentNode.isConnected()) {
            ofAction = ((flags & FLAG_OF_SENT) != 0) ? DELETE : NONE;
            meshAction = ((flags & FLAG_MESH_SENT) != 0) ? DELETE : NONE;
            status = STATUS_DELETE;
        }
        // DELETE ENTRY if no longer used for routing
        else if (currentFwdTblEntry == null) {
            ofAction = ((flags & FLAG_OF_SENT) != 0) ? DELETE : NONE;
            meshAction = ((flags & FLAG_MESH_SENT) != 0) ? DELETE : NONE;
            status = STATUS_DELETE;
        }
        // ADD NEW ENTRY if it was never applied
        else if (appliedFwdTblEntry == null) {
            ofAction = ((mode != MODE_LEAF) || (state != STATE_DISCONNECTED)) ? ADD : NONE;
            meshAction = ((state == STATE_COMPLETE) && (currentFwdTblEntry.primSrcSectMac != -1)) ? ADD : NONE;
            status = STATUS_NEW;
        }
        // MODIFY (OR ADD) ENTRY if an update is required
        else {
            // Make sure entry ID has not changed
            if (currentFwdTblEntry.entryId != appliedFwdTblEntry.entryId) {
                throw new AssertionError("Entry ID changed from: " + appliedFwdTblEntry.entryId +
                                                 " to: " + currentFwdTblEntry.entryId);
            }

            // Check for OF updates
            if ((currentFwdTblEntry.priority != appliedFwdTblEntry.priority) ||
                    (currentFwdTblEntry.actionPrimPort != appliedFwdTblEntry.actionPrimPort) ||
                    (currentFwdTblEntry.actionPrimDstSectMac != appliedFwdTblEntry.actionPrimDstSectMac) ||
                    (currentFwdTblEntry.actionVlanId != appliedFwdTblEntry.actionVlanId) ||
                    (currentFwdTblEntry.actionPcp != appliedFwdTblEntry.actionPcp)) {
                ofAction = MODIFY;
            }

            // Check for MESH updates
            // NOTE: Mesh node must be in COMPLETE state to receive flow rule updates
            if (state == STATE_COMPLETE) {
                if ((flags & FLAG_MESH_SENT) == 0) {
                    meshAction = ADD;
                } else if ((currentFwdTblEntry.actionPrimPort != appliedFwdTblEntry.actionPrimPort) ||
                        (currentFwdTblEntry.actionPrimDstSectMac != appliedFwdTblEntry.actionPrimDstSectMac) ||
                        (currentFwdTblEntry.actionAltPort != appliedFwdTblEntry.actionAltPort) ||
                        (currentFwdTblEntry.actionAltDstSectMac != appliedFwdTblEntry.actionAltDstSectMac) ||
                        (currentFwdTblEntry.actionVlanId != appliedFwdTblEntry.actionVlanId) ||
                        (currentFwdTblEntry.actionPcp != appliedFwdTblEntry.actionPcp)) {
                    meshAction = MODIFY;
                }
            }

            // Check for TMP Tunnel deletion
            if (tmpFwdTblEntry != null) {
                if (state == STATE_COMPLETE) {
                    // FINAL tunnel confirmed
                    tmpAction = DELETE;
                } else if ((state == STATE_DISCONNECTED) &&
                        (currentFwdTblEntry.priority > tmpFwdTblEntry.priority)) {
                    // TMP tunnel moved to a new peer node
                    tmpAction = DELETE;
                }
            }

            // Set status depending on whether updates are required
            if ((ofAction != NONE) || (meshAction != NONE)) {
                status = STATUS_MODIFY;
            } else {
                status = STATUS_APPLIED;
            }
        }

        logger.trace("FWD TBL Entry[" + id + "] mesh node[" +
                             HexString.toHexString(parentNode.getOwnBridgeMac()) +
                             "] vlan[" + getVlanId() + "] status[" + getStatusString(status) +
                             "] ofaction[" + getActionString(ofAction) +
                             "] meshAction[" + getActionString(meshAction) +
                             "] tmpAction[" + getActionString(tmpAction) + "]");
    }

    /**
     * Set default priority based on flow direction and type
     * @param isRouteToGw
     * @param isData
     * @return Default flow priority
     */
    private short getPriority(boolean isRouteToGw, boolean isData) {
        return (isRouteToGw) ?
                ((isData) ? PRIO_UL_DATA : PRIO_UL_SIG) :
                ((isData) ? PRIO_DL_DATA : PRIO_DL_SIG);
    }

    /**
     *
     */
    private void matchVlan() {
        currentFwdTblEntry.matchDstMac = -1;
        currentFwdTblEntry.matchSrcMac = -1;
        currentFwdTblEntry.matchVlanId = vlanId;
        currentFwdTblEntry.matchPcp = pcp;
    }

    /**
     *
     */
    private void matchSrcMac() {
        currentFwdTblEntry.matchDstMac = -1;
        currentFwdTblEntry.matchVlanId = 0;
        currentFwdTblEntry.matchPcp = 0;
    }

    /**
     *
     */
    private void matchDstMac() {
        currentFwdTblEntry.matchSrcMac = -1;
        currentFwdTblEntry.matchVlanId = 0;
        currentFwdTblEntry.matchPcp = 0;
    }

    /**
     *
     */
    private void popVlan() {
        currentFwdTblEntry.actionVlanId = -1;
        currentFwdTblEntry.actionPcp = -1;
    }

    /**
     *
     */
    private void pushVlan() {
        currentFwdTblEntry.actionVlanId = vlanId;
        currentFwdTblEntry.actionPcp = pcp;
    }

    /**
     *
     * @param status
     * @return
     */
    private String getStatusString(byte status) {
        String string;
        switch(status) {
            case STATUS_NEW:
                string = "STATUS_NEW";
                break;
            case STATUS_MODIFY:
                string = "STATUS_MODIFY";
                break;
            case STATUS_DELETE:
                string = "STATUS_DELETE";
                break;
            case STATUS_APPLIED:
                string = "STATUS_APPLIED";
                break;
            default:
                throw new AssertionError("Unsupported status: " + status);
        }
        return string;
    }

    /**
     *
     * @param action
     * @return
     */
    private String getActionString(byte action) {
        String string;
        switch(action) {
            case NONE:
                string = "NONE";
                break;
            case ADD:
                string = "ADD";
                break;
            case MODIFY:
                string = "MODIFY";
                break;
            case DELETE:
                string = "DELETE";
                break;
            default:
                throw new AssertionError("Unsupported action: " + action);
        }
        return string;
    }

    /**
     *
     * @param state
     * @return
     */
    private String getStateString(byte state) {
        String string;
        switch(state) {
            case STATE_DISCONNECTED:
                string = "STATE_DISCONNECTED";
                break;
            case STATE_UNCONFIRMED:
                string = "STATE_UNCONFIRMED";
                break;
            case STATE_CONFIRMED:
                string = "STATE_CONFIRMED";
                break;
            case STATE_COMPLETE:
                string = "STATE_COMPLETE";
                break;
            default:
                throw new AssertionError("Unsupported state: " + state);
        }
        return string;
    }

    /**
     *
     * @param mode
     * @return
     */
    private String getModeString(byte mode) {
        String string;
        switch(mode) {
            case MODE_GW:
                string = "MODE_GW";
                break;
            case MODE_TRANSIT:
                string = "MODE_TRANSIT";
                break;
            case MODE_PEER:
                string = "MODE_PEER";
                break;
            case MODE_LEAF:
                string = "MODE_LEAF";
                break;
            default:
                throw new AssertionError("Unsupported mode: " + mode);
        }
        return string;
    }
}

