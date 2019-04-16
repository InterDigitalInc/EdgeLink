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

$(document).ready(alertsInit);

var alerts_input_html =
'<form class="form-horizontal st-inputfields-shiftup">                                   ' +

'    <div class="form-group ">                                                           ' +
'        <div class="col-sm-offset-4 col-sm-2 badge st-form-label">Type</div>            ' +
'        <div class="col-sm-2">                                                          ' +
'            <input class="form-control st-form-value" id="js-type-alerts" placeholder="0-All, 1-Single"> ' +
'        </div>                                                                          ' +
'    </div> <!-- end form-group -->                                                      ' +

'    <div class="form-group">                                                            ' +
'        <div class="col-sm-offset-4 col-sm-2 badge st-form-label">Alert ID</div>        ' +
'        <div class="col-sm-2">                                                          ' +
'            <input type="text" class="form-control st-form-value" id="js-alert-id" placeholder="Alert ID"> ' +
'        </div>                                                                          ' +
'    </div> <!-- end form-group -->                                                      ' +

'    <div class="form-group">                                                            ' +
'        <div class="col-sm-offset-4 col-sm-4 col-md-offset-5 col-md-2">                 ' +
'            <button type="submit" id="js-getalerts-btn" class="btn btn-info btn-block" href="#">Get Alerts</button> ' +
'        </div>                                                                          ' +
'    </div> <!-- end form-group -->                                                      ' +
'</form> <!-- end form-horizontal -->'; //alerts_input_html

var alerts_output_html = 
'<div class="row st-alerts-tbl" id="js-output-row">                                 ' +
'   <table id="js-alerts-tbl" data-toolbar="#js-btn-toolbar" data-search="true"     ' +
'       data-pagination="true" data-page-size="10" data-page-list="[10,50,100]"     ' +
'       data-classes="table table-hover table-condensed" data-striped="true">       ' +
'        <thead>                                                                    ' +
'            <tr>                                                                   ' +
'                <th data-formatter="tableIndexGenerator"> S.No. </th>              ' +
'                <th data-field="alertId"    data-sortable="true"> Alert ID </th>   ' +
'                <th data-field="alertState" data-sortable="true"> Alert State </th>' +
'                <th data-field="alertType"  data-sortable="true"> Alert Type </th> ' +
'                <th data-field="alertInfo"  data-sortable="true"> Alert Info </th> ' +
'            </tr>                                                                  ' +
'        </thead>                                                                   ' +
'    </table>                                                                       ' +
'</div>'; //alerts_output_html

function alertsInit() {
    printLog('dbg', 'alertsInit entered');

    /******** FUTURE SPRINT *********/
    return;

    //1. initialize
    commonInit("alerts");
    $('#st-pg-title').html('Get Alerts');

    //2. make page active
    $('#js-pg-alerts').addClass("active");

    //3. enable page if session is valid
    if (validateSession() == 'err') {
        printLog('err', 'session invalid. Customer registration required!!');
        $('#js-right-corepart').html('<div id="js-http-err">Customer registration required!!</div>');
        return 'err';
    }

    //4. set page content
    $('#js-input-params').html(alerts_input_html);
    $('#js-output-params').html(alerts_output_html);

    //5. enable event handlers
    $('#js-getalerts-btn').on("click", alertsBtnClick);

    //6. populate default input fields
    alertsCreateInputVarsMap();
    localStorage.setItem("type", 0);
    localStorage.setItem("alert_id", 0);
    alertsInputVarsMap.forEach(function(value, key) {
        $(value).val(localStorage.getItem(key));
    });

    //7. cleanout output fields
    cleanOutResult();

    //8. Create response code map
    alertsCreateResponseCodeMap();

    //9. custom CSS
    $('#js-responsebar-row').addClass('st-responsebar-shiftup');

    //10. Call RPC on Pg load itself
    try {
        alertsInvokeRpc();
    }
    catch(e) {
        errTxt = 'RPC exception - ' + e;
        printLog('err', errTxt);
        populateHttpErrorText(errTxt);
    }
} //func alertsInit

// file-global map containing input parameters and corresponding html div placeholders
var alertsInputVarsMap = new Map();
function alertsCreateInputVarsMap() {
    printLog('dbg', 'alertsCreateInputVarsMap entered');
    alertsInputVarsMap.set('type',      '#js-type-alerts');
    alertsInputVarsMap.set('alert_id',  '#js-alert-id');
} //func alertsCreateInputVarsMap

// file-global map containing output response code and corresponding interpretations
var alertsResponseCodeMap = new Map();
function alertsCreateResponseCodeMap() {
    printLog('dbg', 'alertsCreateResponseCodeMap entered');
    alertsResponseCodeMap.set('NC_NBI_OK',                 'Alerts Fetched Successfully');
    alertsResponseCodeMap.set('NC_NBI_INVALID_SESSION_ID', 'Invalid Session ID');
    alertsResponseCodeMap.set('NC_NBI_INVALID_ALERT_ID',   'Invalid Alert ID');
} //func alertsCreateResponseCodeMap

function alertsBtnClick(evnt) {
    printLog('dbg', 'alertsBtnClick entered');
    cleanOutResult();
    if ('ok' == validateInputFields(alertsInputVarsMap)) {
        try {
            alertsInvokeRpc();
        }
        catch(e) {
            errTxt = 'RPC exception - ' + e;
            printLog('err', errTxt);
            populateHttpErrorText(errTxt);
        }
    }
    evnt.preventDefault(); //avoid page reload
} //alertsBtnClick

