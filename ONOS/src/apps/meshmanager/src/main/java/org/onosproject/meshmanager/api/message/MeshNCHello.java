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
import org.onosproject.meshmanager.NeighborNode;
import org.onosproject.net.DeviceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MeshNCHello extends MeshSBIMessage {
    public static final int MINIMUM_LENGTH_NC_HELLO = 10;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public byte version;
    public byte nbSectors;
    public long bridgeMac;
    public List<NeighborList> neighborList = new ArrayList<NeighborList>();
    private CapabilityInfo capInfo;
    private short signalingPortIndex;
    private short dataPortIndex;
    private boolean isCompleteMessage;

    /**
     *
     */
    public MeshNCHello() {
        super();
        this.type = MeshSBIMessageType.NC_HELLO;
        this.signalingPortIndex = 0;
        this.dataPortIndex = 0;
    }

    /**
     *
     * @return
     */
    public byte getVersion() {
       return version;
    }

    /**
     *
     * @return
     */
    public byte getNbSectors() {
        return nbSectors;
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
    public List<NeighborList> getNeighborList() {
        return neighborList;
    }

    /**
     *
     * @return
     */
    public CapabilityInfo getCapabilityInfo() {
        return capInfo;
    }

    /**
     *
     * @param capInfo
     */
    public void setCapabilityInfo(CapabilityInfo capInfo) {
        this.capInfo = capInfo;
    }

    /**
     *
     * @return
     */
    public short getSignalingPortIndex() {
        return signalingPortIndex;
    }

    /**
     *
     * @return
     */
    public short getDataPortIndex() {
        return dataPortIndex;
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
    public void writeTo(ChannelBuffer data){
        log.trace("Mesh NC Hello write to called");
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
        nbSectors = payload.readByte();
        bridgeMac = payload.readLong();
        int msgLength = getLength();
        int sizeRead = SBI_HDR_LEN + MINIMUM_LENGTH_NC_HELLO;
        isCompleteMessage = true;

        if (getDataPathId() == 0) {
            //Temporarily done only for Hello messages
            setDataPathId(bridgeMac);
            setNodeId(bridgeMac);
            URI uri = null;
            try {
                uri = new URI("mesh:"+HexString.toHexString(bridgeMac));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            DeviceId mesDid = DeviceId.deviceId(uri);
            setDeviceId(mesDid);

        }
        log.trace("Decoded version:{} nbSector:{} and bridgemac:{} ", version, nbSectors,
                  HexString.toHexString(bridgeMac));

        while ((payload.readable()) && (sizeRead < msgLength)) {
            log.trace("total bytes:{} bytes Read:{}",msgLength,sizeRead);
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
        long mac;
        ieType = payload.readByte();
        currentIeSize = payload.readShort();
        switch(ieType) {
            case IE_NEIGHBOR_LIST: {
                int nbNeighborsRead = 0;
                byte nbNeighbors;
                short portIndex;
                NeighborList newNeighborList;

                //read ownaddress
                mac = payload.readLong();
                portIndex = payload.readShort();
                nbNeighbors = payload.readByte();

                log.trace("IE_NEIGHBOR_LIST mac:{} portIndex:{} nbNeighbors:{}",
                          HexString.toHexString(mac), portIndex, nbNeighbors);
                newNeighborList = new NeighborList(mac, portIndex);
                //read the neighbor nodes
                while (nbNeighborsRead < nbNeighbors) {
                    mac = payload.readLong(); //neighborMac
                    NeighborNode node = new NeighborNode(mac, null);
                    //new NeighborNode(mac);
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
            case IE_CAPABILITY_INFO: {
                //set capability info
                byte mcsSupport, nbBuffers, nodeAttributes;
                int bufferSize;
                nodeAttributes = payload.readByte();
                mcsSupport = payload.readByte();
                nbBuffers = payload.readByte();
                bufferSize = payload.readInt();
                log.trace("IE_CAPABILITY_INFO: mcsSupport:{} nbBuffers:{} bufferSize:{} ",
                          mcsSupport, nbBuffers, bufferSize);
                capInfo = new CapabilityInfo(nodeAttributes, mcsSupport, nbBuffers, bufferSize);

                break;
            }
            case IE_EXTERNAL_PORT_INFO: {
                this.signalingPortIndex = payload.readShort();
                this.dataPortIndex = payload.readShort();
                log.trace("IE_EXTERNAL_PORT_INFO: signalingPortIndex:{} dataPortIndex:{}",
                          signalingPortIndex, dataPortIndex);
                break;
            }
            default:
                //ie not supported... we just move the pointer along and ignore it
                break;
        }
        log.trace ("total bytes decoded from IE:"+(currentIeSize + TlvIe.IE_HDR_LEN));
        return (currentIeSize + TlvIe.IE_HDR_LEN); //Ie length excludes header.

    }

}

