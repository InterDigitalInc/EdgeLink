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
package org.onosproject.meshcli;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.openflow.controller.OpenFlowController;
import org.onosproject.openflow.controller.OpenFlowSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class AppComponent {

    private final Logger log = LoggerFactory.getLogger(getClass());
    static final String MESH_APP_ID = "org.onosproject.mesh";
    private ApplicationId appId;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenFlowController controller;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(MESH_APP_ID);
        if(controller != null) {
            log.info("controller not null");

            for (OpenFlowSwitch sw : controller.getSwitches()) {
                log.info(sw.toString());
            }

            Iterable<Device> devices = deviceService.getAvailableDevices();
            for(Device device : devices) {
                log.info("DEVICE : " + device.toString());
            } 
        }
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
 
        Iterable<Device> devices = deviceService.getAvailableDevices();
        for(Device device : devices) {
            log.info("DEVICE : " + device.toString());
//            sendDefaultSelfFlow(device.id());
        }
   }

    public void sendDefaultSelfFlow(/*MeshNode meshNode, OpenFlowSwitch switchHndlr*/ DeviceId deviceId) {
        log.trace("sendDefaultSelfFlow from UDP handler");

        //OFFlowAdd.Builder flowMod = OFFactories.getFactory(OFVersion.OF_10).buildFlowAdd();
        //OFFlowAdd.Builder fm = switchHndlr.factory().buildFlowAdd();

        //Match match;

        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder()
            .matchEthDst(MacAddress.valueOf("11:22:33:44:55:66"));

        TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder()
            .setEthDst(MacAddress.valueOf("11:22:33:44:55:66"))
            .setOutput(PortNumber.NORMAL);

            
        FlowRule flowRule = DefaultFlowRule.builder()
 //           .fromApp(appId)
            .forDevice(deviceId)
            .withSelector(selectorBuilder.build())
            .withTreatment(treatmentBuilder.build())
            .withPriority(12000)
            .withCookie(1)
            .makePermanent()
            .build();

        log.info("FlowRule: " + flowRule.toString());
        flowRuleService.applyFlowRules(flowRule);
    }

}
