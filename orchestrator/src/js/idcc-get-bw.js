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

$(document).ready(getMaxBwInit);

var get_max_bw_input_html =
'<form class="form-horizontal ">                                                                                  ' +

'    <div class="form-group">                                                                                     ' +
'        <div class="col-sm-offset-3 col-sm-3 col-md-offset-4 col-md-2 col-lg-offset-4 col-lg-1 badge st-form-label">Node ID</div> ' +
'        <div class="col-sm-3 col-md-3 dropdown">                                                                 ' +
'          <button class="btn btn-default dropdown-toggle st-nodeid-dropdown-width" type="button" id="js-node-id" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true"> ' +
'             Select Node ID  <span class="caret"></span>                                                         ' +
'          </button>                                                                                              ' +
'          <ul class="dropdown-menu" aria-labelledby="js-node-id" id="js-node-dropdown-menu">                     ' +
'          </ul>                                                                                                  ' +
'        </div>  <!-- class=dropdown -->                                                                          ' +
'    </div> <!-- end form-group -->                                                                               ' +

'    <div class="form-group">                                                                                     ' +
'        <div class="col-sm-offset-4 col-sm-4 col-md-offset-5 col-md-3 col-lg-offset-5 st-getbw-width">           ' +
'            <button type="submit" id="js-get-max-bw-btn" class="btn btn-info btn-block" href="#">Get Max BW</button> ' +
'        </div>                                                                                                   ' +
'    </div> <!-- end form-group -->                                                                               ' +

'</form> <!-- end form-horizontal -->'; //get_max_bw_input_html

var get_max_bw_output_html =
'<div class="row" id="js-output-row"> <!-- output params -->                                                      ' +
'    <form class="form-horizontal">                                                                               ' +

'        <div class="form-group">                                                                                 ' +
'            <div class="col-sm-offset-2 col-sm-8 col-md-offset-3 col-md-6" id="js-result-text"></div>            ' +
'        </div> <!-- end form-group -->                                                                           ' +

'        <div class="form-group">                                                                                 ' +
'            <div class="col-sm-offset-2 col-sm-4 col-md-offset-3 col-md-3 col-lg-offset-4 col-lg-2 badge st-form-label">Uplink only (mbps)</div> ' +
'            <div class="col-sm-4 col-md-3 col-lg-2">                                                             ' +
'                <input type="text" class="form-control st-form-value" id="js-uplink-bw-txt" disabled="true">     ' +
'            </div>                                                                                               ' +
'        </div> <!-- end form-group -->                                                                           ' +

'        <div class="form-group">                                                                                 ' +
'            <div class="col-sm-offset-2 col-sm-4 col-md-offset-3 col-md-3 col-lg-offset-4 col-lg-2 badge st-form-label">Downlink only (mbps)</div> ' +
'            <div class="col-sm-4 col-md-3 col-lg-2">                                                             ' +
'                <input type="text" class="form-control st-form-value" id="js-downlink-bw-txt" disabled="true">   ' +
'            </div>                                                                                               ' +
'        </div> <!-- end form-group -->                                                                           ' +

'        <div class="form-group">                                                                                 ' +
'            <div class="col-sm-offset-2 col-sm-8 col-md-offset-3 col-md-6" id="st-gwtbw-disclaimer">             ' +
'               <b>DISCLAIMER</b>: Available BW may vary based on link quality and BW allocation algorithm. See Orchestrator User Guide for more details. ' +
'            </div>                                                                                               ' +
'        </div> <!-- end form-group -->                                                                           ' +

'    </form> <!-- end form-horizontal -->                                                                         ' +
'</div> <!-- row output params -->'; //get_max_bw_output_html

