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
import org.jboss.netty.buffer.ChannelBuffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 *
 */
public class MeshSBIMessageFactory {

    public static final int MINIMUM_LENGTH_FOR_MAGIC_NO_AND_TYPE = 3;

    private static final Logger log = LoggerFactory.getLogger(MeshSBIMessageFactory.class);

    /**
     *
     * @param buffer
     * @return
     */
    public MeshSBIMessage getMeshMessage(ChannelBuffer buffer) {

        if (buffer.readableBytes() < MINIMUM_LENGTH_FOR_MAGIC_NO_AND_TYPE) {
            log.error("Minimumn bytes to read magic number and message type is not available");
            return null;
        }

        log.trace("getMeshMessage: reading buffer to get message type");
        MeshSBIMessage message = null;
        int oldReaderIndex = buffer.readerIndex();
        short magic = buffer.readShort();

        log.trace("getMeshMessage: magic: " + Integer.toHexString(magic));
        if (magic != MeshSBIMessage.MESH_MAGIC_NUMBER) {
            log.trace("Invalid magic number");
            return null;
        }

        int type = (int) buffer.readByte();
        MeshSBIMessageType mtype = MeshSBIMessageType.lookup.get(type);
        log.trace("getMeshMessage: MessageType:" + mtype);
        try {
            switch (mtype) {
                case NC_CONNECT:
                    message = new MeshNCConnect();
                    message.readFrom(buffer);
                    break;
                case NC_HELLO:
                    message = new MeshNCHello();
                    message.readFrom(buffer);
                    break;
                case NC_CONFIG_CONFIRM:
                    message = new MeshNCConfigConfirm();
                    message.readFrom(buffer);
                    break;
                case NC_REPORT_INDICATION:
                    message = new MeshNCReportInd();
                    message.readFrom(buffer);
                    break;
                case NC_RESET:
                    message = new MeshNCReset();
                    message.readFrom(buffer);
                    break;
                default:
                    while (buffer.readable()) {
                        buffer.readByte();
                    }
                    break;
            }
        } catch (IndexOutOfBoundsException ibe) {
            log.error("getMeshMessage: IndexOutOfBoundsException");
            buffer.readerIndex(oldReaderIndex);
            return null;
        }

        // Make sure complete message has been received
        if ((message != null) && !message.isCompleteMessage()) {
            log.error("getMeshMessage: Incomplete message");
            buffer.readerIndex(oldReaderIndex);
            return null;
        }

        return message;
    }

    /**
     *
     * @param buffer
     * @return
     */
    public MeshSBIMessage getMeshMessage(ByteBuffer buffer) {
        ChannelBuffer cb = ChannelBuffers.dynamicBuffer();
        if (buffer != null) {
            cb.writeBytes(buffer);
        } else {
            return null;
        }
        return getMeshMessage(cb);
    }
}
