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

import java.util.ArrayList;
import java.util.List;

public class MeshNBIGetNodeInfoRsp extends MeshNBIResponse {

    private ResultCodeE returnCode;
    private Short numNodes;
    List<NodeInfo> nodeInfoList = new ArrayList<>();

    public Short getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(Short numNodes) {
        this.numNodes = numNodes;
    }

    public List<NodeInfo> getNodeInfoList() {
        return nodeInfoList;
    }

    public void setNodeInfoList(List<NodeInfo> nodeInfoList) {
        this.nodeInfoList = nodeInfoList;
    }

    public ResultCodeE getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(ResultCodeE returnCode) {
        this.returnCode = returnCode;
    }

    public MeshNBIGetNodeInfoRsp(ResultCodeE returnCode, Short numNodes,
                              List<NodeInfo> nodeInfoList) {
        this.returnCode = returnCode;
        this.numNodes = numNodes;
        this.nodeInfoList = nodeInfoList;
    }

    @Override
    public String toString() {
        String string;
        string = "GetNodeInfoOutput {\n\treturnCode : " + returnCode + "\n\tnumNodes : "
                + numNodes +  "\n\tnodeInfoList :[";
        for (NodeInfo nodeInfo : this.getNodeInfoList())
        {
            string +=  nodeInfo.toString();
        }
        string += "\n\t] \n\t}";
        return string;
    }
}
