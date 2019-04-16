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

$(document).ready(sliceCreateInit);

var dropdownHtml = 
'          <button class="btn btn-default dropdown-toggle st-nodeid-dropdown-width" type="button" id="js-node-id" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true"> ' +
'             Select Node ID  <span class="caret"></span>                                                         ' +
'          </button>                                                                                              ' +
'          <ul class="dropdown-menu" aria-labelledby="js-node-id" id="js-node-dropdown-menu">                     ' +
'          </ul>                                                                                                  ';

var slice_create_input_html =
'<form class="form-horizontal st-form-horizontal" id="st-slice-create-input">                    ' +

'    <div class="form-group">                                                                                     ' +
'        <div class="col-sm-offset-3 col-sm-3 col-md-offset-4 col-md-2 col-lg-offset-4 col-lg-1 badge st-form-label">Node ID</div> ' +
'        <div class="col-sm-3 col-md-3 dropdown" id="js-dropdown-div">                                            ' +
'        </div>  <!-- js-dropdown-div -->                                                                         ' +
'    </div> <!-- end form-group -->                                                                               ' +

'    <br/>  ' +

'    <div class="form-group st-form-group">                                                     ' +
'        <div class="col-md-offset-1 col-lg-offset-3 col-sm-2 col-md-2 col-lg-1 badge st-form-label">POA ID</div> ' +
'        <div class="col-sm-10 col-md-9 col-lg-8">                                              ' +
'            <input type="text" id="js-poa-id-1" class="st-input-mac" maxlength="2" size="1" /> ' +
'            :                                                                                  ' +
'            <input type="text" id="js-poa-id-2" class="st-input-mac" maxlength="2" size="1" /> ' +
'            :                                                                                  ' +
'            <input type="text" id="js-poa-id-3" class="st-input-mac" maxlength="2" size="1" /> ' +
'            :                                                                                  ' +
'            <input type="text" id="js-poa-id-4" class="st-input-mac" maxlength="2" size="1" /> ' +
'            :                                                                                  ' +
'            <input type="text" id="js-poa-id-5" class="st-input-mac" maxlength="2" size="1" /> ' +
'            :                                                                                  ' +
'            <input type="text" id="js-poa-id-6" class="st-input-mac" maxlength="2" size="1" /> ' +
'        </div>                                                                                 ' +
'    </div> <!-- end form-group -->                                                             ' +

'    <div class="col-sm-offset-1 col-sm-10" id="st-slice-create-tbl"> <!-- SLA panel -->                    ' +
'        <div class="panel panel-info">                                                                     ' +
'            <div class="panel-heading st-slice-panel-heading"> SLA </div>                                  ' +
'            <div class="table-responsive">                                                                 ' +
'                <table class="table">                                                                      ' +
'                    <tr class="active">                                                                    ' +
'                        <th class="col-sm-1 st-slice-create-priority" rowspan="2">Priority</th>            ' +
'                        <th colspan="3">Statistics</th>                                                    ' +
'                    </tr>                                                                                  ' +
'                    <tr class="active">                                                                    ' +
'                        <th class="col-sm-1">Stats Report</th>                                             ' +
'                        <th class="col-sm-1">Accumulation Interval (sec)</th>                              ' +
'                        <th class="col-sm-1">Reporting Interval (sec)</th>                                 ' +
'                    </tr>                                                                                  ' +
'                    <tr>                                                                                   ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-priority" placeholder="Priority"></td> ' +
'                        <td>                                                                               ' +
'                            <label class="st-stats-chkbox st-text-ellipsis">                               ' +
'                                <input type="checkbox" data-toggle="toggle" data-onstyle="info" id="js-sla-stats-chk"> ' +
//checked propoerty off by default as this is unused currently
// '                                <input type="checkbox" checked data-toggle="toggle" data-onstyle="info" id="js-sla-stats-chk"> ' +
'                            </label>                                                                       ' +
'                        </td>                                                                              ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-acc-int" placeholder="Accumulation Interval"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-rep-int" placeholder="Reporting Interval"></td> ' +
'                    </tr>                                                                                  ' +
'                </table>                                                                                   ' +

