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

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.concurrent.ThreadPoolExecutor;

import static org.onlab.util.Tools.groupedThreads;

/**
 * Creates a ChannelPipeline for a server-side mesh channel.
 */
public class MeshPipelineFactory
        implements ChannelPipelineFactory, ExternalResourceReleasable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SSLContext sslContext;
    protected MeshControllerImpl controller;
    protected ThreadPoolExecutor pipelineExecutor;
    protected Timer timer;
    protected IdleStateHandler idleHandler;
    protected ReadTimeoutHandler readTimeoutHandler;

    public MeshPipelineFactory(MeshControllerImpl controller,
                               ThreadPoolExecutor pipelineExecutor,
                               SSLContext sslContext) {
        super();
        this.controller = controller;
        this.pipelineExecutor = pipelineExecutor;
        this.timer = new HashedWheelTimer(groupedThreads("meshpelineFactory", "timer-%d", log));
        this.idleHandler = new IdleStateHandler(timer, 20, 25, 0);
        this.readTimeoutHandler = new ReadTimeoutHandler(timer, 30);
        this.sslContext = sslContext;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        log.trace("getPipeline()");
        MeshChannelHandler handler = new MeshChannelHandler(controller);
        ChannelPipeline pipeline = Channels.pipeline();
        if (sslContext != null) {
            log.trace("Mesh SSL enabled.");
            SSLEngine sslEngine = sslContext.createSSLEngine();

            sslEngine.setNeedClientAuth(true);
            sslEngine.setUseClientMode(false);
            sslEngine.setEnabledProtocols(sslEngine.getSupportedProtocols());
            sslEngine.setEnabledCipherSuites(sslEngine.getSupportedCipherSuites());
            sslEngine.setEnableSessionCreation(true);

            SslHandler sslHandler = new SslHandler(sslEngine);
            pipeline.addLast("ssl", sslHandler);
        } else {
            log.trace("Mesh SSL disabled");
        }
        pipeline.addLast("meshmessagedecoder", new MeshMessageDecoder());
        pipeline.addLast("meshmessageencoder", new MeshMessageEncoder());
        pipeline.addLast("idle", idleHandler);
        pipeline.addLast("timeout", readTimeoutHandler);
        // XXX S ONOS: was 15 increased it to fix Issue #296

        if (pipelineExecutor != null) {
            pipeline.addLast("pipelineExecutor",
                             new ExecutionHandler(pipelineExecutor));
        }
        pipeline.addLast("handler", handler);
        return pipeline;
    }

    @Override
    public void releaseExternalResources() {
        log.info("releaseExternalResources: timer stopped");
        timer.stop();
    }
}
