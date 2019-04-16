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

import org.slf4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class NbiSession {
    private final Logger logger = getLogger(getClass());

    private CustomerSession customerSession;
    private short sessionId;
    private short sessionDuration;
    private Timer sessionTimer;
    private boolean isActive;

    /**
     *
     * @param newCustomerSession
     * @param newSessionId
     * @param newSessionDuration
     */
    public NbiSession(CustomerSession newCustomerSession, short newSessionId, short newSessionDuration) {
        customerSession = newCustomerSession;
        sessionId = newSessionId;
        sessionDuration = newSessionDuration;
        isActive = false;

        if (newSessionDuration != -1) {
            sessionTimer = new Timer();
            sessionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (isActive()) {
                            logger.trace("SessionTimer expired for session " + sessionId);
                            customerSession.deleteNbiSession(sessionId);
                        } else {
                            setActive(true);
                        }
                    } catch (Exception e) {
                        logger.debug("Caught exception: ", e);
                    }
                }
            }, sessionDuration);
        }
    }

    /**
     *
     */
    public void cancelSessionTimer() {
        sessionTimer.cancel();
        setActive(false);
    }

    /**
     *
     * @return
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     *
     * @param value
     */
    public void setActive(boolean value) {
        isActive = value;
    }

    /**
     *
     * @return
     */
    public short getSessionId() {
        return sessionId;
    }

    /**
     *
     * @return
     */
    public short getSessionDuration() {
        return sessionDuration;
    }

    /**
     *
     * @param duration
     */
    public void setSessionDuration(short duration) {
        sessionDuration = duration;
    }

    /**
     *
     * @return
     */
    public String toString() {
        String myString = " sessionId: " + sessionId + "(" + sessionDuration + ")";
        return myString;
    }

    /**
     *
     * @return
     */
    public String toStringFromList() {
        String myString = sessionId + " - ";
        return myString;
    }

}