'                <table class="table table-hover">                                                          ' +
'                    <tr class="active">                                                                    ' +
'                        <th></th>                                                                          ' +
'                        <th>CIR (Mbps)</th>                                                                ' +
'                        <th>CBS (Kbytes)</th>                                                              ' +
'                        <th>EIR (Mbps)</th>                                                                ' +
'                        <th>EBS (Kbytes)</th>                                                              ' +
'                        <th>Latency (usec)</th>                                                            ' +
'                        <th>Jitter (msec)</th>                                                             ' +
'                        <th>FLR</th>                                                                       ' +
'                    </tr>                                                                                  ' +
'                    <tr>                                                                                   ' +
'                        <th class="active">UL</th>                                                         ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-ul-cir" placeholder="UL CIR"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-ul-cbs" placeholder="UL CBS"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-ul-eir" placeholder="UL EIR"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-ul-ebs" placeholder="UL EBS"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-ul-lat" placeholder="UL Lat"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-ul-jit" placeholder="UL Jitter"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-ul-fir" placeholder="UL FIR"></td> ' +
'                    </tr>                                                                                  ' +
'                    <tr>                                                                                   ' +
'                        <th class="active">DL</th>                                                         ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-dl-cir" placeholder="DL CIR"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-dl-cbs" placeholder="DL CBS"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-dl-eir" placeholder="DL EIR"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-dl-ebs" placeholder="DL EBS"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-dl-lat" placeholder="DL Lat"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-dl-jit" placeholder="DL Jitter"></td> ' +
'                        <td><input type="text" class="form-control" id="js-sla-input-dl-fir" placeholder="DL FIR"></td> ' +
'                    </tr>                                                                                  ' +
'                </table>                                                                                   ' +
'            </div> <!-- panel-body -->                                                                     ' +
'        </div> <!-- panel-info -->                                                                         ' +
'    </div>  <!-- SLA panel -->                                                                             ' +

'    <div class="form-group">                                                                               ' +
'        <div class="col-sm-offset-2 col-md-offset-3 col-sm-3 col-md-2">                                    ' +
'            <button type="submit" class="btn btn-info btn-block" id="js-slice-reset"> Reset Values </button> ' +
'        </div>                                                                                             ' +

'        <div class="col-sm-offset-2 col-sm-3 col-md-2">                                                    ' +
'            <button type="submit" class="btn btn-info btn-block" id="js-slice-create"> Create Slice </button> ' +
'        </div>                                                                                             ' +
'    </div> <!-- end form-group -->                                                                         ' +

'</form> <!-- end form-horizontal -->'; //slice_create_input_html

var slice_create_output_html =
'<div class="row" id="js-output-row"> <!-- output params -->                                                ' +
'    <form class="form-horizontal">                                                                         ' +

'        <div class="form-group">                                                                           ' +
'            <div class="col-sm-offset-2 col-sm-8" id="js-result-text">                                     ' +
'            </div>                                                                                         ' +
'        </div> <!-- end form-group -->                                                                     ' +

'        <div class="form-group">                                                                           ' +
'            <div class="col-sm-offset-3 col-md-offset-4 col-sm-3 col-md-2 badge st-form-label">Slice ID</div> ' +
'            <div class="col-sm-3 col-md-2">                                                                ' +
'               <input type="text" class="form-control st-form-value" id="js-slice-id-txt">                 ' +
'            </div>                                                                                         ' +
'        </div> <!-- end form-group -->                                                                     ' +

'    </form> <!-- end form-horizontal -->                                                                   ' +
'</div> <!-- row output params -->'; //slice_create_output_html

