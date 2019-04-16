/*
 * Copyright (c) 2017 IDCC, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.onosproject.mesh;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;

/**
 * Adapter for testing against a netty channel handler context.
 */
public class MeshChannelHandlerContextAdapter implements ChannelHandlerContext {
    @Override
    public Channel getChannel() {
        return null;
    }

    @Override
    public ChannelPipeline getPipeline() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ChannelHandler getHandler() {
        return null;
    }

    @Override
    public boolean canHandleUpstream() {
        return false;
    }

    @Override
    public boolean canHandleDownstream() {
        return false;
    }

    @Override
    public void sendUpstream(ChannelEvent channelEvent) {

    }

    @Override
    public void sendDownstream(ChannelEvent channelEvent) {

    }

    @Override
    public Object getAttachment() {
        return null;
    }

    @Override
    public void setAttachment(Object o) {

    }
}
