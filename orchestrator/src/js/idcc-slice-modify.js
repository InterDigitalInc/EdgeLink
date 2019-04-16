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

$(document).ready(sliceModifyInit);

var slice_modify_input_html =
'<form class="form-horizontal st-form-horizontal">                                                          ' +

'    <div class="form-group ">                                                                              ' +
'        <div class="col-sm-offset-4 col-sm-2 badge st-form-label">Slice ID</div>                           ' +
'        <div class="col-sm-2">                                                                             ' +
'            <input class="form-control st-form-value" id="js-slice-id" placeholder="Slice ID">             ' +
'        </div>                                                                                             ' +
'    </div> <!-- end form-group -->                                                                         ' +

'    <div class="col-sm-offset-1 col-sm-10" id="st-slice-modify-tbl"> <!-- SLA panel -->                    ' +
'        <div class="panel panel-info">                                                                     ' +
'            <div class="panel-heading st-slice-panel-heading"> SLA </div>                                  ' +
'            <div class="table-responsive">                                                                 ' +
'                <table class="table">                                                                      ' +
'                    <tr class="active">                                                                    ' +
'                        <th class="col-sm-1 st-slice-modify-priority" rowspan="2">Priority</th>            ' +
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
'        <div class="col-sm-offset-3 col-sm-3 col-md-offset-4 col-md-2">                                    ' +
'            <button type="submit" class="btn btn-info btn-block" id="js-slice-reset"> Reset Values </button> ' +
'        </div>                                                                                             ' +

'        <div class="col-sm-3 col-md-2">                                                                    ' +
'            <button type="submit" class="btn btn-info btn-block" id="js-slice-modify"> Modify Slice </button> ' +
'        </div>                                                                                             ' +
'    </div> <!-- end form-group -->                                                                         ' +
'</form> <!-- end form-horizontal -->'; //slice_modify_input_html

var slice_modify_output_html = 
'<div class="row" id="js-output-row"> <!-- output params -->                                                ' +
'    <form class="form-horizontal">                                                                         ' +
'        <div class="form-group">                                                                           ' +
'            <div class="col-sm-offset-2 col-sm-8" id="js-result-text">                                     ' +
'            </div>                                                                                         ' +
'        </div> <!-- end form-group -->                                                                     ' +
'    </form> <!-- end form-horizontal -->                                                                   ' +
'</div> <!-- row output params -->'; //slice_modify_output_html

function sliceModifyInit() {
    printLog('dbg', 'sliceModifyInit entered');

    //1. initialize
    commonInit();
    $('#st-pg-title').html('Modify Slice');

    //2. make page active
    $('#js-pg-slice-modify').addClass("active");

    //3. enable page if session is valid
    if (validateSession() == 'err') {
        printLog('err', 'session invalid. Customer registration required!!');
        $('#js-right-corepart').html('<div id="js-http-err">Customer registration required!!</div>');
        return 'err';
    }

    //4. set page content
    $('#js-input-params').html(slice_modify_input_html);
    $('#js-output-params').html(slice_modify_output_html);

    //5. enable event handlers
    $('#js-slice-reset').on("click", sliceModifyResetInput);
    $('#js-slice-modify').on("click", sliceModifyBtnClick);

    //6. populate default input fields
    sliceModifyCreateInputVarsMap();
    sliceModifyInputVarsMap.forEach(function(value, key) {
        $(value).val(localStorage.getItem(key));
    });

    //7. cleanout output fields
    cleanOutResult();

    //8. Create response code map
    sliceModifyCreateResponseCodeMap();

    //9. Disable unused values for now
    sliceModifyInputDisableFields();
} //func sliceModifyInit

function sliceModifyInputDisableFields() {
    printLog('dbg', 'sliceModifyInputDisableFields entered');
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
} //func sliceModifyInputDisableFields

