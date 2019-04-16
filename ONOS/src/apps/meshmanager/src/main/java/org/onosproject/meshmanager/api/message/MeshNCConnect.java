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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MeshNCConnect extends MeshSBIMessage {

    public static final int MINIMUM_LENGTH_NC_CONNECT = 9;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private byte version;
    private long bridgeMac;
    private boolean isCompleteMessage;

    //from LINK INFO
    private long ownSector;
    private long peerSector;
    private short ownSectPortIndex;

    /**
     *
     */
    public MeshNCConnect() {
        super();
        this.type = MeshSBIMessageType.NC_CONNECT;
        this.ownSectPortIndex = 0;
        this.ownSector = 0;
        this.peerSector = 0;
    }

    /**
     *
     * @param ver
     */
    public void setVersion(byte ver) {
        this.version = ver;
    }

    /**
     *
     * @param bridgeMac
     */
    public void setBridgeMac(long bridgeMac) {
        this.bridgeMac = bridgeMac;
    }

    /**
     *
     * @return
     */
    public byte getVersion() {
       return this.version;
    }

    /**
     *
     * @return
     */
    public long getBridgeMacAddr() {
        return bridgeMac;
    }

    /**
     *
     * @return
     */
    public long getOwnSector() {
        return this.ownSector;
    }

    /**
     *
     * @return
     */
    public long getPeerSector() {
        return this.peerSector;
    }

    /**
     *
     * @return
     */
    public short getOwnSectPortIndex() {
        return this.ownSectPortIndex;
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
            log.trace("MeshHello::readfrom: payload found payloadlength:" + ploadLength);
            decode(data);
        }
    }

    /**
     *
     * @param payload
     */
    private void decode(ChannelBuffer payload) {
        version = payload.readByte();
        bridgeMac = payload.readLong();
        int msgLength = getLength();
        int sizeRead = SBI_HDR_LEN + MINIMUM_LENGTH_NC_CONNECT;
        isCompleteMessage = true;

        log.trace("decoded version:{} and bridgemac:{} ", version, HexString.toHexString(bridgeMac));
        while ((payload.readable()) && (sizeRead < msgLength)) {
            log.trace("totoal bytes:{} bytes Read:{}",msgLength,sizeRead);
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
        short currentIeSize=0;
        ieType = payload.readByte();
        currentIeSize = payload.readShort();
        switch(ieType) {
            case IE_LINK_INFO:
                ownSector = payload.readLong();
                peerSector = payload.readLong();
                break;
            case IE_LINK_INFO_PORT_INDEX:
                ownSectPortIndex = payload.readShort();
                break;
            default:
                //ie not supported... we just move the pointer along and ignore it
                break;
        }
        return (currentIeSize + TlvIe.IE_HDR_LEN); //Ie length excludes header.
    }
}