function alertsInvokeRpc() {
    printLog('dbg', 'alertsInvokeRpc entered');
    var alertsRequestBody = {
        "input" : {
            "sessionId" : localStorage.getItem("session_id"),
            "type"      : localStorage.getItem("type"),
            "alertId"   : localStorage.getItem("alert_id"),
        } //input
    }; //alerts

    if (isUtMode() == true) {
        ut_alerts_rpcStub();
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlGetAlerts"), alertsRequestBody, alertsParseResponseCallback);
    }
} //func alertsInvokeRpc

function alertsParseResponseCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'alertsParseResponseCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ',        JSON.stringify(textStatus));

    alertsPopulateOutput(data);
} //func alertsParseResponseCallback

function alertsPopulateOutput(response) {
    printLog('dbg', 'alertsPopulateOutput entered');

    try{
        response_code       = response.output.returnCode;
        result_text = alertsResponseCodeMap.get(response_code);
        printLog('info', 'response_code='+response_code, ', result_text='+result_text);
        $('#js-response-code').html(response_code);

        if (response_code == 'NC_NBI_OK') {
            numAlert = response.output.numAlert;
            printLog('info', 'numAlert', numAlert);
            localStorage.setItem("numAlert", numAlert);

            alertsPopulateOutputTable(response.output.alertsReportList);
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
} //func alertsPopulateOutput

function alertsPopulateOutputTable(alertsReportList) {
    printLog('dbg', 'alertsPopulateOutputTable entered', 'alertsReportList='+JSON.stringify(alertsReportList));
    $('#js-alerts-tbl').bootstrapTable('destroy');
    $('#js-alerts-tbl').bootstrapTable({data: alertsReportList});
} //func alertsPopulateOutputTable

function ut_alerts_rpcStub() {
    printLog('dbg', 'ut_alerts_rpcStub entered');

    var response_one_alert = {
        "output": {
            "returnCode": "NC_NBI_OK",
            // "returnCode": "NC_NBI_INVALID_SESSION_ID",
            // "returnCode": "NC_NBI_INVALID_ALERT_ID",
            "numAlert": 1,
            "alertsReportList": [
                {
                    "alertId" : 101,
                    "alertState" : "NC_NBI_ALERT_ON",
                    "alertType" : "NC_NBI_SLA_NOT_MET_ALERT",
                    "alertInfo" : 1001
                }
            ] //alertsReportList
        }}; //response_one_alert

    var response_all_alerts = {
        "output": {
            "returnCode": "NC_NBI_OK",
            // "returnCode": "NC_NBI_INVALID_SESSION_ID",
            // "returnCode": "NC_NBI_INVALID_ALERT_ID",
            "numAlert": -1,
            "alertsReportList": [
                {
                    "alertId" : 101,
                    "alertState" : "NC_NBI_ALERT_ON",
                    "alertType" : "NC_NBI_SLA_NOT_MET_ALERT",
                    "alertInfo" : 1001
                },
                {
                    "alertId" : 102,
                    "alertState" : "NC_NBI_ALERT_OFF",
                    "alertType" : "NC_NBI_SLA_MAYBE_MET_ALERT",
                    "alertInfo" : 1002
                },
                {
                    "alertId" : 103,
                    "alertState" : "NC_NBI_ALERT_ON",
                    "alertType" : "NC_NBI_SLA_NOT_MET_ALERT",
                    "alertInfo" : 1003
                },
                {
                    "alertId" : 104,
                    "alertState" : "NC_NBI_ALERT_OFF",
                    "alertType" : "NC_NBI_SLA_MAYBE_MET_ALERT",
                    "alertInfo" : 1004
                },
                {
                    "alertId" : 105,
                    "alertState" : "NC_NBI_ALERT_ON",
                    "alertType" : "NC_NBI_SLA_NOT_MET_ALERT",
                    "alertInfo" : 1005
                },
                {
                    "alertId" : 106,
                    "alertState" : "NC_NBI_ALERT_OFF",
                    "alertType" : "NC_NBI_SLA_MAYBE_MET_ALERT",
                    "alertInfo" : 1006
                },
                {
                    "alertId" : 107,
                    "alertState" : "NC_NBI_ALERT_ON",
                    "alertType" : "NC_NBI_SLA_NOT_MET_ALERT",
                    "alertInfo" : 1007
                },
                {
                    "alertId" : 108,
                    "alertState" : "NC_NBI_ALERT_OFF",
                    "alertType" : "NC_NBI_SLA_MAYBE_MET_ALERT",
                    "alertInfo" : 1008
                },
                {
                    "alertId" : 109,
                    "alertState" : "NC_NBI_ALERT_ON",
                    "alertType" : "NC_NBI_SLA_NOT_MET_ALERT",
                    "alertInfo" : 1009
                },
                {
                    "alertId" : 110,
                    "alertState" : "NC_NBI_ALERT_OFF",
                    "alertType" : "NC_NBI_SLA_MAYBE_MET_ALERT",
                    "alertInfo" : 1010
                },
                {
                    "alertId" : 111,
                    "alertState" : "NC_NBI_ALERT_ON",
                    "alertType" : "NC_NBI_SLA_NOT_MET_ALERT",
                    "alertInfo" : 1011
                },
                {
                    "alertId" : 112,
                    "alertState" : "NC_NBI_ALERT_OFF",
                    "alertType" : "NC_NBI_SLA_MAYBE_MET_ALERT",
                    "alertInfo" : 1012
                },
            ] //alertsReportList
        }}; //response_all_alerts

    alertsPopulateOutput(response_one_alert);
    // alertsPopulateOutput(response_all_alerts);

    $('#js-response-time').html('9999 msec');
} //func ut_alerts_rpcStub
