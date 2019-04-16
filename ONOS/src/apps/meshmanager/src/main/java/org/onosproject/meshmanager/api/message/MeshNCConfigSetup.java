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

import org.jboss.netty.buffer.ChannelBuffer;
import org.onlab.util.HexString;
import org.onosproject.meshmanager.AccessNode;
import org.onosproject.meshmanager.MeshFwdTblEntry;
import org.onosproject.meshmanager.MeshNode;
import org.onosproject.meshmanager.MeshSector;
import org.onosproject.meshmanager.NeighborNode;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class MeshNCConfigSetup extends MeshSBIMessage {
    private final Logger logger = getLogger(getClass());
 
    private long nodeId;
    private byte version;
    private List<TlvIe> tlvs = new ArrayList<>();

    private static final byte COMMAND_ADD = 0;
    private static final byte COMMAND_MODIFY = 1;
    private static final byte COMMAND_DELETE = 2;

    private static final int CFG_SETUP_HDR_LEN = 9;

    public static final byte CFG_TYPE_ASSOC = 1;
    public static final byte CFG_TYPE_FWD_TBL = 1 << 1;
    public static final byte CFG_TYPE_LINK_REPORT = 1 << 2;
    public static final byte CFG_TYPE_BUFFER_REPORT = 1 << 3;
    public static final byte CFG_TYPE_SLA = 1 << 4;
    private byte cfgType = 0;

    /**
     *
     */
    public MeshNCConfigSetup() {
        super();
        type = MeshSBIMessageType.NC_CONFIG_SETUP;
    }

    /**
     *
     */
    public MeshNCConfigSetup(MeshNode meshNode) {
        super();

        type = MeshSBIMessageType.NC_CONFIG_SETUP;
        version = meshNode.getVersion();
        nodeId = meshNode.getNodeId();
        deviceId = meshNode.getMeshDeviceId();
        tid = meshNode.getNewTid();

        super.nodeId = nodeId;
    }

    /**
     *
     * @param version
     * @param nodeId
     * @param tid
     */
    public void setBasics(byte version, long nodeId, int tid) {
        this.version = version;
        this.nodeId = nodeId;
        super.nodeId = nodeId;
        this.tid = tid;
    }

    /**
     *
     * @param msg
     */
    public void setBasicsFrom(MeshNCConfigSetup msg) {
        version = msg.version;
        this.nodeId = msg.nodeId;
        super.nodeId = msg.nodeId;
        tid = msg.tid;
    }

    /**
     *
     * @return
     */
    public byte getCfgType() {
        return cfgType;
    }

    /**
     *
     * @param meshSector
     * @return
     */
    public int encodeAssocIe(MeshSector meshSector) {
        Long meshSectorMac = meshSector.getOwnSectMac();
        NeighborNode neighNode;
        int tlvSize = tlvs.size();
        logger.trace("Encoding assoc ie for: " + HexString.toHexString(meshSectorMac));

        // Create new ASSOC IE TLV
        TlvIe tlv = new TlvIe(IE_ASSOC_CONFIG);

        // Append own sector mac
        tlv.append(meshSectorMac);

        // Append peer sector mac (0 if no neighbor)
        neighNode = meshSector.getFirstNeighborNode();
        tlv.append((neighNode == null) ? (long)0 : neighNode.getPeerSectMac());

        // Append sector personality
        tlv.append(meshSector.getSectorPersonality());

        // Add TLV to list
        tlvs.add(tlv);

        cfgType |= CFG_TYPE_ASSOC;
        return (tlvs.size() - tlvSize);
    }

    /**
     *
     * @param accessNode
     * @param direction
     * @return
     */
    public int encodeSlaIe(AccessNode accessNode, short direction) {
        int tlvSize = tlvs.size();

        if (accessNode != null) {
            TlvIe tlv = new TlvIe(IE_SLA_CONFIG);
            tlv.append(accessNode.getUniqueId());
            tlv.append(accessNode.getOwnMac());

            // If UL, populate IE with UL metrics for leaf node
            // If DL, populate IE with DL metrics for GW node
            if (direction == MeshConstants.UPLINK) {
                tlv.append((short)accessNode.getReqSlaUl());
                tlv.append(accessNode.getCbsUl());
                tlvs.add(tlv);
            } else {
                tlv.append((short)accessNode.getReqSlaDl());
                tlv.append(accessNode.getCbsDl());
                tlvs.add(tlv);
            }
        }

        cfgType |= CFG_TYPE_SLA;
        return (tlvs.size() - tlvSize);
    }

    /**
     *
     * @return
     */
    public int encodeLinkReportIe() {
        int tlvSize = tlvs.size();

        TlvIe tlv = new TlvIe(IE_LINK_REPORT_CONFIG);
        tlv.append(LinkReport.DEFAULT_LINK_REPORT_PERIODICITY);
        tlv.append(LinkReport.DEFAULT_LINK_REPORT_THRESHOLD);
        tlvs.add(tlv);

        cfgType |= CFG_TYPE_LINK_REPORT;
        return (tlvs.size() - tlvSize);
    }

    /**
     *
     * @return
     */
    public int encodeBufferReportIe() {
        int tlvSize = tlvs.size();

        TlvIe tlv = new TlvIe(IE_BUFFER_REPORT_CONFIG);
        tlv.append(BufferReport.DEFAULT_AVG_PKT_DELAY);
        tlv.append(BufferReport.DEFAULT_AVG_BUFFER_OCCUPANCY);
        tlvs.add(tlv);

        cfgType |= CFG_TYPE_BUFFER_REPORT;
        return (tlvs.size() - tlvSize);
    }

    /**
     *
     * @param meshNode
     * @param fwdTblEntry
     * @return
     */
    public int encodeFwdTblIe(MeshNode meshNode, MeshFwdTblEntry fwdTblEntry) {
        int tlvSize = tlvs.size();
        byte command;

        // Make sure mesh node & fwd tbl entry are valid
        if ((meshNode == null) || (fwdTblEntry == null)) {
            return 0;
        }

        // Make sure device is present
        if (meshNode.getAssociatedDeviceId() == null) {
            logger.warn("No associated device. Ignoring MESH Action[{}]",
                        fwdTblEntry.getMeshAction());
            return 0;
        }

        // Determine required action based on FWD TBL entry processing
        switch (fwdTblEntry.getMeshAction()) {
            case MeshFwdTblEntry.ADD:
                command = COMMAND_ADD;
                break;
            case MeshFwdTblEntry.MODIFY:
                command = COMMAND_MODIFY;
                break;
            case MeshFwdTblEntry.DELETE:
                command = COMMAND_DELETE;
                break;
            default:
                logger.error("Trying to encode a FWD TBL entry where no update is required");
                return 0;
        }

        MeshFwdTblEntry.FwdTblEntry entry = (command == COMMAND_DELETE) ?
                fwdTblEntry.getAppliedFwdTblEntry() :
                fwdTblEntry.getFwdTblEntry();

        // Encode FWD TBL entry as a FWD TBL IE
        logger.trace("for nodeId: " + HexString.toHexString(nodeId) + " encoding rule: " +
                             entry.entryId + " as command: " + command +
                             " in msg tid: " + Integer.toHexString(tid));

        TlvIe tlv = new TlvIe(IE_FWD_TBL_CONFIG);
        tlv.append(command);
        tlv.append(entry.entryId);
        tlv.append(entry.priority);
        tlv.append(entry.inactivityTimer);
        tlv.append(entry.primSrcSectMac);
        tlv.append(entry.altSrcSectMac);
        tlv.append(entry.primDstSectMac);
        tlv.append(entry.altDstSectMac);
        tlv.append(entry.matchDstIp);
        tlv.append(entry.matchSrcMac);
        tlv.append(entry.matchDstMac);
        tlv.append(entry.matchQos);
        tlv.append(entry.matchVlanId);
        tlv.append(entry.matchPcp);
        tlv.append(entry.actionCommand);
        tlv.append(entry.actionPrimPort);
        tlv.append(entry.actionAltPort);
        tlv.append(entry.actionPrimSrcSectMac);
        tlv.append(entry.actionAltSrcSectMac);
        tlv.append(entry.actionPrimDstSectMac);
        tlv.append(entry.actionAltDstSectMac);
        tlv.append(entry.actionPrimDstNodeMac);
        tlv.append(entry.actionAltDstNodeMac);
        tlv.append(entry.actionVlanId);
        tlv.append(entry.actionPcp);
        tlvs.add(tlv);

        cfgType |= CFG_TYPE_FWD_TBL;
        logger.trace("total new IE encoded=" + (tlvs.size() - tlvSize));
        return (tlvs.size() - tlvSize);
    }

    /**
     *
     * @param version
     */
    public void setVersion(byte version) {
       this.version = version;
    }

    /**
     *
     * @param nodeId
     */
    public void setConfigNodeId(long nodeId) {
        this.nodeId = nodeId;
        super.nodeId = nodeId;
    }

    /**
     *
     * @return
     */
    public boolean isEmptyPayload() {
        return tlvs.isEmpty();
    }

    /**
     *
     * @param bb
     */
    @Override
    public void writeTo(ChannelBuffer bb) {
        logger.trace("Writing NCConfigSetup to channel buffer");
        super.writeTo(bb);
        int length = getEncodedMessageLength();

        bb.writeShort(length);
        bb.writeLong(nodeId);
        bb.writeByte(version);
        logger.trace( "nodeId:{} version:{} length:{}", HexString.toHexString(nodeId), version, length);

        for (TlvIe tlv : tlvs) {
            bb.writeByte(tlv.getType());
            bb.writeShort(tlv.getLength());
            ByteBuffer buff = tlv.getPayload();
            if (buff != null) {
                buff.limit(buff.position());
                buff.rewind();
                logger.trace("buffer: " + buff);
                bb.writeBytes(buff);
            }
        }
    }

    /**
     *
     * @return
     */
    private int getEncodedMessageLength() {
        int size = CFG_SETUP_HDR_LEN + SBI_HDR_LEN;

        for (TlvIe tlv : tlvs) {
            size += (tlv.getLength() + TlvIe.IE_HDR_LEN);
        }
        return size;
    }
}

