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
import org.onosproject.net.DeviceId;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Base class for all Mesh SBI messages.
 */
public abstract class MeshSBIMessage extends MeshMessage {
    public static final int NC_TID_MASK = 0x80000000;
    public static final short MESH_MAGIC_NUMBER = 0x6634;
    public static final int SBI_HDR_LEN = 17;

    DeviceId deviceId;
    protected Short magic;
    protected MeshSBIMessageType type; //8 bits
    protected long nodeId;
    protected Integer tid;
    protected Short payloadLength;

    private final Logger log = getLogger(getClass());
    protected long dataPathId;

    // IE elements
    public static final byte IE_NOT_DEFINED = 0x00;
    public static final byte IE_RESULT_CODE = 0x01;
    public static final byte IE_ASSOC_CONFIG = 0x02;
    public static final byte IE_FWD_TBL_CONFIG = 0x03;
    public static final byte IE_LINK_REPORT_CONFIG = 0x04;
    public static final byte IE_BUFFER_REPORT_CONFIG = 0x05;
    public static final byte IE_NEIGHBOR_LIST = 0x06;
    public static final byte IE_CAPABILITY_INFO = 0x07;
    public static final byte IE_LINK_REPORT = 0x08;
    public static final byte IE_BUFFER_REPORT = 0x09;
    public static final byte IE_NEW_NODE = 0x0A; //obsolete, replaced by IE_LINK_INFO
    public static final byte IE_PACKET_INFO = 0x0B;
    public static final byte IE_CIR_CONFIG = 0x0C;
    public static final byte IE_INTF_CONFIG = 0x0D;
    public static final byte IE_INTF_SCHEDULE_ELEM = 0x0E;
    public static final byte IE_INTF_RESULTS = 0x0F;
    public static final byte IE_LINK_INFO = 0x10;
    public static final byte IE_LINK_INFO_PORT_INDEX = 0x11;
    public static final byte IE_EXTERNAL_PORT_INFO = 0x12;
    public static final byte IE_SLA_CONFIG = 0x13;
    public static final byte IE_MAX = 0x14;

    /**
     * Constructor
     */
    public MeshSBIMessage() {
        tid = 0;
    }

    /**
     * Set the device where/to this belong is from/or going to
     * @return
     */
    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Get the device where/to this belong is from/or going to
     * @return
     */
    public DeviceId getDeviceId() {
        return deviceId;
    }

    /**
     * Get the type of this message
     * @return
     */
    public MeshSBIMessageType getType() {
        return type;
    }

    /**
     * Set the type of this message
     * @param type
     */
    public void setType(MeshSBIMessageType type) {
        this.type = type;
    }

    /**
     * Get the nodeId of this message
     * @return
     */
    public long getNodeId() {
        return nodeId;
    }

    /**
     * Set the nodeId of this message
     * @param nodeId
     */
    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Get the transaction id of this message
     * @return
     */
    public Integer getTid() {
        return tid;
    }


    /**
     * Set the transaction id of this message
     * @param tid
     */
    public void setTid(Integer tid) {
        this.tid = tid;
    }

    /**
     * Set the PayLoad length of the message
     * @param length
     */
    public void setLength(Short length) {
        this.payloadLength = length;
    }

    /**
     * Get the payload length in message
     * @return payloadLength
     */
    public Short getLength() {
        return payloadLength;
    }

    /**
     * Set the datapathId of the channel
     * @param dpid
     */
    public void setDataPathId(long dpid) {
        this.dataPathId = dpid;
    }

    /**
     * Get the datapathId of the channel
     * @preturn dpid
     */
    public long getDataPathId() {
        return this.dataPathId;
    }

    /**
     *
     * @return
     */
    public boolean isCompleteMessage() {
        return true;
    }

    /**
     * Returns a summary of the message
     * @return "mesh msg: m=0x6634;t=$type:n=$nodeId:xid=$xid:l=$len"
     */
    public String toString() {
        String str = "mesh msg";
        //check if its an unsolicited msg
        if ((getTid() & NC_TID_MASK) == NC_TID_MASK) {
            str += " (unsolicited)";
        }

        str += ";t=" + getType();
        if (deviceId != null) {
            str += ";d=" + deviceId.toString();
        } else {
            str += ";d=null";
        }
        str += ";n=" + HexString.toHexString(getNodeId()) +
                ";x=" + Integer.toHexString(getTid());

        if (getType() == MeshSBIMessageType.NC_CONFIG_SETUP) {
            str += ";ie=" + ((MeshNCConfigSetup)this).getCfgType();
        }
        return str;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MeshSBIMessage)) {
            return false;
        }
        MeshSBIMessage other = (MeshSBIMessage) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (nodeId != other.nodeId) {
            return false;
        }
        if (tid != other.tid) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param buffer
     */
    public void readFrom(ChannelBuffer buffer) {
        Long dpid = buffer.readLong();
        if (dpid != 0) {
            setDataPathId(dpid);
            setNodeId(dpid);
            URI uri = null;
            try {
                uri = new URI("mesh:"+HexString.toHexString(dpid));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            DeviceId mesDid = DeviceId.deviceId(uri);
            setDeviceId(mesDid);
        }
        int transactionIdDecoded = buffer.readInt();
        setTid(transactionIdDecoded);
        log.trace("decode: tid: " + Integer.toHexString(transactionIdDecoded));
        Short length = buffer.readShort();
        this.setLength(length);
        log.trace("MeashHdr::readfrom: decoded dpid:{}, tid:{} length:{}",
                  dpid, Integer.toHexString(tid), length);
    }

    /**
     *
     * @param bb
     */
    public void writeTo(ChannelBuffer bb){
        log.trace("writing the message header");
        log.trace("type[{}] nodeId[{}] tid[{}]", type.getValue(),
                  HexString.toHexString(nodeId), Integer.toHexString(tid));

        bb.writeShort(MESH_MAGIC_NUMBER);
        bb.writeByte(type.getValue());
        bb.writeLong(nodeId);
        bb.writeInt(tid);
        //bb.writeShort(this.getLength());
    }
}
