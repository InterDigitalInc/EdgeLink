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
package org.onosproject.mesh.controller.impl;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.onosproject.meshmanager.api.message.MeshSBIMessage;
import org.onosproject.meshmanager.api.message.MeshSBIMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decode Mesh message from a Channel, for use in a netty pipeline.
 */
public class MeshMessageDecoder extends FrameDecoder {

    private static final Logger log =
            LoggerFactory.getLogger(MeshMessageDecoder.class);
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel,
            ChannelBuffer buffer) throws Exception {
        if (!channel.isConnected()) {
            // In testing, I see decode being called AFTER decode last.
            // This check avoids that from reading corrupted frames
            return null;
        }
        log.trace("MeshMessageDecoder: decode ");
        MeshSBIMessageFactory factory = new MeshSBIMessageFactory();
        MeshSBIMessage message = factory.getMeshMessage(buffer);
        log.trace("Message Received:"+ message);
        return message;
    }

}
