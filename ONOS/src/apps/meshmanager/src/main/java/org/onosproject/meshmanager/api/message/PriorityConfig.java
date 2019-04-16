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

package org.onosproject.meshmanager.api.message;

/**
 *
 */
public class PriorityConfig {
    private Byte priority;

    /**
     *
     * @return
     */
    public Byte getPriority() {
        return priority;
    }

    /**
     *
     * @param priority
     */
    public void setPriority(Byte priority) {
        if (priority != null && (priority < 0) || (priority > 7)) {
            throw new IllegalArgumentException("PriorityConfig: priority should be 0 to 7.");
        }
        this.priority = priority;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "PriorityConfig [priority=" + priority + "]";
    }
}
