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

public class MeshNBICustomerRegistrationReq extends MeshNBIRequest {

    private Long customerId;
    private String password;
    private Byte apiVersion;


    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        if (customerId != null && customerId < 0L) {
            throw new IllegalArgumentException("CustomerRegistrationInput: customerId should be non-negative.");
        }
        this.customerId = customerId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null && password.length() > 20) {
            throw new IllegalArgumentException("CustomerRegistrationInput: Password length should be max 20 chars");
        }
        this.password = password;
    }

    public Byte getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(Byte apiVersion) {
        if (apiVersion != null && apiVersion < 0) {
            throw new IllegalArgumentException("CustomerRegistrationInput: apiVersion should be non-negative.");
        }
        this.apiVersion = apiVersion;
    }

    @Override
    public String toString() {
        return "CustomerRegistrationInput [customerId=" + customerId + ", password=" + password + ", apiVersion="
                + apiVersion + "]";
    }

    @Override
    public boolean validate() {
        if (customerId == null || password == null || apiVersion == null) {
            return false;
        }
        return true;
    }
}