// file-global map containing input parameters and corresponding html div placeholders
var sliceModifyInputVarsMap = new Map();
function sliceModifyCreateInputVarsMap() {
    printLog('dbg', 'sliceModifyCreateInputVarsMap entered');
    sliceModifyInputVarsMap.set('slice_id', '#js-slice-id');
    sliceModifyInputVarsMap.set('sla_priority', '#js-sla-input-priority');
    sliceModifyInputVarsMap.set('sla_acc_int', '#js-sla-input-acc-int');
    sliceModifyInputVarsMap.set('sla_rep_int', '#js-sla-input-rep-int');
    sliceModifyInputVarsMap.set('sla_ul_cir', '#js-sla-input-ul-cir');
    sliceModifyInputVarsMap.set('sla_ul_cbs', '#js-sla-input-ul-cbs');
    sliceModifyInputVarsMap.set('sla_ul_eir', '#js-sla-input-ul-eir');
    sliceModifyInputVarsMap.set('sla_ul_ebs', '#js-sla-input-ul-ebs');
    sliceModifyInputVarsMap.set('sla_ul_lat', '#js-sla-input-ul-lat');
    sliceModifyInputVarsMap.set('sla_ul_jit', '#js-sla-input-ul-jit');
    sliceModifyInputVarsMap.set('sla_ul_fir', '#js-sla-input-ul-fir');
    sliceModifyInputVarsMap.set('sla_dl_cir', '#js-sla-input-dl-cir');
    sliceModifyInputVarsMap.set('sla_dl_cbs', '#js-sla-input-dl-cbs');
    sliceModifyInputVarsMap.set('sla_dl_eir', '#js-sla-input-dl-eir');
    sliceModifyInputVarsMap.set('sla_dl_ebs', '#js-sla-input-dl-ebs');
    sliceModifyInputVarsMap.set('sla_dl_lat', '#js-sla-input-dl-lat');
    sliceModifyInputVarsMap.set('sla_dl_jit', '#js-sla-input-dl-jit');
    sliceModifyInputVarsMap.set('sla_dl_fir', '#js-sla-input-dl-fir');
} //func sliceModifyCreateInputVarsMap

// file-global map containing output response code and corresponding interpretations
var sliceModifyResponseCodeMap = new Map();
function sliceModifyCreateResponseCodeMap() {
    printLog('dbg', 'sliceModifyCreateResponseCodeMap entered');
    sliceModifyResponseCodeMap.set('NC_NBI_OK',                 'Slice Modified Successfully');
    sliceModifyResponseCodeMap.set('NC_NBI_INVALID_SESSION_ID', 'Invalid Session ID');
    sliceModifyResponseCodeMap.set('NC_NBI_INVALID_SLICE_ID',   'Invalid Slice ID');
    sliceModifyResponseCodeMap.set('NC_NBI_INVALID_SLA',        'Invalid SLA');
    sliceModifyResponseCodeMap.set('NC_NBI_CANNOT_MEET_SLA',    'Cannot Meet SLA');
} //func sliceModifyCreateResponseCodeMap

function sliceModifyResetInput(evnt) {
    printLog('dbg', 'sliceCreateResetInput entered');
    $('.form-control').val('');

    //cleanout output fields
    cleanOutResult();

    evnt.preventDefault(); //avoid page reload
} //func sliceCreateResetInput

function sliceModifyBtnClick(evnt) {
    printLog('dbg', 'sliceModifyBtnClick entered');
    cleanOutResult();
    if ('ok' == validateInputFields(sliceModifyInputVarsMap)) {
        try {
            sliceModifyInvokeRpc();
        }
        catch(e) {
            errTxt = 'RPC exception - ' + e;
            printLog('err', errTxt);
            populateHttpErrorText(errTxt);
        }
    }
    evnt.preventDefault(); //avoid page reload
} //sliceModifyBtnClick

function sliceModifyInvokeRpc() {
    printLog('dbg', 'sliceModifyInvokeRpc entered');
    var sliceModifyRequestBody = {
        "input" : {
            "sessionId" : localStorage.getItem("session_id"),
            "sliceId"    : localStorage.getItem("slice_id"),
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
    }; //sliceModify

    if (isUtMode() == true) {
        ut_sliceModify_rpcStub();
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlSliceModify"), sliceModifyRequestBody, sliceModifyParseResponseCallback);
    }
} //func sliceModifyInvokeRpc

function sliceModifyParseResponseCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'sliceModifyParseResponseCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ',        JSON.stringify(textStatus));

    sliceModifyPopulateOutput(data);
} //func sliceModifyParseResponseCallback

function sliceModifyPopulateOutput(response) {
    printLog('dbg', 'sliceModifyPopulateOutput entered');

    try{
        response_code       = response.output.returnCode;
        result_text = sliceModifyResponseCodeMap.get(response_code);
        printLog('info', 'response_code='+response_code, ', result_text='+result_text);
        $('#js-response-code').html(response_code);

        if (response_code == 'NC_NBI_OK') {
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
} //func sliceModifyPopulateOutput

function ut_sliceModify_rpcStub() {
    printLog('dbg', 'ut_sliceModify_rpcStub entered');
    var response = {'output': {returnCode: 'NC_NBI_OK'}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_SESSION_ID'}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_SLICE_ID'}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_SLA'}};
    // var response = {'output': {returnCode: 'NC_NBI_CANNOT_MEET_SLA'}};
    sliceModifyPopulateOutput(response);
    $('#js-response-time').html('9999 msec');
} //func ut_sliceModify_rpcStub
