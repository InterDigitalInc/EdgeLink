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

import com.google.common.base.Strings;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.onlab.util.HexString;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.CoreService;
import org.onosproject.mesh.controller.MeshController;
import org.onosproject.mesh.controller.MeshDpid;
import org.onosproject.mesh.controller.MeshListener;
import org.onosproject.mesh.controller.MeshSwitch;
import org.onosproject.meshmanager.api.message.MeshSBIMessage;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.get;
import static org.onlab.util.Tools.groupedThreads;


@Component(immediate = true)
@Service
public class MeshControllerImpl implements MeshController {
    private static final Logger log =
            LoggerFactory.getLogger(MeshControllerImpl.class);
    private static final String APP_ID = "org.onosproject.mesh";
    private static final String DEFAULT_MESHPORT = "6634";
    private static final int DEFAULT_WORKER_THREADS = 0;
    private static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService cfgService;

    @Property(name = "meshPort", value = DEFAULT_MESHPORT,
            label = "Port number used by Mesh protocol; default is 6634")
    private String meshPort = DEFAULT_MESHPORT;

    @Property(name = "workerThreads", intValue = DEFAULT_WORKER_THREADS,
            label = "Number of controller worker threads")
    private int workerThreads = DEFAULT_WORKER_THREADS;

    private NioServerSocketChannelFactory execFactory;
    private ChannelGroup cg;
    private MeshSwitchAgent agent = new MeshSwitchAgentImpl();
    private List<InternalMeshSwitch> connectedSwitches = new ArrayList<>();
    private Set<MeshListener> meshSwitchListener = new CopyOnWriteArraySet<>();


    @Activate
    public void activate(ComponentContext context) {
        log.trace("activate()");
        coreService.registerApplication(APP_ID, this::cleanup);
        cfgService.registerProperties(getClass());
        setConfigParams(context.getProperties());
        start();
    }

    @Deactivate
    public void deactivate() {
        log.trace("deactivate()");
        cleanup();
        cfgService.unregisterProperties(getClass(), false);
    }

    @Modified
    public void modified(ComponentContext context) {
        log.trace("modified()");
        stop();
        setConfigParams(context.getProperties());
        start();
    }

    @Override
    public MeshSwitch getSwitch(MeshDpid dpid) {
        log.trace("getSwitch()");
        for (InternalMeshSwitch sw : connectedSwitches) {
            if (dpid.equals(sw.getDpid())) {
                return sw;
            }
        }
        return null;
    }

    @Override
    public void addListener(MeshListener listener) {
        log.trace("addListener()");
        if (!meshSwitchListener.contains(listener)) {
            this.meshSwitchListener.add(listener);
        }
    }

    @Override
    public void removeListener(MeshListener listener) {
        log.trace("removeListener()");
        this.meshSwitchListener.remove(listener);
    }

    protected InternalMeshSwitch getMeshSwitchInstance(long dpid, Channel channel) {
        log.info("Getting a mesh switch instance for dpid: {}",
                 HexString.toHexString(dpid));
        MeshDpid meshDpid = new MeshDpid(dpid);
        return new MeshSwitchImpl(meshDpid, channel, agent);
    }

    private void start() {
        log.info("start()");
        this.run();
    }

    private void stop() {
        log.info("stop()");
        cg.close();
        execFactory.shutdown();
    }

    private void cleanup() {
        // Close listening channel and all MESH channels.
        // Clean information about switches before deactivating
        log.trace("cleanup()");
        stop();
        connectedSwitches.forEach(MeshSwitch::disconnect);
        connectedSwitches.clear();
    }

    private void run() {
        log.trace("run()");
        try {
            final ServerBootstrap bootstrap = createServerBootStrap();

            bootstrap.setOption("reuseAddr", true);
            bootstrap.setOption("child.keepAlive", true);
            bootstrap.setOption("child.tcpNoDelay", true);
            bootstrap.setOption("child.sendBufferSize", MeshControllerImpl.SEND_BUFFER_SIZE);

            ChannelPipelineFactory pfact = new MeshPipelineFactory(
                    this, null, null);
            bootstrap.setPipelineFactory(pfact);
            cg = new DefaultChannelGroup();

            InetSocketAddress sa = new InetSocketAddress(Integer.parseInt(meshPort));
            cg.add(bootstrap.bind(sa));
            log.info("Listening for switch connections on {}", sa);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ServerBootstrap createServerBootStrap() {
        log.trace("createServerBootStrap()");
        if (workerThreads == 0) {
            execFactory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/app", "boss-%d", log)),
                    Executors.newCachedThreadPool(groupedThreads("onos/app", "worker-%d", log)));
            return new ServerBootstrap(execFactory);
        } else {
            execFactory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/app", "boss-%d", log)),
                    Executors.newCachedThreadPool(groupedThreads("onos/app", "worker-%d", log)), workerThreads);
            return new ServerBootstrap(execFactory);
        }
    }

    private void setConfigParams(Dictionary<?, ?> properties) {
        log.trace("setConfigParams()");
        String port = get(properties, "meshPort");
        if (!Strings.isNullOrEmpty(port)) {
            meshPort = port;
        }
        log.info("Mesh port set to {}", meshPort);

        String threads = get(properties, "workerThreads");
        if (!Strings.isNullOrEmpty(threads)) {
            workerThreads = Integer.parseInt(threads);
        }
        log.info("Number of worker threads set to {}", workerThreads);
    }

    /**
     * Implementation of an MeshSwitch Agent which is responsible for
     * keeping track of connected switches and the state in which they are.
     */
    private class MeshSwitchAgentImpl implements MeshSwitchAgent {
        private final Logger log = LoggerFactory.getLogger(MeshSwitchAgentImpl.class);

        @Override
        public boolean addSwitch(InternalMeshSwitch sw) {
            log.info("addSwitch({})", sw.getDpid());
            if (connectedSwitches.contains(sw) == false) {
                connectedSwitches.add(sw);
                for (MeshListener l : meshSwitchListener) {
                    l.switchAdded(sw.getDpid());
                }
                return true;
            } else {
                log.error("Switch already exists");
                return false;
            }
        }

        @Override
        public void removeSwitch(InternalMeshSwitch sw) {
            log.info("removeSwitch({})", sw.getDpid());
            if (connectedSwitches.remove(sw) == true) {
                if (sw.isConnected()) {
                    for (MeshListener l : meshSwitchListener) {
                        l.switchRemoved(sw.getDpid());
                    }
                }
            }
        }

        @Override
        public void processMessage(InternalMeshSwitch sw, MeshSBIMessage msg) {
            log.trace("processMessage({}) with dpid{}", msg, sw.getDpid());
            for (MeshListener l : meshSwitchListener) {
                l.handleMessage(sw.getDpid(), msg);
            }
        }
    }

}

