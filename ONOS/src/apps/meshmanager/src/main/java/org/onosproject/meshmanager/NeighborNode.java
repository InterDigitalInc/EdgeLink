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

package org.onosproject.meshmanager;

import org.onlab.util.HexString;
import org.onosproject.meshmanager.api.message.BufferReport;
import org.onosproject.meshmanager.api.message.LinkReport;

/**
 *
 */
public class NeighborNode {
    private MeshSector ownSector;
    private Long peerSectMac;
    private LinkReport linkReport;
    private BufferReport bufferReport;

    /**
     *
     * @param peerMac
     * @param sector
     */
    public NeighborNode(Long peerMac, MeshSector sector) {
        this.peerSectMac = peerMac;
        this.ownSector = sector;
    }

    /**
     *
     * @param sector
     */
    public void setParentSector(MeshSector sector) {
        ownSector = sector;
    }

    /**
     *
     * @param mac
     */
    public void setPeerSectorMac(long mac) {
        this.peerSectMac = mac;
    }

    /**
     *
     * @return
     */
    public MeshSector getOwnSector() {
        return ownSector;
    }

    /**
     *
     * @return
     */
    public Long getPeerSectMac() {
        return peerSectMac;
    }

    /**
     *
     * @return
     */
    public LinkReport getLinkReport() {
        return linkReport;
    }

    /**
     *
     * @param lr
     */
    public void setLinkReport(LinkReport lr) {
        linkReport = lr;
    }

    /**
     *
     * @return
     */
    public BufferReport getBufferReport() {
        return bufferReport;
    }

    /**
     *
     * @param br
     */
    public void setBufferReport(BufferReport br) {
        bufferReport = br;
    }

    /**
     * Returns a summary of the neighbor data and link associated
     * @return
     */
    public String toString() {
        String str;
        str = "\n\t\t***** NEIGHBOR NODE *****";
        str += "\n\t\tpeerMac=" + HexString.toHexString(peerSectMac);

        // linkReport is null if this node was created from a report indication of a new node
        if (linkReport != null) {
            str += linkReport.toString();
        }
        if (bufferReport != null) {
            str += bufferReport.toString();
        }
        str += "\n";
        return str;
    }
}
