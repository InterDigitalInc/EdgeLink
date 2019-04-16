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
import org.onosproject.meshmanager.api.message.MeshConstants;
import org.slf4j.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class CustomerSession {
    private final Logger logger = getLogger(getClass());

    private MeshManager meshManager;
    private List<NbiSession> nbiSessionsList = new ArrayList<>();
    private long customerId;
    private long sessionDuration;

    // An access node has a 1-1 mapping to a slice, so the accessNode object represents the slice
    private ConcurrentHashMap<Short, AccessNode> sliceMap;
    private ConcurrentHashMap<Short, Timestamp> sliceStartTimeStampMap;

    // Alerts are kept in a list until a request is made to get the alerts...
    // only the last few alerts are kept to limit the size of the list

    // List of alerts raised/cleared between getAlerts from the Orchestrator
    private List<MeshAlert> alertList = new ArrayList<>();
    // true if an alert needs to be generated (i.e. the orchestrator will be notified of that change)
    private boolean alertUpdateNeeded;
    // true if a stat update needs to be generated (i.e. the orchestrator will be notified of that change)
    private boolean statsUpdateNeeded;

    /**
     * Constructor
     * @param meshManager
     * @param customerId
     */
    public CustomerSession(MeshManager meshManager, long customerId) {
        this.meshManager = meshManager;
        this.customerId = customerId;
        this.sliceMap = new ConcurrentHashMap<>();
        this.sliceStartTimeStampMap = new ConcurrentHashMap<>();
        this.alertUpdateNeeded = false;
        this.statsUpdateNeeded = false;
    }

    /**
     *
     * @return
     */
    public List<NbiSession> getNbiSessionsList() {
        return nbiSessionsList;
    }

    /**
     *
     * @param sessionId
     * @param sessionDuration
     * @return
     */
    public boolean createNewNbiSession(short sessionId, short sessionDuration) {
        NbiSession newNbiSession = new NbiSession(
                this, sessionId, sessionDuration);
        nbiSessionsList.add(newNbiSession);
        return true;
    }

    /**
     *
     * @param sessionId
     * @return
     */
    public boolean deleteNbiSession(short sessionId) {
        NbiSession nbiSessionToDelete = null;
        for (NbiSession nbiSession : nbiSessionsList) {
            if (nbiSession.getSessionId() == sessionId) {
                nbiSessionToDelete = nbiSession;
                break;
            }
        }
        if (nbiSessionToDelete != null) {
            nbiSessionToDelete.cancelSessionTimer();
            nbiSessionsList.remove(nbiSessionToDelete);
            // Update the mapping
            meshManager.removeNbiSession(nbiSessionToDelete.getSessionId());
            return true;
        }
        return false;
    }

    /**
     *
     * @param nbiSessionToDelete
     * @return
     */
    public boolean deleteNbiSession(NbiSession nbiSessionToDelete) {
        return nbiSessionsList.remove(nbiSessionToDelete);
    }

    /**
     *
     * @param nbiSessionToDelete
     * @return
     */
    public void terminateCustomerSession() {
        sliceMap.clear();
        sliceStartTimeStampMap.clear();

        for (NbiSession nbiSession : nbiSessionsList) {
            nbiSession.cancelSessionTimer();
            //nbiSessionsList.remove(nbiSession);
            // Update the mapping
            meshManager.removeNbiSession(nbiSession.getSessionId());
        }
        nbiSessionsList.clear();
 
    }

    /**
     *
     * @return
     */
    public long getCustomerId() {
        return customerId;
    }

    /**
     *
     * @return
     */
    public long getSessionDuration() {
        return sessionDuration;
    }

    /**
     *
     * @param duration
     */
    public void setSessionDuration(long duration) {
        sessionDuration = duration;
    }

    /**
     *
     * @return
     */
    public boolean getAlertUpdate() {
        return alertUpdateNeeded;
    }

    /**
     *
     * @return
     */
    public boolean setAlertUpdate() {
        if (alertUpdateNeeded == true) {
            return false;
        }
        alertUpdateNeeded = true;
        return true;
    }

    /**
     *
     */
    public void clearAlertUpdate() {
        alertUpdateNeeded = false;
    }

    /**
     *
     * @param alert
     */
    public void addAlert(MeshAlert alert) {
        alertList.add(alert);
    }

    /**
     *
     * @return
     */
    public List<MeshAlert> getAlerts() {
        return alertList;
    }

    /**
     *
     */
    public void clearAlerts() {
        alertList.clear();
    }

    /**
     *
     * @return
     */
    public boolean getStatsUpdate() {
        return statsUpdateNeeded;
    }

    /**
     *
     * @return
     */
    public boolean setStatsUpdate() {
        if (statsUpdateNeeded) {
            return false;
        }
        statsUpdateNeeded = true;
        return true;
    }

    /**
     *
     */
    public void clearStatsUpdate() {
        statsUpdateNeeded = false;
    }

    /**
     *
     * @param accessNode
     */
    public void addSlice(AccessNode accessNode) {
        sliceMap.put(accessNode.getId(), accessNode);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        sliceStartTimeStampMap.put(accessNode.getId(), timestamp);
    }

    /**
     *
     * @param accessNode
     */
    public void removeSlice(AccessNode accessNode) {
        sliceMap.remove(accessNode.getId());
    }

    /**
     *
     * @param sliceId
     */
    public void removeSlice(short sliceId) {
        sliceMap.remove(sliceId);
        sliceStartTimeStampMap.remove(sliceId);
    }

    /**
     *
     * @param sliceId
     * @return
     */
    public AccessNode getSlice(short sliceId) {
        return sliceMap.get(sliceId);
    }

    /**
     *
     * @param sliceId
     * @return
     */
    public Timestamp getSliceTimeStamp(short sliceId) {
        return sliceStartTimeStampMap.get(sliceId);
    }

    /**
     *
     * @return
     */
    public List<AccessNode> getAllSlices() {
        List<AccessNode> accessNodeList = new ArrayList<>();
        for (Entry<Short, AccessNode> entry : sliceMap.entrySet()) {
            accessNodeList.add(entry.getValue());
        }
        return accessNodeList;
    }

    /**
     *
     * @return
     */
    public List<Short> getAllSliceIds() {
        List<Short> sliceIdList = new ArrayList<>();
        for (Entry<Short, AccessNode> entry : sliceMap.entrySet()){
            sliceIdList.add(entry.getKey());
        }
        return sliceIdList;
    }

    /**
     *
     * @param debugLevel
     * @return
     */
    public String toString(byte debugLevel) {
        String myString = " ";

        switch(debugLevel) {
            case MeshConstants.TRACE:
            case MeshConstants.DEBUG:
                printSliceMap();
                printMeshAlertList();
                printSliceStartTimeStampMap();
                break;
            case MeshConstants.INFO:
            case MeshConstants.WARNING:
            case MeshConstants.ERROR:
                printCondensedSliceMap();
                break;
            default:
                break;
        }
        return myString;
    }

    /**
     *
     * @return
     */
    public String printSessionIds() {
        String myString = "";
        if (nbiSessionsList.size() > 0) {
            for (NbiSession nbiSession : nbiSessionsList) {
                myString += nbiSession.toStringFromList();
            }
        } else {
            myString += "none";
        }
        return myString;
    }

    /**
     *
     * @return
     */
    public String printCondensedSliceMap() {
        String myString = "Slice Map {content}\n";
        for (Entry<Short, AccessNode> entry : sliceMap.entrySet()) {
            myString+="sliceId : " + entry.getKey() + " accessNodeNodeMac : " +
                    HexString.toHexString(entry.getValue().getOwnMac()) + "\n";
        }
        return myString;
    }

    /**
     *
     * @return
     */
    public String printSliceMap() {
        String myString = "Slice Map {content}\n";
        for (Entry<Short, AccessNode> entry : sliceMap.entrySet()) {
            myString+="sliceId : " + entry.getKey() + "\n" +
                    (entry.getValue().toString()) + "\n";
        }
        return myString;
    }

    /**
     *
     * @return
     */
    public String printMeshAlertList() {
        String myString = "Alerts {content}\n";
        for (MeshAlert alert : alertList) {
            myString += alert.toString();
            myString += "\n";
        }
        return myString;
    }

    /**
     *
     * @return
     */
    public String printSliceStartTimeStampMap() {
        String myString = "Slice StartTimeStamp Map {content}\n";
        for (Entry<Short, Timestamp> entry : sliceStartTimeStampMap.entrySet()) {
            myString+="sliceId : " + entry.getKey() + "\n" +
                    (entry.getValue().toString()) + "\n";
        }
        return myString;
    }



}

