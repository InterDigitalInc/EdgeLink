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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MeshNCReset extends MeshSBIMessage {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private byte version;
    private int resetId;
    private boolean internallyGenerated;

    /**
     *
     */
    public MeshNCReset() {
        super();
        this.type = MeshSBIMessageType.NC_RESET;
        this.internallyGenerated = false;
    }

    /**
     *
     * @param version
     * @param resetId
     */
    public MeshNCReset(byte version, int resetId) {
        super();
        this.type = MeshSBIMessageType.NC_RESET;
        this.version = version;
        this.resetId = resetId;
        this.internallyGenerated = true;
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
    public int getResetId() {
        return resetId;
    }

    /**
     *
     * @return
     */
    public boolean isInternallyGenerated() {
        return internallyGenerated;
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
            log.trace("MeshNCConfig::readfrom: payload found payload length:"+ploadLength);
            decode(data);
        }
    }

    /**
     *
     * @param payload
     */
    public void decode(ChannelBuffer payload) {
        version = payload.readByte();
        resetId = payload.readInt();
    }
}

