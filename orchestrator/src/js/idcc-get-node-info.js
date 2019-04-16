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

$(document).ready(getNodeInfoInit);

var node_info_input_html =
'<form class="form-horizontal st-inputfields-shiftup">                                                      ' +
'    <div class="form-group">                                                                               ' +
'        <div class="col-sm-offset-4 col-sm-4 col-lg-offset-5 col-lg-2">                                    ' +
'            <button type="submit" id="js-getNodeInfo-btn" class="btn btn-info btn-block" href="#">Get Node Info</button> ' +
'        </div>                                                                                             ' +
'    </div> <!-- end form-group -->                                                                         ' +
'</form> <!-- end form-horizontal -->'; //node_info_input_html

var node_info_output_html = 
'<div class="row st-node-info-tbl" id="js-output-row">                             ' +
'   <table id="js-node-info-tbl" data-toolbar="#js-btn-toolbar" data-search="true" ' +
'       data-pagination="true" data-page-size="10" data-page-list="[10,50,100]"    ' +
'       data-classes="table table-hover table-condensed" data-striped="true">      ' +
'        <thead>                                                                   ' +
'            <tr>                                                                  ' +
'                <th data-formatter="tableIndexGenerator">              S.No.    </th> ' +
'                <th data-field="nodeId" data-sortable="true">          Node ID  </th> ' +
'                <th data-field="poaList">                              PoA      </th> ' +
'                <th data-field="nodeLocation" data-sortable="true" class="text-muted">    Location </th> ' +
'            </tr>                                                                 ' +
'        </thead>                                                                  ' +
'    </table>                                                                      ' +
'</div>'; //node_info_output_html

function getNodeInfoInit() {
    printLog('dbg', 'nodeInfoInit entered');

    //1. initialize
    commonInit();
    $('#st-pg-title').html('Get Node Info');

    //2. make page active
    $('#js-pg-node-info').addClass("active");

    //3. enable page if session is valid
    if (validateSession() == 'err') {
        printLog('err', 'session invalid. Customer registration required!!');
        $('#js-right-corepart').html('<div id="js-http-err">Customer registration required!!</div>');
        return 'err';
    }

    //4. set page content
    $('#js-input-params').html(node_info_input_html);
    $('#js-output-params').html(node_info_output_html);

    //5. enable event handlers
    $('#js-getNodeInfo-btn').on("click", getNodeInfoBtnClick);

    //6. cleanout output fields
    cleanOutResult();

    //7. Create response code map
    getNodeInfoCreateResponseCodeMap();

    //8. custom CSS
    $('#js-responsebar-row').addClass('st-responsebar-shiftup');

    //9. AutoTab functionality for mac address fields
    $.autotab({ tabOnSelect: true });
    $('.st-input-mac').autotab('filter', 'hexadecimal');

    //10. Call RPC on Pg load itself
    try {
        getNodeInfoInvokeRpc();
    }
    catch(e) {
        errTxt = 'RPC exception - ' + e;
        printLog('err', errTxt);
        populateHttpErrorText(errTxt);
    }
} //func getNodeInfoInit

// file-global map containing output response code and corresponding interpretations
var getNodeInfoResponseCodeMap = new Map();
function getNodeInfoCreateResponseCodeMap() {
    printLog('dbg', 'nodeInfoCreateResponseCodeMap entered');
    getNodeInfoResponseCodeMap.set('NC_NBI_OK',                 'Node Info Fetched Successfully');
    getNodeInfoResponseCodeMap.set('NC_NBI_INVALID_SESSION_ID', 'Invalid Session ID');
} //func getNodeInfoCreateResponseCodeMap

function getNodeInfoBtnClick(evnt) {
    printLog('dbg', 'getNodeInfoBtnClick entered');
    cleanOutResult();
    try {
        getNodeInfoInvokeRpc();
    }
    catch(e) {
        errTxt = 'RPC exception - ' + e;
        printLog('err', errTxt);
        populateHttpErrorText(errTxt);
    }
    evnt.preventDefault(); //avoid page reload
} //getNodeInfoBtnClick

function getNodeInfoInvokeRpc() {
    printLog('dbg', 'nodeInfoInvokeRpc entered');
    var getNodeInfoRequestBody = {
        "input" : {
            "sessionId" : localStorage.getItem("session_id")
        } //input
    }; //getNodeInfoRequestBody

    if (isUtMode() == true) {
        ut_node_info_rpcStub();
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlgetNodeInfo"), getNodeInfoRequestBody, getNodeInfoParseResponseCallback);
    }
} //func getNodeInfoInvokeRpc

function getNodeInfoParseResponseCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'nodeInfoParseResponseCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ',        JSON.stringify(textStatus));

    getNodeInfoPopulateOutput(data);
} //func getNodeInfoParseResponseCallback

function getNodeInfoPopulateOutput(response) {
    printLog('dbg', 'nodeInfoPopulateOutput entered');

    try{
        response_code   = response.output.returnCode;
        result_text     = getNodeInfoResponseCodeMap.get(response_code);
        printLog('info', 'response_code='+response_code, ', result_text='+result_text);
        $('#js-response-code').html(response_code);

        if (response_code == 'NC_NBI_OK') {
            getNodeInfoPopulateOutputTable(response.output);
            populateResultText(result_text);
        }
        else {
            if (! result_text) {
                result_text = response_code;
            }
            populateHttpErrorText(result_text);
        }
    }
    catch(e) {
        error_string = 'Parse error in response:<br/>' + JSON.stringify(response) + '<br/>' + e;
        printLog('err', 'exception: ', error_string);
        populateHttpErrorText(error_string);
    }
} //func getNodeInfoPopulateOutput

function getNodeInfoPopulateOutputTable(output) {
    // printLog('dbg', 'nodeInfoPopulateOutputTable entered', 'output='+JSON.stringify(output));

    //1. process data - convert mac
    output.nodeInfoList = getNodeInfoProcessDataForDisplay(output.nodeInfoList);

    //2. populate data into table
    $('#js-node-info-tbl').bootstrapTable('destroy');
    $('#js-node-info-tbl').bootstrapTable({data: output.nodeInfoList});

    //3. process empty cells
    $("#js-node-info-tbl td:empty").html('--');
} //func getNodeInfoPopulateOutputTable

function getNodeInfoProcessDataForDisplay(unprocessedData) {
    printLog('dbg', "nodeInfoProcessDataForDisplay entered");
    nonGwNodeMacStringList = [];
    // remove NodeID mac strings from orchestrator memory
    nodeIdClearStorage();

    //convert each MAC from long to hex
    processedData = [];
    unprocessedData.forEach(function(eachNode) {
        dataString = JSON.stringify(eachNode);

        // nodeId
        decoratedMac = convertLongToMacString(eachNode.nodeId);
        nonGwNodeMacStringList.push(decoratedMac); //for use in nodeID dropdowns
        dataString = dataString.replace(/(\"nodeId\"\s*:\s*)(\d*)/, '$1'+decoratedMac);

        // poaList
        macPoaList = [];
        isFirstValue = "yes";
        eachNode.poaList.forEach(function(eachPoa) {
            if (isFirstValue == "yes") { //dont prepend with spaces
                macPoaList.push(convertLongToMacString(eachPoa));
                isFirstValue = "not-now";
            }
            else { //spaces after every comma
                macPoaList.push(convertLongToMacString(eachPoa, "yes"));
            } //if-else eachPoa
        });
        dataString = dataString.replace(/(\"poaList\"\s*:\s*\[)([\d,]*)(\])/, '$1'+macPoaList+'$3');

        //mark unused location info
        dataString = dataString.replace(/latitude=[\d\S]*,/,  "latitude=N/A,");
        dataString = dataString.replace(/longitude=[\d\S]*,/, "longitude=N/A,");
        dataString = dataString.replace(/altitude=[\d.\d]*/,  "altitude=N/A");

        processedData.push(JSON.parse(dataString));
    }); //forEach
    // printLog('dbg', "processedData="+JSON.stringify(processedData));

    localStorage.setItem("nonGwNodeMacStringList", nonGwNodeMacStringList); //will be used on other pages

    return processedData;
} //func getNodeInfoProcessDataForDisplay

function ut_node_info_rpcStub() {
    printLog('dbg', 'ut_node_info_rpcStub entered');

    var response_nodeInfo = {
        "output": {
            "returnCode": "NC_NBI_OK",
            "numNodes" : 2,
            "nodeInfoList" : [
            {
                "nodeId" : 8963798073610,
                "nodeLocation" : "latitude=39.780608, longitude=-75.494902, altitude=33.33",
                "numPoa" : 2,
                "poaList"  : [8963798073867, 8963798073868]
            },
            {
                "nodeId" : 8963798073710,
                "nodeLocation" : "latitude=0, longitude=0, altitude=0",
                "numPoa" : 0,
                "poaList"  : []
            }]
        }}; //response_nodeInfo

    getNodeInfoPopulateOutput(response_nodeInfo);

    $('#js-response-time').html('9999 msec');
} //func ut_node_info_rpcStub
