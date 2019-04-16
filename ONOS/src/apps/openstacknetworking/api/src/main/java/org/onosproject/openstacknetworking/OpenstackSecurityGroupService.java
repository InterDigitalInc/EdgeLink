/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.openstacknetworking;

import org.onosproject.net.Host;

import org.onosproject.openstackinterface.OpenstackPort;

/**
 * Represents OpenstackSecurityGroupService Interface.
 */
public interface OpenstackSecurityGroupService {

    /**
     * Updates the flow rules for Security Group for the VM (OpenstackPort).
     *
     * @param osPort OpenstackPort information for the VM
     */
    void updateSecurityGroup(OpenstackPort osPort);

    /**
     * Handles to purge data plane flow of existing VM.
     *
     * @param host VM Host information
     */
    void purgeVmFlow(Host host);

    /**
     * Handles to reinstall data plane flow of existing VM.
     *
     * @param host VM Host information
     */
    void reinstallVmFlow(Host host);
}
