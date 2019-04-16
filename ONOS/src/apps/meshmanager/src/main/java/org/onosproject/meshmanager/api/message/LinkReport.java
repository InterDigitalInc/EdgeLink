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
public class LinkReport {
    static private byte MAX_MCS = 12;
    private byte linkMcs;

    protected static final short DEFAULT_LINK_REPORT_PERIODICITY = 1;
    protected static final byte DEFAULT_LINK_REPORT_THRESHOLD = 10;

    /**
     *
     */
    public LinkReport() {
    }

    /**
     *
     * @return
     */
    public byte getLinkMcs() {
        return linkMcs;
    }

    /**
     *
     * @param mcs
     */
    public void setLinkMcs(byte mcs) {
        linkMcs = mcs;
    }

    /**
     * Returns a summary of the linkReport
     */
    public String toString() {
        return "\n\t\tlinkReport\n\t\tlinkMcs=" + linkMcs;
    }
}

