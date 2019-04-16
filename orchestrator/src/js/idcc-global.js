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

function commonInit(pgType = "general") {
    printLog('dbg', 'commonInit entered');
    pgTemplatesLoad();
    cleanOutResult();
    idccUrlAddToStorage();
    $('#js-pg-*').removeClass("active");

    if ('ok' == updateSessionFields()) {
        //if session fields are valid, check if they are stale
        if ('err' == checkIfSessionStale()) {
            customerDeregistrationTrigger(); //remove stale session
        }

        // keep the timer running
        sessionTimerResume();
    }

    /* persist state of sidebar on pg changes and refreshes */
    oppositeFlag = (localStorage.getItem('isSidebarCollapsed') == "true")?"false":"true";
    localStorage.setItem("isSidebarCollapsed", oppositeFlag); //so that toggle may toggle it :p
    toggleSidebarCollapse();

    //open websocket connection for notifications
    // FUTURE SPRINT - wsInit();
} //func commonInit

// comment either of the two return statement to drive ut or not
function isUtMode() {
    // return true;
    return false;
}

function idccUrlAddToStorage() {
    printLog('dbg', 'idccUrlAddToStorage entered');

    operationsPrefix = '/meshnbi';

    rpcUrlCustRegn = operationsPrefix + '/customer/registration';
    localStorage.setItem("rpcUrlCustRegn", rpcUrlCustRegn);

    rpcUrlCustDeregn = operationsPrefix + '/customer/deregistration';
    localStorage.setItem("rpcUrlCustDeregn", rpcUrlCustDeregn);

    rpcUrlSliceCreate = operationsPrefix + '/slice/create';
    localStorage.setItem("rpcUrlSliceCreate", rpcUrlSliceCreate);

    rpcUrlSliceGetConfig = operationsPrefix + '/slice/config';
    localStorage.setItem("rpcUrlSliceGetConfig", rpcUrlSliceGetConfig);

    rpcUrlSliceDelete = operationsPrefix + '/slice/delete';
    localStorage.setItem("rpcUrlSliceDelete", rpcUrlSliceDelete);

    rpcUrlSliceModify = operationsPrefix + '/slice/modify';
    localStorage.setItem("rpcUrlSliceModify", rpcUrlSliceModify);

    rpcUrlGetMaxBw = operationsPrefix + '/node/max-bandwidth';
    localStorage.setItem("rpcUrlGetMaxBw", rpcUrlGetMaxBw);

    rpcUrlGetAlerts = operationsPrefix + '/alerts';
    localStorage.setItem("rpcUrlGetAlerts", rpcUrlGetAlerts);

    rpcUrlGetStats = operationsPrefix + '/slice/statistics';
    localStorage.setItem("rpcUrlGetStats", rpcUrlGetStats);

    rpcUrlgetNodeInfo = operationsPrefix + '/node/info';
    localStorage.setItem("rpcUrlgetNodeInfo", rpcUrlgetNodeInfo);
} //func idccUrlAddToStorage

/* selectively enable logs */
function printLog(level, text, val="") {
    switch(level) {
        case 'info':  console.log(level+': ' +text+' '+val); break;
        case 'dbg' :  console.log(level+' : '+text+' '+val); break;
        case 'err' : console.warn(level+': ' +text+' '+val); break;
    }
    // console.trace();
} //func printLog

function cleanOutResult() {
    $('#js-response-code').html('');
    $('#js-response-time').html('');
    $('#js-result-text').html('');
    $('#js-http-err').html('');

    $('#js-responsebar-row').hide();
    $('#js-output-row').hide();
    $('#js-http-err').hide();
} //func cleanOutResult

function populateResultText(text) {
    $('#js-result-text').html(text);
    $('#js-output-row').show();
    $('#js-responsebar-row').show();
} //func populateResultText

function populateHttpErrorText(text) {
    $('#js-http-err').html(text);
    $('#js-http-err').show();
    $('#js-responsebar-row').show();
} //func populateHttpErrorText

