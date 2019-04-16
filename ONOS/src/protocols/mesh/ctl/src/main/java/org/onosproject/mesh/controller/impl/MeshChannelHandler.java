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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.onosproject.mesh.controller.SwitchStateException;
import org.onosproject.meshmanager.api.message.MeshSBIMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Channel handler deals with the switch connection and dispatches
 * switch messages to the appropriate locations.
 */
class MeshChannelHandler extends SimpleChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(MeshChannelHandler.class);
    private long dpid; // channelHandler cached value of connected switch id
    private final MeshControllerImpl controller;
    private InternalMeshSwitch sw;
    private Channel channel;

    /**
     * Create a new unconnected OFChannelHandler.
     * @param controller parent controller
     */
    MeshChannelHandler(MeshControllerImpl controller) {
        this.controller = controller;
    }

    //*************************
    //  Channel handler methods
    //*************************

    @Override
    public void channelConnected(ChannelHandlerContext ctx,
            ChannelStateEvent e) throws Exception {
        channel = e.getChannel();
        log.info("New switch connection from {}", channel.getRemoteAddress());
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx,
            ChannelStateEvent e) throws Exception {
        log.info("Channel Disconnected");
        if (sw != null) {
            sw.remove();
            sw = null;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        if (e.getCause() instanceof ReadTimeoutException) {
            // switch timeout
            log.error("Disconnecting switch {} due to read timeout",
                      getSwitchInfoString());
            ctx.getChannel().close();
        }
        log.info("exceptionCaught: In ChannelHandler: ", e);
        log.info("exception detail: ", e.getCause());
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        log.trace("messageReceived() from channel handler");
        if (e.getMessage() instanceof List) {
            @SuppressWarnings("unchecked")
            List<MeshSBIMessage> msglist = (List<MeshSBIMessage>) e.getMessage();
            for (MeshSBIMessage msg : msglist) {
                // Do the actual packet processing
                processMeshMessage(msg);
            }
        } else {
            MeshSBIMessage msg = (MeshSBIMessage) e.getMessage();
            if (msg != null) {
                processMeshMessage(msg);
            }
        }
    }

    private void processMeshMessage(MeshSBIMessage msg)
            throws IOException, SwitchStateException {
        log.trace("processMeshMessage()");

        // Get switch instance
        if (sw == null) {
            dpid = msg.getDataPathId();
            sw = controller.getMeshSwitchInstance(dpid, channel);
            sw.add();
        }
        sw.handleMessage(msg);
    }

    private String getSwitchInfoString() {
        log.trace("getSwitchInfoString()");
        if (sw != null) {
            return sw.toString();
        }
        String channelString;
        if (channel == null || channel.getRemoteAddress() == null) {
            channelString = "?";
        } else {
            channelString = channel.getRemoteAddress().toString();
        }
        String dpidString = String.valueOf(dpid);
        return String.format("[%s DPID[%s]]", channelString, dpidString);
    }
}
