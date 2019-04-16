/*
 * Copyright 2017-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.driver.pipeline;

import org.onosproject.net.behaviour.PipelinerContext;
/**
 * Driver for software switch emulation of the OFDPA pipeline.
 * The software switch is the OVS OF 1.3 switch. Unfortunately the OVS switch
 * does not handle vlan tags and mpls labels simultaneously, which requires us
 * to do some workarounds in the driver. This driver is meant for the use of
 * the cpqd switch when MPLS is required. As a result this driver works only
 * on incoming untagged packets.
 */
public class OvsOfdpa2Pipeline extends CpqdOfdpa2Pipeline {
    @Override
    protected void initDriverId() {
        driverId = coreService.registerApplication(
                "org.onosproject.driver.OvsOfdpa2Pipeline");
    }

    @Override
    protected void initGroupHander(PipelinerContext context) {
        groupHandler = new OvsOfdpa2GroupHandler();
        groupHandler.init(deviceId, context);
    }

    @Override
    protected boolean supportCopyTtl() {
        return false;
    }
}