function toggleSidebarCollapse() {
    $("#st-sidebar").removeClass();
    $("#id-corepart-right-column").removeClass();

    isSidebarCollapsed = localStorage.getItem('isSidebarCollapsed');
    if( isSidebarCollapsed == "true") {
        printLog('dbg', 'sidebar show');
        $('#st-sidebar').collapse('show');
        localStorage.setItem("isSidebarCollapsed", "false");

        $("#st-sidebar").addClass("col-xs-4 col-sm-3 col-md-3 col-lg-2");
        $("#id-corepart-right-column").addClass("col-xs-offset-4 col-xs-8 col-sm-offset-3 col-sm-9 col-md-offset-3 col-md-9 col-lg-offset-2 col-lg-10");
    }
    else {
        printLog('dbg', 'sidebar hide');
        $('#st-sidebar').collapse('hide');
        localStorage.setItem("isSidebarCollapsed", "true");

        $("#st-sidebar").addClass("hidden-xs hidden-sm hidden-md hidden-lg");
        $("#id-corepart-right-column").addClass("col-xs-12");
    }
} //func toggleSidebarCollapse

//expects a JSON object in request, converts it into string and calls xhr
function callRpc(type, uri, request, rpcSuccessCallbackArg, rpcErrorCallbackArg = rpcErrorCallback) {
    url = 'http://' + localStorage.getItem("controller_ip") + ':' + localStorage.getItem("controller_rpc_port") + uri;
    printLog('dbg', 'callRpc entered for url:'+url, 'request:'+JSON.stringify(request));

    rpcStartTime = new Date().getTime();
    printLog('dbg', 'rpcStartTime=', rpcStartTime);
    localStorage.setItem("rpcStartTime", rpcStartTime);
    var jq_xhr = $.ajax({
        type: type,
        url: url,
        data: JSON.stringify(request),
        contentType: 'application/json',
        timeout: 20000, //millisec
        async: true, //required for timeout to work
        cache: false,
    }); //ajax

    jq_xhr.fail(rpcErrorCallbackArg);
    jq_xhr.done(rpcSuccessCallbackArg);
    jq_xhr.always(rpcAlwaysCallback); //register it at the end
} //func callRpc

function rpcErrorCallback(jqXHR, textStatus, errorThrown) {
    printLog('dbg', 'rpcErrorCallback entered');
    printLog('dbg', 'jqXHR: ', JSON.stringify(jqXHR));
    printLog('dbg', 'textStatus: ', JSON.stringify(textStatus));
    printLog('dbg', 'errorThrown: ', JSON.stringify(errorThrown));

    try {
        printLog('err', 'error in response: ', jqXHR.responseText);
        error_string = JSON.stringify(JSON.parse(jqXHR.responseText).errors.error[0])
        .replace(/(?:\\n)/g, '<br/>')
        .replace(/(?:,)/g, ',<br/>')
        .replace(/(?:{|})/g);
    }
    catch(e) {
        //check if preflight/forbidden http
        if ((jqXHR.readyState == 0) && (jqXHR.status == 0)) {
            error_string = 'Controller unreachable at ' + JSON.stringify(this.url)
            + '<br/>Error text: ' + JSON.stringify(jqXHR.statusText);
        }
        else {
            printLog('err', 'rpcErrorCallback', 'Some strange error');
            error_string = JSON.stringify(jqXHR);
        }
    } //try-catch

    printLog('err', 'error_string: ', error_string);
    cleanOutResult();
    populateHttpErrorText(error_string);
} //func rpcErrorCallback

// we update response code here
function rpcAlwaysCallback() {
    printLog('dbg', 'rpcAlwaysCallback entered');
    rpcStartTime = localStorage.getItem("rpcStartTime");
    rpcEndTime = new Date().getTime();
    printLog('dbg', 'rpcStartTime : rpcEndTime :: ', rpcStartTime+' : '+rpcEndTime);
    rpcDiffTime = rpcEndTime - rpcStartTime; //msec
    printLog('info', 'rpc turnaround time (msec)=', rpcDiffTime);
    $('#js-response-time').html(rpcDiffTime + ' msec');
} //func rpcAlwaysCallback

