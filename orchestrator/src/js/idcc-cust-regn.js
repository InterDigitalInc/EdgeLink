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

$(document).ready(custRegnInit);

var cust_regn_input_html =
'<form class="form-horizontal ">                                                                                  ' +
'    <div class="form-group">                                                                                     ' +
'        <div class="col-sm-offset-3 col-sm-3 badge st-form-label">Customer ID</div>                              ' +
'        <div class="col-sm-3">                                                                                   ' +
'            <input type="text" class="form-control st-form-value" id="js-cust-id" placeholder="customer ID">     ' +
'        </div>                                                                                                   ' +
'    </div> <!-- end form-group -->                                                                               ' +

'    <div class="form-group ">                                                                                    ' +
'        <div class="col-sm-offset-3 col-sm-3 badge st-form-label">Password</div>                                 ' +
'        <div class="col-sm-3">                                                                                   ' +
'            <input type="password" class="form-control st-form-value" id="js-password" placeholder="password">   ' +
'        </div>                                                                                                   ' +
'    </div> <!-- end form-group -->                                                                               ' +

'    <div class="form-group">                                                                                     ' +
'        <div class="col-sm-offset-3 col-sm-3 badge st-form-label">API Version</div>                              ' +
'        <div class="col-sm-3">                                                                                   ' +
'            <input type="text" class="form-control st-form-value" id="js-orch-api-ver" placeholder="Orchestrator API version"> ' +
'        </div>                                                                                                   ' +
'    </div> <!-- end form-group -->                                                                               ' +

'    <div class="form-group">                                                                                     ' +
'        <div class="col-sm-offset-4 col-sm-4 col-md-offset-5 col-md-2">                                          ' +
'            <button type="submit" id="js-register-btn" class="btn btn-info btn-block" href="#"> Register</button>' +
'        </div>                                                                                                   ' +
'    </div> <!-- end form-group -->                                                                               ' +
'</form> <!-- end form-horizontal -->'; //cust_regn_input_html

var cust_regn_output_html =
'<div class="row" id="js-output-row"> <!-- output params -->                                                      ' +
'    <form class="form-horizontal">                                                                               ' +

'        <div class="form-group">                                                                                 ' +
'            <div class="col-sm-offset-2 col-sm-8 col-md-offset-3 col-md-6" id="js-result-text">                  ' +
'            </div>                                                                                               ' +
'        </div> <!-- end form-group -->                                                                           ' +

'        <div class="form-group">                                                                                 ' +
'            <div class="col-sm-offset-3 col-sm-3 badge st-form-label">Session ID</div>                           ' +
'            <div class="col-sm-3">                                                                               ' +
'                <input type="text" class="form-control st-form-value" id="js-session-id-txt" disabled="true">                    ' +
'            </div>                                                                                               ' +
'        </div> <!-- end form-group -->                                                                           ' +

'        <div class="form-group">                                                                                 ' +
'            <div class="col-sm-offset-3 col-sm-3 badge st-form-label">Session Duration</div>                     ' +
'            <div class="col-sm-3">                                                                               ' +
'                <input type="text" class="form-control st-form-value" id="js-session-duration-txt" disabled="true">              ' +
'            </div>                                                                                               ' +
'        </div> <!-- end form-group -->                                                                           ' +

'        <div class="form-group">                                                                                 ' +
'            <div class="col-sm-offset-3 col-sm-3 badge st-form-label">API Version</div>                          ' +
'            <div class="col-sm-3">                                                                               ' +
'                <input type="text" class="form-control st-form-value" id="js-cntrlr-api-ver-txt" disabled="true">                ' +
'            </div>                                                                                               ' +
'        </div> <!-- end form-group -->                                                                           ' +

'    </form> <!-- end form-horizontal -->                                                                         ' +
'</div> <!-- row output params -->'; //cust_regn_output_html

