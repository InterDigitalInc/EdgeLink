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
import org.onosproject.meshmanager.MeshSector;
import org.onosproject.meshmanager.NeighborNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MeshNCReportInd extends MeshSBIMessage {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private byte reportCode;
    private boolean isCompleteMessage;

    //set after reading the message to be processed
    private Long ownSector;
    private Long peerSector;
    private List<NeighborList> neighborList = new ArrayList<>();
    private List<LinkReportInfo> linkReportInfoList = new ArrayList<>();

    //reportCode enum
    public static final byte REPORT_PERIODIC = 0x01;
    public static final byte REPORT_EVENT_TROGGERED = 0x02;
    public static final byte REPORT_PACKET_IN = 0x03;
    public static final byte REPORT_NEW_NODE = 0x04;
    public static final byte REPORT_LINK_FAILURE = 0x05;
    public static final byte REPORT_FLOW_EXPIRY = 0x06;
    public static final byte REPORT_PERSONALITY_SWITCH = 0x07;
    public static final byte REPORT_NL_UPDATE = 0x08;
    public static final byte REPORT_SCHED_FAILURE = 0x09;
    public static final byte REPORT_CODE_MAX = 0x09;

    /**
     * Constructor
     */
    public MeshNCReportInd() {
        super();
        type = MeshSBIMessageType.NC_REPORT_INDICATION;
    }

    /**
     * Constructor
     * @param reportCode
     * @param ownSector
     * @param peerSector
     */
    public MeshNCReportInd(byte reportCode, MeshSector ownSector, MeshSector peerSector) {
        super();
        type = MeshSBIMessageType.NC_REPORT_INDICATION;
        this.reportCode = reportCode;
        this.ownSector = ownSector.getOwnSectMac();
        this.peerSector = peerSector.getOwnSectMac();
        this.nodeId = ownSector.getParentNode().getNodeId();
    }

    /**
     *
     * @param reportCode
     * @param ownSectMac
     * @param peerSectMac
     */
    public void setLinkFailureContent(byte reportCode, long ownSectMac, long peerSectMac) {
        this.reportCode = reportCode;
        this.ownSector = ownSectMac;
        this.peerSector = peerSectMac;
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
    public byte getReportCode() {
       return reportCode;
    }

    /**
     *
     * @return
     */
    public String getReportCodeString() {
        String str;
        switch(this.reportCode) {
        case REPORT_PERIODIC:
            str = "REPORT_PERIODIC";
            break;
        case REPORT_EVENT_TROGGERED:
            str = "REPORT_EVENT_TROGGERED";
            break;
        case REPORT_PACKET_IN:
            str = "REPORT_PACKET_IN";
            break;
        case REPORT_NEW_NODE:
            str = "REPORT_NEW_NODE";
            break;
        case REPORT_LINK_FAILURE:
            str = "REPORT_LINK_FAILURE";
            break;
        case REPORT_FLOW_EXPIRY:
            str = "REPORT_FLOW_EXPIRY";
            break;
        case REPORT_PERSONALITY_SWITCH:
            str = "REPORT_PERSONALITY_SWITCH";
            break;
        case REPORT_NL_UPDATE:
            str = "REPORT_NL_UPDATE";
            break;
        case REPORT_SCHED_FAILURE:
            str = "REPORT_SCHED_FAILURE";
            break;
        default:
            str = "UNDEFINED(" + this.reportCode + ")";
            break;
        }
        return str;
     }

    /**
     *
     * @return
     */
    public Long getOwnSector() {
        return ownSector;
    }

    /**
     *
     * @return
     */
    public Long getPeerSector() {
        return peerSector;
    }

    /**
     *
     * @return
     */
    public List<NeighborList> getNeighborList() {
        return neighborList;
    }

    /**
     *
     * @return
     */
    public List<LinkReportInfo> getLinkReportInfoList() {
        return linkReportInfoList;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isCompleteMessage() {
        return isCompleteMessage;
    }

    /**
     *
     * @param data
     */
    @Override
    public void readFrom(ChannelBuffer data) {
        super.readFrom(data);

        int ploadLength = getLength() - SBI_HDR_LEN;
        if (ploadLength > 0) {
            log.trace("MeshNCReportInd::readfrom: payload found payloadlength:" + ploadLength);
            decode(data);
        }
    }

    /**
     *
     * @param payload
     */
    private void decode(ChannelBuffer payload) {
        int sizeRead = SBI_HDR_LEN;
        int msgLength = getLength();
        isCompleteMessage = true;

        decodeReportCode(payload);
        sizeRead++;

        // Capacity represents all the msgLength of the different messages if in a list
        if (msgLength != payload.capacity()) {
            log.info("More than 1 message in channel buffer. Processing first one only.");
        }
        while ((payload.readable()) && (sizeRead < msgLength)) {
            log.trace("total bytes:{} bytes Read:{}", msgLength, sizeRead);
            sizeRead += decodeNextIE(payload);
        }
        if (!payload.readable() && (sizeRead < msgLength)) {
            log.trace("Incomplete message :: sizeRead:" + sizeRead + " , msgLength:" + msgLength);
            isCompleteMessage = false;
        }
    }

    /**
     *
     * @param payload
     * @return
     */
    private int decodeNextIE(ChannelBuffer payload) {
        byte ieType;
        short currentIeSize;

        ieType = payload.readByte();
        log.trace("IeType received is:"+ ieType);

        //skip the rest of the message, since we have no idea if the rest of the message makes sense
        if(ieType == IE_NOT_DEFINED) {
            return 1;
        }
        currentIeSize = payload.readShort();
        switch(ieType) {
            case IE_NEW_NODE:
                //we will send a config setup as a response to this new node, store the values
                ownSector = payload.readLong();
                peerSector = payload.readLong();
                break;
            case IE_LINK_INFO:
                //we will send a config setup as a response to this new node, store the values
                ownSector = payload.readLong();
                peerSector = payload.readLong();
                break;
            case IE_PACKET_INFO:
                //i don't really care, the controller will take care of that
                break;
            case IE_NEIGHBOR_LIST: {
                //temporarily valid... will be replaced by another IE
                NeighborList newNeighborList;
                long mac;
                int nbNeighbors, nbNeighborsRead = 0;
                short ownSectPortIndex = 0;

                //read ownaddress
                ownSector = payload.readLong();
                ownSectPortIndex = payload.readShort();
                nbNeighbors = payload.readByte();

                newNeighborList = new NeighborList(ownSector, ownSectPortIndex);
                //newNeighborList.setNbOwnSectors(nbSectors);

                //read the neighbor nodes
                while (nbNeighborsRead < nbNeighbors) {
                    mac = payload.readLong(); //neighborMac
                    NeighborNode node = new NeighborNode(mac, null);
                    newNeighborList.addNeighbor(node);

                    //read the link report list
                    LinkReport lr = new LinkReport();
                    lr.setLinkMcs(payload.readByte());
                    node.setLinkReport(lr);

                    //read the buffer report list
                    BufferReport br = new BufferReport();
                    br.setAvgPktDelay(payload.readShort());
                    br.setAvgBufferOccupancy(payload.readByte());
                    node.setBufferReport(br);
                    nbNeighborsRead++;
                }

                neighborList.add(newNeighborList);
                break;
            }
            case IE_LINK_REPORT: {
                LinkReportInfo linkReportInfo = new LinkReportInfo();

                // Link Info
                linkReportInfo.setOwnSectorMAC(payload.readLong());
                linkReportInfo.setPeerSectorMAC(payload.readLong());

                // Link Report
                LinkReport linkReport = new LinkReport();
                linkReport.setLinkMcs(payload.readByte());
                linkReportInfo.setLinkReport(linkReport);

                linkReportInfoList.add(linkReportInfo);
                break;
            }
            default:
                //ie not supported... we just move the pointer along and ignore it
                break;
        }

        return currentIeSize + TlvIe.IE_HDR_LEN; //Ie length excludes header
    }

    /**
     *
     * @param payload
     */
    private void decodeReportCode(ChannelBuffer payload) {
        reportCode = payload.readByte();
        log.debug("ReportIndication code:"+ getReportCodeString());
    }
}