function checkIfSessionStale() {
    printLog('dbg', 'checkIfSessionStale entered');

    session_end_date = localStorage.getItem('session_end_date');
    session_end_time = localStorage.getItem('session_end_time');
    if ((session_end_date == null) || (session_end_time == null)) {
        printLog('err', 'null session values');
        return 'err';
    }

    session_end         = session_end_date+' '+session_end_time;
    session_end_moment  = moment(session_end, 'DD-MMM-YYYY HHmm');
    printLog('dbg', 'session_end= '+session_end, ' session_end_moment='+session_end_moment);

    current_moment   = moment(moment(), 'DD-MMM-YYYY HHmm');
    printLog('dbg', 'current_moment='+current_moment);

    var diff_moment = current_moment.diff(session_end_moment); //a.diff(b) = a-b
    printLog('dbg', 'diff_moment=', diff_moment);
    if (diff_moment > 0) {
        printLog('err', 'current_moment > session_end_moment so session_end is in the past');
        return 'err';
    }

    printLog('dbg', 'current_moment <= session_end_moment so session_end is in the future');
    return 'ok';
} //func checkIfSessionStale

function updateSessionFields() {
    printLog('dbg', 'updateSessionFields entered');
    $('.st-sessionTime-label').hide();
    $('.st-sessionTime-value').hide();

    try {
        session_start_date  = localStorage.getItem('session_start_date');
        session_start_time  = localStorage.getItem('session_start_time');
        session_end_date    = localStorage.getItem('session_end_date');
        session_end_time    = localStorage.getItem('session_end_time');
        if ( (session_start_date == null) || (session_start_time == null) ||
           (session_start_date == 0) || (session_start_time == 0) ||
           (session_end_date == null) || (session_end_time == null) ) {
            printLog('err', 'invalid session values');
            throw 'Customer registration required!!';
        }

        // set session start        
        session_start = session_start_date+' '+session_start_time;
        printLog('dbg', 'session_start=', session_start);
        $('#js-session-start').html(session_start);

        // set session end
        session_end = session_end_date+' '+session_end_time;
        printLog('dbg', 'session_end=', session_end);
        // if session_date is 0, then no session end
        if (session_end_date == 0) {
            infinity_html = '<span style="display:inline-block;transform:scale(3,1);">\u221e</span>';
            $('#js-session-end').html(infinity_html);
        }
        else {
            $('#js-session-end').html(session_end);
        }

        $('.st-sessionTime-label').show();
        $('.st-sessionTime-value').show();
        return 'ok';
    }
    catch(e) {
        printLog('dbg', 'updateSessionFields Exception: ', e);
        return 'err';
    }
} //func updateSessionFields

var session_timer_g; //scope: file global
function sessionTimerStart() {
    printLog('dbg', 'sessionTimerStart entered');

    session_mins_left = Number(localStorage.getItem('session_mins_left'));
    printLog('dbg', 'session_mins_left=', session_mins_left);

    session_timer_g = new Timer(); //scope: file global
    session_timer_g.start({
        countdown: true,
        precision: 'seconds',
            startValues: {seconds: session_mins_left*60}, //startValues means expiry for countdown timer
        });
    session_timer_g.addEventListener('secondsUpdated', function (e) {
            // printLog('dbg', 'sessionTimerUpdateInterval timeLeft=', session_timer_g.getTimeValues().toString());
            hrs = Number(session_timer_g.getTimeValues().toString(['hours']));
            min = Number(session_timer_g.getTimeValues().toString(['minutes']));
            sec = Number(session_timer_g.getTimeValues().toString(['seconds']));
            session_mins_left = hrs*60 + min + sec/60;
            localStorage.setItem('session_mins_left', session_mins_left);
            // printLog('dbg', 'session_mins_left=', session_mins_left);
        });
    session_timer_g.addEventListener('targetAchieved', sessionTimerExpired);
    printLog('dbg', 'session_timer_g created=', JSON.stringify(session_timer_g.getTotalTimeValues()));
} //func sessionTimerStart

function sessionTimerExpired() {
    printLog('dbg', 'sessionTimerExpired entered');
    alert('Session expired! Customer needs to reregister.');
    customerDeregistrationTrigger();
} //func sessionTimerExpired