function custRegnInit() {
    printLog('dbg', 'custRegnInit entered');

    //1. initialize
    commonInit();
    $('#st-pg-title').html('Customer Registration');

    //2. make page active
    $('#js-pg-cust-regn').addClass("active");

    //3. enable page if session is valid
    //this page is always available - even out of session

    //4. set page content
    $('#js-input-params').html(cust_regn_input_html);
    $('#js-output-params').html(cust_regn_output_html);

    //5. enable event handlers
    $('#js-register-btn').on("click", custRegnRegisterBtnClicked);

    //6. populate default input fields
    custRegnCreateInputVarsMap();
    custRegnInputVarsMap.forEach(function(value, key) {
        $(value).val(localStorage.getItem(key));
    });

    //7. cleanout output fields
    cleanOutResult();

    //8. Create response code map
    custRegnCreateResponseCodeMap();

    //9. Disable unused values for now
    disableField("#js-orch-api-ver", "yes");
} //func custRegnInit

// file-global map containing input parameters and corresponding html div placeholders
var custRegnInputVarsMap = new Map();
function custRegnCreateInputVarsMap() {
    printLog('dbg', 'custRegnCreateInputVarsMap entered');
    custRegnInputVarsMap.set('customer_id', '#js-cust-id');
    custRegnInputVarsMap.set('password', '#js-password');
    custRegnInputVarsMap.set('orch_api_ver', '#js-orch-api-ver');
} //func custRegnCreateInputVarsMap

// file-global map containing output response code and corresponding interpretations
var custRegnResponseCodeMap = new Map();
function custRegnCreateResponseCodeMap() {
    printLog('dbg', 'custRegnCreateResponseCodeMap entered');
    custRegnResponseCodeMap.set('NC_NBI_OK',                    'Registration Successful');
    custRegnResponseCodeMap.set('NC_NBI_INVALID_CUSTOMER_ID',   'Invalid Customer ID');
    custRegnResponseCodeMap.set('NC_NBI_INVALID_PASSWORD',      'Invalid Password');
    custRegnResponseCodeMap.set('NC_NBI_VERSION_NOT_SUPPORTED', 'API version not supported');
} //func custRegnCreateResponseCodeMap

function custRegnRegisterBtnClicked(evnt) {
    printLog('dbg', 'custRegnRegisterBtnClicked entered');
    cleanOutResult();
    if ('ok' == validateInputFields(custRegnInputVarsMap)) {
        try {
            custRegnInvokeRpc();
        }
        catch(e) {
            errTxt = 'RPC exception - ' + e;
            printLog('err', errTxt);
            populateHttpErrorText(errTxt);
        }
    }
    evnt.preventDefault(); //avoid page reload
} //func custRegnRegisterBtnClicked

function custRegnInvokeRpc() {
    printLog('dbg', 'custRegnInvokeRpc entered');
    var custRegnRequestBody = {
        "input" : {
            "customerId" : localStorage.getItem("customer_id"),
            "password"   : localStorage.getItem("password"),
            "apiVersion" : "0" //unused as of now - actual - localStorage.getItem("orch_api_ver"),
        }
    };
    localStorage.removeItem('password'); //dont keep it in localStorage any longer


    if (isUtMode() == true) {
        ut_custRegn_rpcStub();
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlCustRegn"), custRegnRequestBody, custRegnParseResponseCallback, custRegnErrorResponseCallback);
    }
} //func custRegnInvokeRpc

function custRegnErrorResponseCallback(jqXHR, textStatus, errorThrown) {
    printLog('dbg', 'custRegnErrorResponseCallback entered');
    rpcErrorCallback(jqXHR, textStatus, errorThrown);
    customerDeregistrationTrigger();
} //func custRegnErrorResponseCallback

function custRegnParseResponseCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'custRegnParseResponseCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ', JSON.stringify(textStatus));

    custRegnPopulateOutput(data);
} //func custRegnParseResponseCallback

