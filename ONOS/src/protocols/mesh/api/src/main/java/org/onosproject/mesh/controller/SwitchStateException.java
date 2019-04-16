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

package org.onosproject.mesh.controller;

/**
 * This exception indicates an error or unexpected message during
 * message handling. E.g., if an OFMessage is received that is illegal or
 * unexpected given the current handshake state.
 *
 * We don't allow wrapping other exception in a switch state exception. We
 * only log the SwitchStateExceptions message so the causing exceptions
 * stack trace is generally not available.
 *
 */
public class SwitchStateException extends Exception {

    private static final long serialVersionUID = 9153954512470002631L;

    public SwitchStateException() {
        super();
    }

    public SwitchStateException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public SwitchStateException(String arg0) {
        super(arg0);
    }

    public SwitchStateException(Throwable arg0) {
        super(arg0);
    }

}
