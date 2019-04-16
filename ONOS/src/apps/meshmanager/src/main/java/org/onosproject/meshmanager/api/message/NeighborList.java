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

import org.onlab.util.HexString;
import org.onosproject.meshmanager.NeighborNode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NeighborList {
    private Long ownSectMac;
    private short ownSectPortIndex;
    private byte nbNeighbors;
    private List<NeighborNode> neighbors = new ArrayList<>();

    /**
     *
     * @param mac
     * @param portIndex
     */
    public NeighborList(Long mac, short portIndex) {
        this.ownSectMac = mac;
        this.ownSectPortIndex = portIndex;
    }

    /**
     *
     * @return
     */
    public List<NeighborNode> getNeighbors() {
        return neighbors;
    }

    /**
     *
     * @return
     */
    public Long getOwnSectMac() {
        return ownSectMac;
    }

    /**
     *
     * @return
     */
    public byte getNbNeighbors() {
        return nbNeighbors;
    }

    /**
     *
     * @return
     */
    public short getOwnSectPortIndex() {
        return ownSectPortIndex;
    }

    /**
     *
     * @param node
     */
    public void addNeighbor(NeighborNode node) {
        neighbors.add(node);
        int nbNodes = nbNeighbors + 1;
        nbNeighbors = (byte)nbNodes;
    }

    /**
     *
     * @param node
     */
    public void removeNeighbor(NeighborNode node) {
        neighbors.remove(node);
        int nbNodes = nbNeighbors - 1;
        this.nbNeighbors = (byte)nbNodes;
    }

    /**
     *
     * @param i
     * @return
     */
    public NeighborNode getNeighborNode(int i) {
        return neighbors.get(i);
    }

    /**
     *
     * @return
     */
    public String toString() {
        String string;
        string = "\n\t\t\tmeshNeighborList" +
                 "\n\t\t\townSectMac=" + HexString.toHexString(ownSectMac) +
                ":port " + ownSectPortIndex;
        if (nbNeighbors != 0) {
            string += "\n\t\t\tneighbors=" + nbNeighbors;
            if (this.getNeighbors().isEmpty()) {
                string += "(EMPTY)";
            } else {
                for(NeighborNode node : this.getNeighbors()) {
                    string += "\n" + node.toString();
                }
            }
        } else {
            string += "\n\t\t\tneighbors=NONE" ;
        }
        return string;
    }
}
