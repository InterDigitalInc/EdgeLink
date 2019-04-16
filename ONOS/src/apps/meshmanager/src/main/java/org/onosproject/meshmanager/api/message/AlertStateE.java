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

public enum AlertStateE {

    NC_NBI_ALERT_OFF(0),

    NC_NBI_ALERT_ON(1);

    int value;

    private AlertStateE(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