function sliceCreateInit() {
    printLog('dbg', 'sliceCreateInit entered');

    //1. initialize
    commonInit();
    $('#st-pg-title').html('Create Slice');

    //2. make page active
    $('#js-pg-slice-create').addClass("active");

    //3. enable page if session is valid
    if (validateSession() == 'err') {
        printLog('err', 'session invalid. Customer registration required!!');
        $('#js-right-corepart').html('<div id="js-http-err">Customer registration required!!</div>');
        return 'err';
    }

    //4. set page content
    $('#js-input-params').html(slice_create_input_html);
    $('#js-output-params').html(slice_create_output_html);

    //5. enable event handlers
    $('#js-slice-reset').on("click", sliceCreateResetInput);
    $('#js-slice-create').on("click", sliceCreateBtnClicked);

    //6. populate default input fields
    //here we can avoid maintaining localStorage as there is no save functionality required but anyways as a bonus...
    sliceCreateCreateInputVarsMap();
    sliceCreateInputVarsMap.forEach(function(value, key) {
        $(value).val(localStorage.getItem(key));
    });
    sliceCreateAffixNodeIdDropdown();

    //7. cleanout output fields
    cleanOutResult();

    //8. Create response code map
    sliceCreateCreateResponseCodeMap();

    //9. custom CSS
    $('#js-responsebar-row').addClass('st-responsebar-shiftup');

    //10. AutoTab functionality for mac address fields
    $.autotab({ tabOnSelect: true });
    $('.st-input-mac').autotab('filter', 'hexadecimal');

    //11. Disable unused values for now
    sliceCreateInputDisableFields();
} //func sliceCreateInit

function sliceCreateInputDisableFields() {
    printLog('dbg', 'sliceCreateInputDisableFields entered');
    disableField("#js-sla-input-ul-eir", "yes");
    disableField("#js-sla-input-ul-ebs", "yes");
    disableField("#js-sla-input-ul-lat", "yes");
    disableField("#js-sla-input-ul-jit", "yes");
    disableField("#js-sla-input-ul-fir", "yes");

    disableField("#js-sla-input-dl-eir", "yes");
    disableField("#js-sla-input-dl-ebs", "yes");
    disableField("#js-sla-input-dl-lat", "yes");
    disableField("#js-sla-input-dl-jit", "yes");
    disableField("#js-sla-input-dl-fir", "yes");

    disableField("#js-sla-input-acc-int", "yes");
    disableField("#js-sla-input-rep-int", "yes");
    disableField("#js-sla-stats-chk");
} //func sliceCreateInputDisableFields

// file-global map containing input parameters and corresponding html div placeholders
var sliceCreateInputVarsMap = new Map();
function sliceCreateCreateInputVarsMap() {
    printLog('dbg', 'sliceCreateCreateInputVarsMap entered');
    sliceCreateInputVarsMap.set('poa_id-1stByte', '#js-poa-id-1');
    sliceCreateInputVarsMap.set('poa_id-2ndByte', '#js-poa-id-2');
    sliceCreateInputVarsMap.set('poa_id-3rdByte', '#js-poa-id-3');
    sliceCreateInputVarsMap.set('poa_id-4thByte', '#js-poa-id-4');
    sliceCreateInputVarsMap.set('poa_id-5thByte', '#js-poa-id-5');
    sliceCreateInputVarsMap.set('poa_id-6thByte', '#js-poa-id-6');
    sliceCreateInputVarsMap.set('sla_priority', '#js-sla-input-priority');
    sliceCreateInputVarsMap.set('sla_acc_int', '#js-sla-input-acc-int');
    sliceCreateInputVarsMap.set('sla_rep_int', '#js-sla-input-rep-int');
    sliceCreateInputVarsMap.set('sla_ul_cir', '#js-sla-input-ul-cir');
    sliceCreateInputVarsMap.set('sla_ul_cbs', '#js-sla-input-ul-cbs');
    sliceCreateInputVarsMap.set('sla_ul_eir', '#js-sla-input-ul-eir');
    sliceCreateInputVarsMap.set('sla_ul_ebs', '#js-sla-input-ul-ebs');
    sliceCreateInputVarsMap.set('sla_ul_lat', '#js-sla-input-ul-lat');
    sliceCreateInputVarsMap.set('sla_ul_jit', '#js-sla-input-ul-jit');
    sliceCreateInputVarsMap.set('sla_ul_fir', '#js-sla-input-ul-fir');
    sliceCreateInputVarsMap.set('sla_dl_cir', '#js-sla-input-dl-cir');
    sliceCreateInputVarsMap.set('sla_dl_cbs', '#js-sla-input-dl-cbs');
    sliceCreateInputVarsMap.set('sla_dl_eir', '#js-sla-input-dl-eir');
    sliceCreateInputVarsMap.set('sla_dl_ebs', '#js-sla-input-dl-ebs');
    sliceCreateInputVarsMap.set('sla_dl_lat', '#js-sla-input-dl-lat');
    sliceCreateInputVarsMap.set('sla_dl_jit', '#js-sla-input-dl-jit');
    sliceCreateInputVarsMap.set('sla_dl_fir', '#js-sla-input-dl-fir');
} //func sliceCreateCreateInputVarsMap