function sessionTimerResume() {
    printLog('dbg', 'sessionTimerResume entered');
    try {
        session_mins_left = Number(localStorage.getItem('session_mins_left'));
        printLog('dbg', 'session_mins_left=', session_mins_left);
        if (session_mins_left > 0) {
            sessionTimerStart();
        }
        retCode = 'ok';
    }
    catch(e) {
        printLog('err', 'session_mins_left get Exception:', e);
        retCode = 'err';
    }
    return retCode;
} //func sessionTimerResume

function sessionExpiry() {
    printLog('err', 'sessionExpiry entered');

    //1. remove session info from localStorage
    localStorage.removeItem('session_duration');
    localStorage.removeItem('session_mins_left');
    localStorage.removeItem('session_start_date');
    localStorage.removeItem('session_start_time');
    localStorage.removeItem('session_end_date');
    localStorage.removeItem('session_end_time');

    //2. disable all pages
    localStorage.removeItem('session_id');

    //3. Update session fields
    updateSessionFields();

    //4. remove NodeID mac strings until get node info rpc is invoked
    nodeIdClearStorage();

    //5. refresh the page
    location.reload();
} //func SessionExpiry

function validateSession() {
    printLog('dbg', 'validateSession entered');
    try {
        session_id = localStorage.getItem('session_id');
        if (session_id == null) {
            printLog('err', 'invalid session');
            return 'err';
        }
        return 'ok';
    }
    catch(e) {
        printLog('err', 'invalid session Exception: ', e);
    }
} //func validateSession

function validateField(field_selector, field_name) {
    field_val = $(field_selector).val();
    if (! field_val) {
        alert('Please enter ' + field_name);
        return 'err';
    }
    printLog('dbg', field_name + '=', field_val);
    localStorage.setItem(field_name, field_val);
    return 'ok';
}

function validateInputFields(inputMapToValidate) {
    for (var [key, value] of inputMapToValidate.entries()) {
        retVal = validateField(value, key);
        if ('err' == retVal) {
            break;
        }
    }
    return retVal;
} //func validateInputFields

//generate serial number for table
function tableIndexGenerator(value, row, index) {
    return (index+1);
} //func tableIndexGenerator

//field_prefix is of type "#js-node-id-"
function macFieldConsolidateValue(field_prefix) {
    printLog('dbg', 'macFieldConsolidateValue entered for field_prefix=', field_prefix);
    var mac_val = "";
    for (i=1; i<=6; i++) {
        mac_val += $(field_prefix+i).val();
    }
    mac_dec = parseInt(mac_val, 16); //convert hex to dec
    printLog('dbg', 'mac_val='+mac_val, " mac_dec="+mac_dec);
    return mac_dec;
} //func macFieldConsolidateValue

function sessionLogoutBtnClicked() {
    printLog('dbg', 'sessionLogoutBtnClicked entered');
    if (validateSession() == 'err') {
        alert('Session does not exist.');
    }
    else {
        alert('Session terminated.');
        if (session_timer_g) {
            session_timer_g.stop();
        }
        customerDeregistrationTrigger();
    }
} //func sessionLogoutBtnClicked

function customerDeregistrationTrigger() {
    printLog('dbg', 'customerDeregistrationTrigger entered');
    if (validateSession() == 'err') {
        printLog('dbg', 'Session does not exist.');
    }
    else {
        printLog('dbg', 'Ending Session.');
        try {
            sessionId = localStorage.getItem("session_id"); //retain - to be used to invoke rpc
            sessionExpiry(); //this will also remove sessionID from localstorage
            custDeregnInvokeRpc(sessionId);
        }
        catch(e) {
            errTxt = 'RPC exception - ' + e;
            printLog('err', errTxt);
            populateHttpErrorText(errTxt);
        }
    } //else validateSession
} //func customerDeregistrationTrigger

function custDeregnInvokeRpc(sessionId) {
    printLog('dbg', 'custDeregnInvokeRpc entered for sessionId=', sessionId);
    var custDeregnRequestBody = {
        "input" : {
            "sessionId" : sessionId,
        }
    };

    if (isUtMode() == true) {
        ut_custDeregn_rpcStub();
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlCustDeregn"), custDeregnRequestBody, custDeregnParseResponseCallback, custDeregnErrorResponseCallback);
    }
} //func custDeregnInvokeRpc

