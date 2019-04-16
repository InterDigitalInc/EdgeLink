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
import org.jboss.netty.channel.ChannelFuture;
import org.onosproject.mesh.controller.MeshDpid;
import org.onosproject.meshmanager.api.message.MeshSBIMessage;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Mesh switch implementation class.
 */
public class MeshSwitchImpl extends AbstractHandlerBehaviour
        implements InternalMeshSwitch {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private MeshDpid dpid;
    private Channel channel;
    private MeshSwitchAgent agent;
    private boolean connected;

    public MeshSwitchImpl(MeshDpid dpid, Channel channel, MeshSwitchAgent agent) {
        this.dpid = dpid;
        this.channel = channel;
        this.agent = agent;
        connected = true;
    }

    //************************
    // MeshSwitch
    //************************

    @Override
    public final void disconnect() {
        if (connected) {
            connected = false;
            channel.close();
        }
    }

    @Override
    public void sendMsg(MeshSBIMessage msg) {
        log.trace("sendMsg({})", msg);
        if (channel.isConnected()) {
            ChannelFuture cf = channel.write(Collections.singletonList(msg));
            try {
                cf.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            log.warn("Switch[{}] not connected. Dropping message.", dpid);
        }
    }

    //************************
    // InternalMeshSwitch
    //************************

    @Override
    public final boolean add() {
        log.trace("adding a switch");
        return agent.addSwitch(this);
    }

    @Override
    public final void remove() {
        log.trace("removing switch");
        agent.removeSwitch(this);
    }

    @Override
    public final MeshDpid getDpid() {
        return dpid;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public final void handleMessage(MeshSBIMessage msg) {
        log.info("handleMessage(): " + msg);
        agent.processMessage(this, msg);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [" + ((channel != null) ?
                channel.getRemoteAddress() : "?") + " DPID[" +
                dpid.toString() +"]]";
    }
}
