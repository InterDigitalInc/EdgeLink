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
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class MeshLink {
    private final Logger logger = getLogger(getClass());
    private long ownMac;
    private long peerMac;
    private byte[] mcs = new byte[2];
    private List<HashMap<Short, Integer>> primReqSlaMap = new ArrayList<>();
    private List<HashMap<Short, Integer>> altReqSlaMap = new ArrayList<>();

    /**
     * Constructor
     * @param peerMac
     * @param ownMac
     */
    public MeshLink(long peerMac, long ownMac) {
        this.peerMac = peerMac;
        this.ownMac = ownMac;
        this.mcs[MeshRoutingManager.STA_TO_PCP] = 0;
        this.mcs[MeshRoutingManager.PCP_TO_STA] = 0;

        // Add 2 HashMaps for primary and backup Sla (1 per direction)
        primReqSlaMap.add(new HashMap<>());
        primReqSlaMap.add(new HashMap<>());
        altReqSlaMap.add(new HashMap<>());
        altReqSlaMap.add(new HashMap<>());
    }

    /**
     *
     * @return
     */
    public long getPeerMac() {
        return peerMac;
    }

    /**
     *
     * @return
     */
    public long getOwnMac() {
        return ownMac;
    }

    /**
     *
     * @param dir
     * @return
     */
    public byte getMCS(byte dir) {
        return mcs[dir];
    }

    /**
     *
     * @param dir
     * @return
     */
    public HashMap<Short, Integer> getPrimReqSlaMap(byte dir) {
        return primReqSlaMap.get(dir);
    }

    /**
     *
     * @param dir
     * @return
     */
    public HashMap<Short, Integer> getAltReqSlaMap(byte dir) {
        return altReqSlaMap.get(dir);
    }

    /**
     *
     * @param mcs
     * @param dir
     */
    public void updateMCS(byte mcs, byte dir) {
        this.mcs[dir] = mcs;
    }

    /**
     *
     * @param vlanId
     * @param newReqSla
     * @param dir
     * @param isPrimPath
     * @return
     */
    public boolean addReqSla(short vlanId, int newReqSla, byte dir, boolean isPrimPath) {
        logger.trace("addReqSla: vlanId[" + vlanId + "] newReqSla(BW)[" + newReqSla +
                             "] direction[" + dir + "] isPrimPath[" + isPrimPath + "]");

        // Add req Sla based on direction & primary vs. alternate path
        if (isPrimPath) {
            if (primReqSlaMap.get(dir).containsKey(vlanId)) {
                logger.trace("primReqSlaMap[{}] already contains vlanId", dir);
                return false;
            }
            primReqSlaMap.get(dir).put(vlanId, newReqSla);
            logger.trace("Added [{}] to primReqSlaMap[{}]", newReqSla, dir);
        } else {
            if (altReqSlaMap.get(dir).containsKey(vlanId)) {
                logger.trace("altReqSlaMap[{}] already contains vlanId", dir);
                return false;
            }
            altReqSlaMap.get(dir).put(vlanId, newReqSla);
            logger.trace("Added [{}] to altReqSlaMap[{}]", newReqSla, dir);
        }
        return true;
    }

    /**
     *
     * @param vlanId
     * @param dir
     * @param isPrimPath
     * @return
     */
    public boolean removeReqSla(short vlanId, short dir, boolean isPrimPath) {
        Integer val;
        logger.trace("removeReqSla: vlanId=" + vlanId + " direction=" + dir +
                             " isPrimPath=" + isPrimPath);

        // Remove req Sla from MAC based on direction & primary vs. backup path
        if (isPrimPath) {
            val = primReqSlaMap.get(dir).remove(vlanId);
            if (val == null) {
                logger.trace("primReqSlaMap[{}] does not contain vlanId", dir);
                return false;
            }
            logger.trace("Removed {} from primReqSlaMap[{}]", val, dir);
        } else {
            val = altReqSlaMap.get(dir).remove(vlanId);
            if (val == null) {
                logger.trace("altReqSlaMap[{}] does not contain vlanId", dir);
                return false;
            }
            logger.trace("Removed {} from altReqSlaMap[{}]", val, dir);
        }
        return true;
    }

    /**
     * Returns a summary of the sector data
     */
    public String toString() {
        String reqSlaString = "";

        for (HashMap<Short, Integer> map : primReqSlaMap) {
            reqSlaString += "\n\t\tprimReqSlaMap[" + getDirString((byte)primReqSlaMap.indexOf(map)) + "]:";
            for (Entry<Short, Integer> entry : map.entrySet()) {
                reqSlaString += ("\n\t\t\tvlanId=" + entry.getKey() + " reqSla=" + entry.getValue());
            }
        }
        for (HashMap<Short, Integer> map : altReqSlaMap) {
            reqSlaString += "\n\t\taltReqSlaMap[" + getDirString((byte)altReqSlaMap.indexOf(map)) + "]:";
            for (Entry<Short, Integer> entry : map.entrySet()) {
                reqSlaString += ("\n\t\t\tvlanId=" + entry.getKey() + " reqSla=" + entry.getValue());
            }
        }

        return "\n\t\t***** MESH LINK *****" +
                "\n\t\tpeerMac=" + HexString.toHexString(peerMac) +
                "\n\t\townMac=" + HexString.toHexString(ownMac) +
                reqSlaString +
                "\n\t\tmcs[" + getDirString(MeshRoutingManager.STA_TO_PCP) + "]=" +
                mcs[MeshRoutingManager.STA_TO_PCP] +
                "\n\t\tmcs[" + getDirString(MeshRoutingManager.PCP_TO_STA) + "]=" +
                mcs[MeshRoutingManager.PCP_TO_STA] + "\n";
    }

    /**
     *
     * @param dir
     * @return
     */
    private String getDirString(byte dir) {
        String string;
        switch(dir) {
            case MeshRoutingManager.STA_TO_PCP:
                string = "STA_TO_PCP";
                break;
            case MeshRoutingManager.PCP_TO_STA:
                string = "PCP_TO_STA";
                break;
            default:
                throw new AssertionError("Unsupported dir: " + dir);
        }
        return string;
    }
}