function custDeregnErrorResponseCallback(jqXHR, textStatus, errorThrown) {
    printLog('dbg', 'custDeregnErrorResponseCallback entered');
    alert('Customer De-Registration rpc invoke exception: ' + textStatus + '\nCheck connectivity to NC');
    rpcErrorCallback(jqXHR, textStatus, errorThrown);
} //func custDeregnErrorResponseCallback

function custDeregnParseResponseCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'custDeregnParseResponseCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ', JSON.stringify(textStatus));

    custDeregnPopulateOutput(data);
} //func custDeregnParseResponseCallback

function custDeregnPopulateOutput(response) {
    printLog('dbg', 'custDeregnPopulateOutput entered');

    var custDeregnResponseCodeMap = new Map();
    custDeregnResponseCodeMap.set('NC_NBI_OK',                 'De-Registration Successful');
    custDeregnResponseCodeMap.set('NC_NBI_INVALID_SESSION_ID', 'Invalid Session ID');

    var result_string;
    try {
        response_code   = response.output.returnCode;
        result_text     = custDeregnResponseCodeMap.get(response_code);
        result_string   = 'response_code='+response_code + ', result_text='+result_text;
        printLog('info', result_string);
    }
    catch(e) {
        result_string = 'Parse error in response:<br/>' + JSON.stringify(response) + '<br/>' + e;
        printLog('err', 'exception: ', result_string);
        populateHttpErrorText(result_string);
    }
    alert('Customer De-Registration:\n' + result_string);
} //func custDeregnPopulateOutput

function ut_custDeregn_rpcStub() {
    printLog('dbg', 'ut_custDeregn_rpcStub entered');
    var response = {'output': {returnCode: 'NC_NBI_OK'}};
    // var response = {'output': {returnCode: 'NC_NBI_INVALID_SESSION_ID'}};
    custDeregnPopulateOutput(response);
} //func ut_custDeregn_rpcStub

function convertLongToMacString(longId, isSpacePaddingRequired="no") {
    // printLog('dbg', 'convertLongToMacString longId=', longId);
    decoratedMac = '"'; // quote it as a string
    if (isSpacePaddingRequired != "no") {
        decoratedMac += '&nbsp;&nbsp;&nbsp;&nbsp';  // spaces prepended too
    }
    decoratedMac += longId
                    .toString(16)               // convert to hex string
                    .padStart(12, "0")          // prepend with zeroes to make it exactly 12 characters (6 bytes in hex)
                    .replace(/..\B/g, '$&:')    // put a colon after every 2nd character
                    + '"';                      // quote it as a string
    // printLog('dbg', 'convertLongToMacString decoratedMac=', decoratedMac);
    return decoratedMac;
} //convertLongToMacString

function macStrToDec(mac_str) {
    printLog('dbg', 'macStrToDec entered for mac_str=', mac_str);
    mac_str = mac_str.replace(/:/g, "");
    mac_dec = parseInt(mac_str, 16); //convert hex to dec
    printLog('dbg', "mac_dec="+mac_dec);
    return mac_dec;
} //func macStrToDec

function nodeIdClearStorage() {
    printLog("dbg", "nodeIdClearStorage entered");
    localStorage.setItem("nonGwNodeMacStringList", ""); //dont remove item lest it will throw exceptions
} //func nodeIdClearStorage

/* when clicking sub-item, show that item as selected */
function nodeIdDropdownChangeItem() {
    $("#js-node-dropdown-menu li a").click(function(){
      $("#js-node-id").text($(this).text());
      $("#js-node-id").val($(this).text());
   });
} //func nodeIdDropdownChangeItem