function getMaxBwInit() {
    printLog('dbg', 'getMaxBwInit entered');

    //1. initialize
    commonInit();
    $('#st-pg-title').html('Get Maximum Available Bandwidth');

    //2. make page active
    $('#js-pg-get-max-bw').addClass("active");

    //3. enable page if session is valid
    if (validateSession() == 'err') {
        printLog('err', 'session invalid. Customer registration required!!');
        $('#js-right-corepart').html('<div id="js-http-err">Customer registration required!!</div>');
        return 'err';
    }

    //4. set page content
    $('#js-input-params').html(get_max_bw_input_html);
    $('#js-output-params').html(get_max_bw_output_html);

    //5. enable event handlers
    $('#js-get-max-bw-btn').on("click", getMaxBwBtnClicked);

    //6. populate input field
    nodeIdPopulateDropdown();

    //7. cleanout output fields
    cleanOutResult();

    //8. Create response code map
    getMaxBwCreateResponseCodeMap();
} //func getMaxBwInit

// file-global map containing output response code and corresponding interpretations
var getMaxBwResponseCodeMap = new Map();
function getMaxBwCreateResponseCodeMap() {
    printLog('dbg', 'getMaxBwCreateResponseCodeMap entered');
    getMaxBwResponseCodeMap.set('NC_NBI_OK',                 'Bandwidth Available is:');
    getMaxBwResponseCodeMap.set('NC_NBI_INVALID_SESSION_ID', 'Invalid Session ID');
    getMaxBwResponseCodeMap.set('NC_NBI_INVALID_NODE_ID',    'Invalid Node ID');
} //func getMaxBwCreateResponseCodeMap

function getMaxBwBtnClicked(evnt) {
    printLog('dbg', 'getMaxBwBtnClicked entered');
    cleanOutResult();

    nodeId = $("#js-node-id").text();
    // hit rpc only if there is a mac selected - mac is string containing colons
    if (nodeId.indexOf(":") != -1) {
        try {
            getMaxBwInvokeRpc();
        }
        catch(e) {
            errTxt = 'RPC exception - ' + e;
            printLog('err', errTxt);
            populateHttpErrorText(errTxt);
        }
    }
    else {
        alert('Please select valid node ID');
    }

    evnt.preventDefault(); //avoid page reload
} //func getMaxBwBtnClicked

function getMaxBwInvokeRpc() {
    printLog('dbg', 'getMaxBwInvokeRpc entered');
    var getMaxBwRequestBody = {
        "input" : {
            "sessionId" : localStorage.getItem("session_id"),
            "nodeId"    : macStrToDec($("#js-node-id").text())
        }
    };

    if (isUtMode() == true) {
        ut_getMaxBw_rpcStub();
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlGetMaxBw"), getMaxBwRequestBody, getMaxBwParseResponseCallback);
    }
} //func getMaxBwInvokeRpc

function getMaxBwParseResponseCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'getMaxBwParseResponseCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ', JSON.stringify(textStatus));

    getMaxBwPopulateOutput(data);
} //func getMaxBwParseResponseCallback

function getMaxBwPopulateOutput(response) {
    printLog('dbg', 'getMaxBwPopulateOutput entered');

    try{
        response_code       = response.output.returnCode;
        result_text = getMaxBwResponseCodeMap.get(response_code);
        printLog('info', 'response_code='+response_code, ', result_text='+result_text);
        $('#js-response-code').html(response_code);

        if (response_code == 'NC_NBI_OK') {
            max_bw_ul          = response.output.maxBwUl;
            printLog('info', 'max_bw_ul', max_bw_ul);
            localStorage.setItem("max_bw_ul", max_bw_ul);
            $('#js-uplink-bw-txt').val(max_bw_ul);

            max_bw_dl          = response.output.maxBwDl;
            printLog('info', 'max_bw_dl', max_bw_dl);
            localStorage.setItem("max_bw_dl", max_bw_dl);
            $('#js-downlink-bw-txt').val(max_bw_dl);

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
} //func getMaxBwPopulateOutput

function ut_getMaxBw_rpcStub() {
    printLog('dbg', 'ut_getMaxBw_rpcStub entered');
    var response = {'output': {returnCode: 'NC_NBI_OK', maxBwUl: 200, maxBwDl: 400}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_SESSION_ID', maxBwUl: 0, maxBwDl: ""}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_NODE_ID', maxBwUl: -200, maxBwDl: -400}};
    getMaxBwPopulateOutput(response);
    $('#js-response-time').html('9999 msec');
} //func ut_getMaxBw_rpcStub
