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
package org.onosproject.provider.mesh;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.mesh.controller.MeshController;
import org.onosproject.mesh.controller.MeshDpid;
import org.onosproject.mesh.controller.MeshListener;
import org.onosproject.mesh.controller.MeshSwitch;
import org.onosproject.meshmanager.api.MeshProvider;
import org.onosproject.meshmanager.api.MeshProviderRegistry;
import org.onosproject.meshmanager.api.MeshProviderService;
import org.onosproject.meshmanager.api.message.MeshSBIMessage;
import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import static org.onosproject.mesh.controller.MeshDpid.meshDpid;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Provider which uses an Mesh controller to detect network end-station hosts.
 */
@Component(immediate = true)
public class MeshProviderImpl extends AbstractProvider implements MeshProvider {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MeshProviderRegistry meshProviderRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MeshController meshController;

    private MeshProviderService meshProviderService;

    private final MeshListenerImpl meshListener = new MeshListenerImpl();

    /**
     * Creates an Mesh host provider.
     */
    public MeshProviderImpl() {
        super(new ProviderId("mesh", "org.onosproject.provider.mesh"));
    }

    @Activate
    protected void activate(ComponentContext context) {
        log.info("activate()");
        //cfgService.registerProperties(getClass());
        meshProviderService = meshProviderRegistry.register(this);
        meshController.addListener(meshListener);
    }


    @Deactivate
    protected void deactivate(ComponentContext context) {
        log.info("deactivate()");
        //cfgService.unregisterProperties(getClass(), false);
        meshListener.disable();
        meshController.removeListener(meshListener);
        meshProviderRegistry.unregister(this);
        meshProviderService = null;
    }

    @Override
    public void disconnectDevice(DeviceId id) {
        log.info("disconnectDevice({})", id);
        MeshDpid dpid = meshDpid(id.uri());
        log.debug("MeshDpid[{}] MeshController[{}]", dpid, meshController);
        MeshSwitch sw = meshController.getSwitch(dpid);
        if (sw != null) {
            sw.disconnect();
        }
    }

    @Override
    public void sendMessage(DeviceId id, MeshSBIMessage msg) {
        log.trace("sendMessage({},{})", id, msg);
        MeshDpid dpid = meshDpid(id.uri());
        log.debug("MeshDpid[{}] MeshController[{}]", dpid, meshController);
        MeshSwitch sw = meshController.getSwitch(dpid);
        if (sw != null) {
            sw.sendMsg(msg);
        }
    }

    /**
     * Mesh Controller listener implementation
     */
    private class MeshListenerImpl implements MeshListener {

        private boolean isDisabled = false;

        @Override
        public void switchAdded(MeshDpid dpid) {
            log.info("switchAdded({})", dpid);
            if (meshProviderService == null || isDisabled) {
                return;
            }
            meshProviderService.deviceConnected(
                    DeviceId.deviceId(MeshDpid.uri(dpid)));
        }

        @Override
        public void switchRemoved(MeshDpid dpid) {
            log.info("switchRemoved({})", dpid);
            if (meshProviderService == null || isDisabled) {
                return;
            }
            meshProviderService.deviceDisconnected(
                    DeviceId.deviceId(MeshDpid.uri(dpid)));
        }

        @Override
        public void handleMessage(MeshDpid dpid, MeshSBIMessage msg) {
            log.trace("handleMessage({},{})", dpid, msg);
            if (meshProviderService == null || isDisabled) {
                return;
            }
            meshProviderService.handleMessage(
                    DeviceId.deviceId(MeshDpid.uri(dpid)), msg);
        }

        private void disable() {
            isDisabled = true;
        }
    }
}