function nodeIdPopulateDropdown() {
    printLog("dbg", "nodeIdPopulateDropdown entered");
    nodeIdMacHtml = "";
    nonGwNodeMacStringList = localStorage.getItem("nonGwNodeMacStringList");
    printLog("dbg", "current nonGwNodeMacStringList=", nonGwNodeMacStringList);
    macArray = nonGwNodeMacStringList.split(",");
    macArray.forEach(function(eachNodeMac) {
        nodeIdMacHtml += '<li><a href="#">' 
                            + eachNodeMac.replace(/"/g, '')  //remove double quotes
                            + '</a></li>';
    });
    $("#js-node-dropdown-menu").html(nodeIdMacHtml);
    nodeIdDropdownChangeItem();
} //func nodeIdPopulateDropdown

function disableField(fieldName, doMarkNA="no") {
    printLog("dbg", "disableField called for", fieldName);
    $(fieldName).prop("disabled", true);
    if (doMarkNA == "yes") {
        $(fieldName).val("N/A");
    }
} //func disableField

/******************************************************************************************************************
 ******************************************************************************************************************
                        BELOW CODE WILL NEVER BE HIT AS OF NOW - NOTIFICATIONS ARE FOR FUTURE SPRINT
 ******************************************************************************************************************
 *******************************************************************************************************************/
var wsNotification_g;
function wsInit() {
    printLog('dbg', 'wsInit entered');
    if (isUtMode() == true) {
        printLog('dbg', 'not opening notification ws in ut mode');
        ut_ws_stub();
    } // if UtMode
    else {
        try {
            wsUrl = 'ws://' + localStorage.getItem("controller_ip") + ':' 
                        + localStorage.getItem("controller_ws_port") + '/notifications';
            printLog('dbg', 'opening notification ws:', wsUrl);
            wsNotification_g = new WebSocket(wsUrl);
            wsNotification_g.onmessage = wsHandleNotification;
        }
        catch (e) {
            errTxt = 'notification ws creation exception - ' + e;
            printLog('err', errTxt);
        }
    } // else UtMode
} //func wsInit

function wsHandleNotification(evt) {
    printLog('dbg', 'wsHandleNotification: received data from ws server in event:'+ JSON.stringify(evt));
    wsProcessNotification(evt.data);
} //func wsHandleNotification

function ut_ws_stub() {
    printLog('dbg', 'ut_ws_stub entered');
    // var notificationData = {'type': 0, 'count': 1, 'idList' : [101, 102, 103], 'status': "created"}; //alerts raise
    // var notificationData = {'type': 0, 'count': 1, 'idList' : [101, 102], 'status': "deleted"}; //alerts clear
    var notificationData = {'type': 1, 'count': 1, 'idList' : [103]};                      //stats
    // var notificationData = {'type': 1, 'count': 2, 'idList' : [104, 105]};                 //stats
    wsProcessNotification(notificationData);
} //func ut_ws_stub

//polymorphic implementation harks
function wsProcessNotification(notificationData) {
    printLog('dbg', 'wsProcessNotification: data:', JSON.stringify(notificationData));
    switch (notificationData.type) {
        case 0: printLog('dbg', 'notification type: alerts');
                processAlertsNotification(notificationData);
                break;
        case 1: printLog('dbg', 'notification type: stats');
                processStatsNotification(notificationData);
                /*
                 *  if this is a stats notification
                 *      1. add it (localStorage) to pending stats list
                 *      2. now the stats pg can keep using this list (and clear it accordingly)
                 */
                break;
        default: printLog('err', 'notification type: unknown');
    } //switch notification type
} //func wsProcessNotification

/* check if this alerts notification is a create alert or delete alert
 *  1. if this is a create alert
 *      1.1 add it (localStorage) to pending alert list - only if it is not already there
 *      1.2 if alert was added to pending list, increment alert-used-up count
 *  2. if this is a delete alert
 *      2.1 delete it (localStorage) from pending alert list - there should be only one instance of it to be deleted
 *      2.2 if alert was deleted from pending list, increment alert-used-up count
 *  3. update alerts Icon number based upon alert-used-up count and type of alert
 * NOTE: alerts are stored in localStorage just to make sure we dont modify alert count for unknown / duplicate alert
 */
function processAlertsNotification(alertsJson) {
    alertsType      = alertsJson.status;
    alertsIdList    = alertsJson.idList;
    printLog('dbg', 'processAlertsNotification: alerts notification type:', alertsType);
    if ((alertsType == "created") || (alertsType == "deleted")) {
        try {
            // initialize if array returned is null
            pendingAlertsArray = JSON.parse(localStorage.getItem('pendingAlertsArray'));
            if (pendingAlertsArray == null) {
                pendingAlertsArray = [];   
            }
            printLog('dbg', 'current pending-alerts-list:', JSON.stringify(pendingAlertsArray));

            // forEach element in list, update list (create/delete) and return number of the elements actually updated
            numAlertsUsedUp = 0;
            alertsIdList.forEach(function(alertId) {
                printLog('dbg', 'processing for alertId:', alertId);
                currentIndex = pendingAlertsArray.indexOf(alertId);
                if (alertsType == "created") { //alert create
                    if (currentIndex == -1) {
                        pendingAlertsArray.push(alertId);
                        printLog('dbg', 'pending-alerts-list got a push:', JSON.stringify(pendingAlertsArray));
                        numAlertsUsedUp++;
                    }
                    else {
                        printLog('err', 'alertId '+alertId+' already exists, not adding');
                    }
                } //if created
                else if (alertsType == "deleted") {
                    if (currentIndex != -1) {
                        pendingAlertsArray.splice(currentIndex, 1);
                        printLog('dbg', 'got 1 splice, new pending-alerts-list:', JSON.stringify(pendingAlertsArray));
                        numAlertsUsedUp++;
                    }
                    else {
                        printLog('err', 'alertId '+alertId+' does not exists, cannot delete');
                    }
                } //else-if deleted
            }); //forEach alertId

            if (numAlertsUsedUp > 0) {
                // push the final array back into pendingAlertsArray
                printLog('dbg', 'updating localStorage with new pending-alerts-list:', JSON.stringify(pendingAlertsArray));
                localStorage.setItem('pendingAlertsArray', JSON.stringify(pendingAlertsArray));
                // now let the alerts fetch function use this list as per design

                printLog('dbg', 'new updates to icon: numAlertsUsedUp=', numAlertsUsedUp);
                updateAlertsIcon(alertsJson.count, alertsType); //refresh will happen from within this function
            }
            else {
                printLog('err', 'alerts pending list was not updated');
                return;
            } //else numAlertsUsedUp
        } //try
        catch (e) {
            printLog('err', 'updateAlertsNotificationList exception - ' + e);
        }
    } //if alertsType
    else {
        printLog('err', 'alerts notification type: unknown');
        return;
    } //else alertsType
} //func processAlertsNotification

/* Process the stats notification data and put all the stats in localStorage in pending-stats-list
 * This pending-stats-list will be used (and subsequently cleared) by the stats-monitoring function (that will run in a timeout loop)
 *
 * NOTE: no need of mutex between this function (that updates pending-stats-list; this runs as and when notification comes) and the function
 *       on stats page (that fetches that stats id and clears it from the pending-stats-list; this runs in setTimeout) because
 *       js on a single page will always use (single-threaded) event-loop to process both the functions so they would NEVER be processed
 *       in parallel.
 */
function processStatsNotification(statsJson) {
    printLog('dbg', 'processStatsNotification entered');
    try {
        // initialize if array returned is null
        pendingStatsArray = JSON.parse(localStorage.getItem('pendingStatsArray'));
        if (pendingStatsArray == null) {
            pendingStatsArray = [];
        }
        printLog('dbg', 'current pending-stats-list:', JSON.stringify(pendingStatsArray));

        statsJson.idList.forEach(function(sliceId) {
            printLog('dbg', 'check for sliceId:', sliceId);
            currentIndex = pendingStatsArray.indexOf(sliceId);
            if (currentIndex == -1) {
                pendingStatsArray.push(sliceId);
                printLog('dbg', 'pending-stats-list got a push:', JSON.stringify(pendingStatsArray));
            }
            else {
                printLog('err', 'sliceId '+sliceId+' already exists, not adding');
            }
        }); //forEach sliceId

        // push the final array back into pendingStatsArray
        printLog('dbg', 'updating localStorage with new pending-stats-list:', JSON.stringify(pendingStatsArray));
        localStorage.setItem('pendingStatsArray', JSON.stringify(pendingStatsArray));
        // now let the stats monitoring function use this list as per design
    } //try
    catch (e) {
        printLog('err', 'processStatsNotification exception - ' + e);
    }
} //func processStatsNotification