// file-global map containing output response code and corresponding interpretations
var sliceCreateResponseCodeMap = new Map();
function sliceCreateCreateResponseCodeMap() {
    printLog('dbg', 'sliceCreateCreateResponseCodeMap entered');
    sliceCreateResponseCodeMap.set('NC_NBI_OK',                 'Slice created Successfully');
    sliceCreateResponseCodeMap.set('NC_NBI_INVALID_SESSION_ID', 'Invalid Session ID');
    sliceCreateResponseCodeMap.set('NC_NBI_INVALID_NODE_ID',    'Invalid Node ID');
    sliceCreateResponseCodeMap.set('NC_NBI_CANNOT_MEET_SLA',    'Cannot Meet SLA');
    sliceCreateResponseCodeMap.set('NC_NBI_INVALID_POA_ID',     'Invalid POA ID');
} //func sliceCreateCreateResponseCodeMap

function sliceCreateAffixNodeIdDropdown() {
    printLog('dbg', 'sliceCreateAffixNodeIdDropdown entered');
    $("#js-dropdown-div").html(dropdownHtml);
    nodeIdPopulateDropdown();
} //func sliceCreateAffixNodeIdDropdown

function sliceCreateResetInput(evnt) {
    printLog('dbg', 'sliceCreateResetInput entered');
    $('.form-control').val('');
    $('.st-input-mac').val('');

    sliceCreateAffixNodeIdDropdown();

    //cleanout output fields
    cleanOutResult();

    evnt.preventDefault(); //avoid page reload
} //func sliceCreateResetInput

function sliceCreateBtnClicked(evnt) {
    printLog('dbg', 'sliceCreateBtnClicked entered');
    cleanOutResult();

    nodeId = $("#js-node-id").text();
    // hit rpc only if there is a mac selected - mac is string containing colons
    if (nodeId.indexOf(":") == -1) {
        alert('Please select valid node ID');
    }
    else if ('ok' == validateInputFields(sliceCreateInputVarsMap)) {
        try {
            sliceCreateInvokeRpc();
        }
        catch(e) {
            errTxt = 'RPC exception - ' + e;
            printLog('err', errTxt);
            populateHttpErrorText(errTxt);
        }
    }

    evnt.preventDefault(); //avoid page reload
} //func sliceCreateBtnClicked