function custRegnPopulateOutput(response) {
    printLog('dbg', 'custRegnPopulateOutput entered');

    try{
        response_code       = response.output.returnCode;
        result_text = custRegnResponseCodeMap.get(response_code);
        printLog('info', 'response_code='+response_code, ', result_text='+result_text);
        $('#js-response-code').html(response_code);

        if (response_code == 'NC_NBI_OK') {
            session_id          = response.output.sessionId;
            printLog('info', 'session_id', session_id);
            localStorage.setItem("session_id", session_id);
            $('#js-session-id-txt').val(session_id);

            session_duration    = response.output.sessionDuration;
            printLog('info', 'session_duration', session_duration);
            localStorage.setItem("session_duration", session_duration);
            $('#js-session-duration-txt').val(session_duration);

            cntrlr_api_ver      = response.output.apiVersion;
            printLog('info', 'cntrlr_api_ver', cntrlr_api_ver);
            localStorage.setItem("cntrlr_api_ver", cntrlr_api_ver);
            // $('#js-cntrlr-api-ver-txt').val(cntrlr_api_ver);  //unused as of now
            disableField("#js-cntrlr-api-ver-txt", "yes"); //unused as of now

            custRegnSetSessionStart();
            custRegnSetSessionEnd();
            populateResultText(result_text);

            // remove NodeID mac strings until get node info rpc is invoked
            nodeIdClearStorage();
        }
        else {
            if (! result_text) {
                result_text = response_code;
            }
            populateHttpErrorText(result_text);
            alert('Cusomter Registration returned error: ' + result_text);
            customerDeregistrationTrigger();
        }
    }
    catch(e) {
        error_string = 'Parse error in response:<br/>' + JSON.stringify(response) + '<br/>' + e;
        printLog('err', 'exception: ', error_string);
        populateHttpErrorText(error_string);
    }
} //func custRegnPopulateOutput

function custRegnSetSessionStart() {
    printLog('dbg', 'custRegnSetSessionStart entered');
    current_moment = moment();
    session_start_date = current_moment.format('DD-MMM-YYYY');
    session_start_time = current_moment.format('HHmm');

    localStorage.setItem('session_start_date', session_start_date);
    localStorage.setItem('session_start_time', session_start_time);
    printLog('info', 'session_start= ', session_start_date+' '+session_start_time);
} //func custRegnSetSessionStart

function custRegnSetSessionEnd() {
    printLog('dbg', 'custRegnSetSessionEnd entered');

    session_start_date = localStorage.getItem('session_start_date');
    session_start_time = localStorage.getItem('session_start_time');
    printLog('dbg', 'session_start= ', session_start_date+' '+session_start_time);
    session_start_moment = moment(session_start_date+' '+session_start_time, 'DD-MMM-YYYY HHmm');

    try {
        session_duration = Number(localStorage.getItem('session_duration'));
    }
    catch(e) {
        printLog('err', 'session_duration get Exception:', e);
        return 'err';
    }
    printLog('dbg', 'session_duration (mins)=', session_duration);
    if (session_duration == 0) { //infinite duration
        session_end_date = '0';
        session_end_time = '0';
    }
    else { //finite duration
        session_end_moment = moment(session_start_moment).add(session_duration, 'minutes');
        session_end_date = session_end_moment.format('DD-MMM-YYYY');
        session_end_time = session_end_moment.format('HHmm');

        localStorage.setItem('session_mins_left', session_duration);
        sessionTimerStart();
    } //if-else session_duration

    localStorage.setItem('session_end_date', session_end_date);
    localStorage.setItem('session_end_time', session_end_time);
    printLog('info', 'session_end= ', session_end_date+' '+session_end_time);

    updateSessionFields();
} //func custRegnSetSessionEnd

function ut_custRegn_rpcStub() {
    printLog('dbg', 'ut_custRegn_rpcStub entered');
    var response = {'output': {returnCode: 'NC_NBI_OK', apiVersion: 111, sessionId: 444, sessionDuration: 100}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_CUSTOMER_ID', apiVersion: 0, sessionId: 0, sessionDuration: 0}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_PASSWORD', apiVersion: -1, sessionId: -1, sessionDuration: -1}};
    // var response = {'output': {returnCode: 'NC_NBI_VERSION_NOT_SUPPORTED', apiVersion: "", sessionId: "", sessionDuration: ""}};
    custRegnPopulateOutput(response);
    $('#js-response-time').html('9999 msec');
} //func ut_custRegn_rpcStub