function sliceCreateInvokeRpc() {
    printLog('dbg', 'sliceCreateInvokeRpc entered');
    var sliceCreateRequestBody = {
        "input" : {
            "sessionId" : localStorage.getItem("session_id"),
            "nodeId"    : macStrToDec($("#js-node-id").text()),
            "poaId"     : macFieldConsolidateValue("#js-poa-id-"),
            "sla" : {
                "bwReqUl" : {
                    "cir" : localStorage.getItem("sla_ul_cir"),
                    "cbs" : localStorage.getItem("sla_ul_cbs"),
                    "eir" : "0", //unused as of now - actual - localStorage.getItem("sla_ul_eir"),
                    "ebs" : "0", //unused as of now - actual - localStorage.getItem("sla_ul_ebs"),
                    "commMaxLatency" : "0", //unused as of now - actual - localStorage.getItem("sla_ul_lat"),
                    "commMaxJitter"  : "0", //unused as of now - actual - localStorage.getItem("sla_ul_jit"),
                    "commMaxFlr"     : "0", //unused as of now - actual - localStorage.getItem("sla_ul_fir"),
                }, //bwReqUl
                "bwReqDl" : {
                    "cir" : localStorage.getItem("sla_dl_cir"),
                    "cbs" : localStorage.getItem("sla_dl_cbs"),
                    "eir" : "0", //unused as of now - actual - localStorage.getItem("sla_dl_eir"),
                    "ebs" : "0", //unused as of now - actual - localStorage.getItem("sla_dl_ebs"),
                    "commMaxLatency" : "0", //unused as of now - actual - localStorage.getItem("sla_dl_lat"),
                    "commMaxJitter"  : "0", //unused as of now - actual - localStorage.getItem("sla_dl_jit"),
                    "commMaxFlr"     : "0", //unused as of now - actual - localStorage.getItem("sla_dl_fir"),
                }, //bwReqDl
                "priorityConfig" : {
                    "priority" : localStorage.getItem("sla_priority"),
                }, //priorityConfig
                "statsConfig" : {
                    "statsReporting"        : "0", //unused as of now - actual - ($("#js-sla-stats-chk")[0].checked)?1:0,
                    "accumulationInterval"  : "0", //unused as of now - actual - localStorage.getItem("sla_acc_int"),
                    "reportingInterval"     : "0" //unused as of now - actual - localStorage.getItem("sla_rep_int"),
                }, //statsConfig
            }, //sla
        } //input
    }; //sliceCreate

    if (isUtMode() == true) {
        ut_sliceCreate_rpcStub();
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlSliceCreate"), sliceCreateRequestBody, sliceCreateParseResponseCallback);
    }
} //func sliceCreateInvokeRpc

function sliceCreateParseResponseCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'sliceCreateParseResponseCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ',        JSON.stringify(textStatus));

    sliceCreatePopulateOutput(data);
} //func sliceCreateParseResponseCallback

function sliceCreatePopulateOutput(response) {
    printLog('dbg', 'sliceCreatePopulateOutput entered');

    try{
        response_code       = response.output.returnCode;
        result_text = sliceCreateResponseCodeMap.get(response_code);
        printLog('info', 'response_code='+response_code, ', result_text='+result_text);
        $('#js-response-code').html(response_code);

        if (response_code == 'NC_NBI_OK') {
            slice_id          = response.output.sliceId;
            printLog('info', 'slice_id', slice_id);
            localStorage.setItem("slice_id", slice_id);
            $('#js-slice-id-txt').val(slice_id);

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
} //func sliceCreatePopulateOutput

function ut_sliceCreate_rpcStub() {
    printLog('dbg', 'ut_sliceCreate_rpcStub entered');
    var response = {'output': {returnCode: 'NC_NBI_OK', sliceId: 444}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_SESSION_ID', sliceId: 0}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_NODE_ID', sliceId: -1}};
    // var response = {'output': {returnCode: 'NC_NBI_CANNOT_MEET_SLA', sliceId: ""}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_POA_ID', sliceId: NaN}};
    sliceCreatePopulateOutput(response);
    $('#js-response-time').html('9999 msec');
} //func ut_sliceCreate_rpcStub
