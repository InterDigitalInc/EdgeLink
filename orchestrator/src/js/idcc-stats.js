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

$(document).ready(statsInit);

var stats_input_html =
'<form class="form-horizontal st-inputfields-shiftup">                                   ' +

'    <div class="form-group ">                                                           ' +
'        <div class="col-sm-offset-4 col-sm-2 badge st-form-label">Type</div>            ' +
'        <div class="col-sm-2">                                                          ' +
'            <input class="form-control st-form-value" id="js-type-stats" placeholder="0-All, 1-Single"> ' +
'        </div>                                                                          ' +
'    </div> <!-- end form-group -->                                                      ' +

'    <div class="form-group">                                                            ' +
'        <div class="col-sm-offset-4 col-sm-2 badge st-form-label">Slice ID</div>        ' +
'        <div class="col-sm-2">                                                          ' +
'            <input type="text" class="form-control st-form-value" id="js-slice-id" placeholder="Slice ID"> ' +
'        </div>                                                                          ' +
/******** FUTURE SPRINT *********
'       <div class="col-sm-4 col-lg-offset-2 col-lg-2 badge st-form-label" id="st-stats-monitor-badge">Statistics Monitoring</div>' +
*********************************/
'    </div> <!-- end form-group -->                                                      ' +

'    <div class="form-group">                                                            ' +
'        <div class="col-sm-offset-4 col-sm-4 col-lg-offset-5 col-lg-2">                 ' +
'            <button type="submit" id="js-getstats-btn" class="btn btn-info btn-block" href="#">Read Stats</button> ' +
'        </div>                                                                          ' +
/******** FUTURE SPRINT *********
'       <div class="col-sm-2 col-lg-offset-3 col-lg-1">                                  ' +
'           <div class="row">                                                            ' +
'               <div class="col-sm-11"><input type="text" id ="js-stats-monitoring-interval" class="form-control st-form-value" placeholder="0"></div> ' +
'               <div class="col-sm-1" id="st-stats-unit">sec</div>                       ' +
'           </div>  <!-- end row -->                                                     ' +
'       </div>                                                                           ' +

'       <label class="col-sm-2 col-lg-1 st-text-ellipsis" id="st-stats-chkbox-lbl">      ' +
'           <input type="checkbox" checked data-toggle="toggle" data-onstyle="info" id="js-sla-stats-chk"> ' +
'       </label>                                                                         ' +
*********************************/
'    </div> <!-- end form-group -->                                                      ' +
'</form> <!-- end form-horizontal -->'; //stats_input_html

var stats_output_html = 
'<div class="row st-stats-tbl" id="js-output-row">                                                          ' +
'   <table id="js-stats-tbl" data-toolbar="#js-btn-toolbar" data-search="true"                              ' +
'       data-pagination="true" data-page-size="10" data-page-list="[10,50,100]"                             ' +
'       data-classes="table table-hover table-condensed" data-striped="true">                               ' +
'        <thead>                                                                                            ' +
'            <tr>                                                                                           ' +
'                <th rowspan="3" data-formatter="tableIndexGenerator"> S.No. </th>                          ' +
'                <th rowspan="3" data-field="sliceId"    data-sortable="true"> Slice ID </th>               ' +
'                <th colspan="2"> Timestamp </th>                                                           ' +
'                <th colspan="15"> Statistics </th>                                                         ' +
'            </tr>                                                                                          ' +
'            <tr>                                                                                           ' +
'                <th rowspan="2" data-field="timestampStart"  data-sortable="true"> Start <br> (msec) </th> ' +
'                <th rowspan="2" data-field="timestampStop"  data-sortable="true"> Stop <br> (msec) </th>   ' +
'                <th colspan="5"> UL </th>                                                                  ' +
'                <th colspan="5"> DL </th>                                                                  ' +
'                <th colspan="3" class="text-muted"> Latency </th>                                                             ' +
'                <th rowspan="2" data-field="linkStats.jitter"  data-sortable="true" class="text-muted"> Jitter <br> (msec) </th> ' +
'                <th rowspan="2" data-field="linkStats.frameLossRatio"  data-sortable="true" class="text-muted"> FLR </th>     ' +
'            </tr>                                                                                          ' +
'            <tr>                                                                                           ' +
'                <th data-field="linkStats.throughputUl"  data-sortable="true"> Throughput <br> (Mbps) </th> ' +
'                <th data-field="linkStats.numDataBytesUl"  data-sortable="true"> Databytes </th>           ' +
'                <th data-field="linkStats.numGoodPktsUl"  data-sortable="true"> Good Pkts </th>            ' +
'                <th data-field="linkStats.numBadPktsUl"  data-sortable="true" class="text-muted"> Bad Pkts </th>              ' +
'                <th data-field="linkStats.numPktsDroppedUl"  data-sortable="true"> Dropped Pkts </th>      ' +
'                <th data-field="linkStats.throughputDl"  data-sortable="true"> Throughput <br> (Mbps) </th> ' +
'                <th data-field="linkStats.numDataBytesDl"  data-sortable="true"> Databytes </th>           ' +
'                <th data-field="linkStats.numGoodPktsDl"  data-sortable="true"> Good Pkts </th>            ' +
'                <th data-field="linkStats.numBadPktsDl"  data-sortable="true" class="text-muted"> Bad Pkts </th>              ' +
'                <th data-field="linkStats.numPktsDroppedDl"  data-sortable="true"> Dropped Pkts </th>      ' +
'                <th data-field="linkStats.latencyAvg"  data-sortable="true" class="text-muted"> Avg <br> (usec) </th>         ' +
'                <th data-field="linkStats.latencyMin"  data-sortable="true" class="text-muted"> Min <br> (usec) </th>         ' +
'                <th data-field="linkStats.latencyMax"  data-sortable="true" class="text-muted"> Max <br> (usec) </th>         ' +
'            </tr>                                                                                          ' +
'        </thead>                                                                                           ' +
'    </table>                                                                                               ' +
'</div>'; //stats_output_html

function statsInit() {
    printLog('dbg', 'statsInit entered');

    //1. initialize
    commonInit();
    $('#st-pg-title').html('Read Statistics');

    //2. make page active
    $('#js-pg-stats').addClass("active");

    //3. enable page if session is valid
    if (validateSession() == 'err') {
        printLog('err', 'session invalid. Customer registration required!!');
        $('#js-right-corepart').html('<div id="js-http-err">Customer registration required!!</div>');
        return 'err';
    }

    //4. set page content
    $('#js-input-params').html(stats_input_html);
    $('#js-output-params').html(stats_output_html);

    //5. enable event handlers
    $('#js-getstats-btn').on("click", statsBtnClick);

    //6. populate default input fields
    statsCreateInputVarsMap();
    localStorage.setItem("type", 0);
    localStorage.setItem("slice_id", 0);
    statsInputVarsMap.forEach(function(value, key) {
        $(value).val(localStorage.getItem(key));
    });

    //7. cleanout output fields
    cleanOutResult();

    //8. Create response code map
    statsCreateResponseCodeMap();

    //9. custom CSS
    $('#js-responsebar-row').addClass('st-responsebar-shiftup');

    //10. Call RPC on Pg load itself
    try {
        statsInvokeRpc();
    }
    catch(e) {
        errTxt = 'RPC exception - ' + e;
        printLog('err', errTxt);
        populateHttpErrorText(errTxt);
    }
} //func statsInit

// file-global map containing input parameters and corresponding html div placeholders
var statsInputVarsMap = new Map();
function statsCreateInputVarsMap() {
    printLog('dbg', 'statsCreateInputVarsMap entered');
    statsInputVarsMap.set('type', '#js-type-stats');
    statsInputVarsMap.set('slice_id', '#js-slice-id');
} //func statsCreateInputVarsMap

// file-global map containing output response code and corresponding interpretations
var statsResponseCodeMap = new Map();
function statsCreateResponseCodeMap() {
    printLog('dbg', 'statsCreateResponseCodeMap entered');
    statsResponseCodeMap.set('NC_NBI_OK',                 'Stats Fetched Successfully');
    statsResponseCodeMap.set('NC_NBI_INVALID_SESSION_ID', 'Invalid Session ID');
    statsResponseCodeMap.set('NC_NBI_INVALID_SLICE_ID',   'Invalid Slice ID');
} //func statsCreateResponseCodeMap

function statsBtnClick(evnt) {
    printLog('dbg', 'statsBtnClick entered');
    cleanOutResult();
    if ('ok' == validateInputFields(statsInputVarsMap)) {
        try {
            statsInvokeRpc();
        }
        catch(e) {
            errTxt = 'RPC exception - ' + e;
            printLog('err', errTxt);
            populateHttpErrorText(errTxt);
        }
    }
    evnt.preventDefault(); //avoid page reload
} //statsBtnClick

function statsInvokeRpc() {
    printLog('dbg', 'statsInvokeRpc entered');
    var statsRequestBody = {
        "input" : {
            "sessionId" : localStorage.getItem("session_id"),
            "type"      : localStorage.getItem("type"),
            "sliceId"   : localStorage.getItem("slice_id"),
        } //input
    }; //stats

    if (isUtMode() == true) {
        ut_stats_rpcStub(statsPopulateOutput);
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlGetStats"), statsRequestBody, statsParseResponseCallback);
    }
} //func statsInvokeRpc

function statsParseResponseCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'statsParseResponseCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ',        JSON.stringify(textStatus));

    statsPopulateOutput(data);
} //func statsParseResponseCallback

function statsPopulateOutput(response) {
    printLog('dbg', 'statsPopulateOutput entered');

    try{
        response_code       = response.output.returnCode;
        result_text = statsResponseCodeMap.get(response_code);
        printLog('info', 'response_code='+response_code, ', result_text='+result_text);
        $('#js-response-code').html(response_code);

        if (response_code == 'NC_NBI_OK') {
            numSlice = response.output.numSlice;
            printLog('info', 'numSlice', numSlice);
            localStorage.setItem("numSlice", numSlice);

            statsPopulateOutputTable(response.output.statsReportList);
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
} //func statsPopulateOutput

function statsPopulateOutputTable(statsReportList) {
    // printLog('dbg', 'statsPopulateOutputTable entered', 'statsReportList='+JSON.stringify(statsReportList));

    //1. process data - unsed fields marked NA
    statsReportList = statsProcessDataForDisplay(statsReportList);

    $('#js-stats-tbl').bootstrapTable('destroy');
    $('#js-stats-tbl').bootstrapTable({data: statsReportList});
} //func statsPopulateOutputTable

/*
 * (1) mark unused fields as NA
 * (2) limit precision to 3 decimal places
 */
function statsProcessDataForDisplay(unprocessedData) {
    printLog('dbg', "statsProcessDataForDisplay entered");

    processedData = [];
    unprocessedData.forEach(function(eachSlice) {
        dataString = JSON.stringify(eachSlice);

        //(1) mark unused fields
        dataString = dataString.replace(/\"numBadPktsUl\"\s*:\s*[\d]*,/g,  "\"numBadPktsUl\":\"N/A\",");
        dataString = dataString.replace(/\"numBadPktsDl\"\s*:\s*[\d]*,/g,  "\"numBadPktsDl\":\"N/A\",");
        dataString = dataString.replace(/\"latencyAvg\"\s*:\s*[\d]*,/g,  "\"latencyAvg\":\"N/A\",");
        dataString = dataString.replace(/\"latencyMin\"\s*:\s*[\d]*,/g,  "\"latencyMin\":\"N/A\",");
        dataString = dataString.replace(/\"latencyMax\"\s*:\s*[\d]*/g,  "\"latencyMax\":\"N/A\"");
        dataString = dataString.replace(/\"jitter\"\s*:\s*[\d]*,/g,  "\"jitter\":\"N/A\",");
        dataString = dataString.replace(/\"frameLossRatio\"\s*:\s*[\d]*/g,  "\"frameLossRatio\":\"N/A\"");

        //(2) limit precision to 3 decimal places
        dataString = dataString.replace(/(\"throughputUl\"\s*:\s*)(\d*.\d*e*-*\d*)(,)/g,
                                            function($0,$1,$2,$3) {return $0+$1+Number($2).toFixed(3).toString()+$3});
        dataString = dataString.replace(/(\"throughputDl\"\s*:\s*)(\d*.\d*e*-*\d*)(,)/g,
                                            function($0,$1,$2,$3) {return $0+$1+Number($2).toFixed(3).toString()+$3});

        processedData.push(JSON.parse(dataString));
    }); //forEach
    // printLog('dbg', "processedData="+JSON.stringify(processedData));

    return processedData;
} //func statsProcessDataForDisplay

function ut_stats_rpcStub(functionCallback) {
    printLog('dbg', 'ut_stats_rpcStub entered');

    var response_one_stats = {
        "output": {
            "returnCode": "NC_NBI_OK",
            // "returnCode": "NC_NBI_INVALID_SESSION_ID",
            // "returnCode": "NC_NBI_INVALID_STATS_ID",
            "numSlice": 1,
            "statsReportList": [
                {
                    "sliceId" : 5, "timestampStart" : 20201, "timestampStop" : 20301,
                    "linkStats" : {
                        "throughputUl" : 88.0129456789, "throughputDl" : 0.999123456789e1, "numDataBytesUl" : 110001110001, "numDataBytesDl" : 220001220001,
                        "numGoodPktsUl" : 33001, "numGoodPktsDl" : 44001, "numBadPktsUl" : 0, "numBadPktsDl" : 0,
                        "numPktsDroppedUl" : 9, "numPktsDroppedDl" : 8, "latencyAvg" : 0, "latencyMin" : 0, "latencyMax" : 0,
                        "jitter" : 0, "frameLossRatio" : 0,
                    } //linkStats
                },
                {"sliceId":4,"timestampStart":1504504421365,"timestampStop":1504504579377,"linkStats":{"throughputUl":0.00007548448418591972,"throughputUl":0.000,"throughputDl":3.642421237934065e-1,"numDataBytesUl":1563355292,"numDataBytesDl":75438,"numGoodPktsUl":1031237,"numGoodPktsDl":127,"numBadPktsUl":"N/A","numBadPktsDl":"N/A","numPktsDroppedUl":2646,"numPktsDroppedDl":1}}
            ] //statsReportList
        }}; //response_one_stats

    var response_all_stats = {
        "output": {
            "returnCode": "NC_NBI_OK",
            // "returnCode": "NC_NBI_INVALID_SESSION_ID",
            // "returnCode": "NC_NBI_INVALID_STATS_ID",
            "numSlice": 1,
            "statsReportList": [
                {
                    "sliceId" : 10101, "timestampStart" : 20201, "timestampStop" : 20301,
                    "linkStats" : {
                        "throughputUl" : 1101, "throughputDl" : 2201, "numDataBytesUl" : 110001, "numDataBytesDl" : 220001,
                        "numGoodPktsUl" : 33001, "numGoodPktsDl" : 44001, "numBadPktsUl" : 55001, "numBadPktsDl" : 66001,
                        "numPktsDroppedUl" : 77001, "numPktsDroppedDl" : 88001, "latencyAvg" : 101, "latencyMin" : 201, "latencyMax" : 301,
                        "jitter" : 401, "frameLossRatio" : 501,
                    } //linkStats
                },
                {
                    "sliceId" : 10102, "timestampStart" : 20202, "timestampStop" : 20302,
                    "linkStats" : {
                        "throughputUl" : 1102, "throughputDl" : 2202, "numDataBytesUl" : 110002, "numDataBytesDl" : 220002,
                        "numGoodPktsUl" : 33002, "numGoodPktsDl" : 44002, "numBadPktsUl" : 55002, "numBadPktsDl" : 66002,
                        "numPktsDroppedUl" : 77002, "numPktsDroppedDl" : 88002, "latencyAvg" : 102, "latencyMin" : 202, "latencyMax" : 302,
                        "jitter" : 402, "frameLossRatio" : 502,
                    } //linkStats
                },
                {
                    "sliceId" : 10103, "timestampStart" : 20203, "timestampStop" : 20303,
                    "linkStats" : {
                        "throughputUl" : 1103, "throughputDl" : 2203, "numDataBytesUl" : 110003, "numDataBytesDl" : 220003,
                        "numGoodPktsUl" : 33003, "numGoodPktsDl" : 44003, "numBadPktsUl" : 55003, "numBadPktsDl" : 66003,
                        "numPktsDroppedUl" : 77003, "numPktsDroppedDl" : 88003, "latencyAvg" : 103, "latencyMin" : 203, "latencyMax" : 303,
                        "jitter" : 403, "frameLossRatio" : 503,
                    } //linkStats
                },
                {
                    "sliceId" : 10104, "timestampStart" : 20204, "timestampStop" : 20304,
                    "linkStats" : {
                        "throughputUl" : 1104, "throughputDl" : 2204, "numDataBytesUl" : 110004, "numDataBytesDl" : 220004,
                        "numGoodPktsUl" : 33004, "numGoodPktsDl" : 44004, "numBadPktsUl" : 55004, "numBadPktsDl" : 66004,
                        "numPktsDroppedUl" : 77004, "numPktsDroppedDl" : 88004, "latencyAvg" : 104, "latencyMin" : 204, "latencyMax" : 304,
                        "jitter" : 404, "frameLossRatio" : 504,
                    } //linkStats
                },
                {
                    "sliceId" : 10105, "timestampStart" : 20205, "timestampStop" : 20305,
                    "linkStats" : {
                        "throughputUl" : 1105, "throughputDl" : 2205, "numDataBytesUl" : 110005, "numDataBytesDl" : 220005,
                        "numGoodPktsUl" : 33005, "numGoodPktsDl" : 44005, "numBadPktsUl" : 55005, "numBadPktsDl" : 66005,
                        "numPktsDroppedUl" : 77005, "numPktsDroppedDl" : 88005, "latencyAvg" : 105, "latencyMin" : 205, "latencyMax" : 305,
                        "jitter" : 405, "frameLossRatio" : 505,
                    } //linkStats
                },
                {
                    "sliceId" : 10106, "timestampStart" : 20206, "timestampStop" : 20306,
                    "linkStats" : {
                        "throughputUl" : 1106, "throughputDl" : 2206, "numDataBytesUl" : 110006, "numDataBytesDl" : 220006,
                        "numGoodPktsUl" : 33006, "numGoodPktsDl" : 44006, "numBadPktsUl" : 55006, "numBadPktsDl" : 66006,
                        "numPktsDroppedUl" : 77006, "numPktsDroppedDl" : 88006, "latencyAvg" : 106, "latencyMin" : 206, "latencyMax" : 306,
                        "jitter" : 406, "frameLossRatio" : 506,
                    } //linkStats
                },
                {
                    "sliceId" : 10107, "timestampStart" : 20207, "timestampStop" : 20307,
                    "linkStats" : {
                        "throughputUl" : 1107, "throughputDl" : 2207, "numDataBytesUl" : 110007, "numDataBytesDl" : 220007,
                        "numGoodPktsUl" : 33007, "numGoodPktsDl" : 44007, "numBadPktsUl" : 55007, "numBadPktsDl" : 66007,
                        "numPktsDroppedUl" : 77007, "numPktsDroppedDl" : 88007, "latencyAvg" : 107, "latencyMin" : 207, "latencyMax" : 307,
                        "jitter" : 407, "frameLossRatio" : 507,
                    } //linkStats
                },
                {
                    "sliceId" : 10108, "timestampStart" : 20208, "timestampStop" : 20308,
                    "linkStats" : {
                        "throughputUl" : 1108, "throughputDl" : 2208, "numDataBytesUl" : 110008, "numDataBytesDl" : 220008,
                        "numGoodPktsUl" : 33008, "numGoodPktsDl" : 44008, "numBadPktsUl" : 55008, "numBadPktsDl" : 66008,
                        "numPktsDroppedUl" : 77008, "numPktsDroppedDl" : 88008, "latencyAvg" : 108, "latencyMin" : 208, "latencyMax" : 308,
                        "jitter" : 408, "frameLossRatio" : 508,
                    } //linkStats
                },
                {
                    "sliceId" : 10109, "timestampStart" : 20209, "timestampStop" : 20309,
                    "linkStats" : {
                        "throughputUl" : 1109, "throughputDl" : 2209, "numDataBytesUl" : 110009, "numDataBytesDl" : 220009,
                        "numGoodPktsUl" : 33009, "numGoodPktsDl" : 44009, "numBadPktsUl" : 55009, "numBadPktsDl" : 66009,
                        "numPktsDroppedUl" : 77009, "numPktsDroppedDl" : 88009, "latencyAvg" : 109, "latencyMin" : 209, "latencyMax" : 309,
                        "jitter" : 409, "frameLossRatio" : 509,
                    } //linkStats
                },
                {
                    "sliceId" : 10110, "timestampStart" : 20210, "timestampStop" : 20310,
                    "linkStats" : {
                        "throughputUl" : 1110, "throughputDl" : 2210, "numDataBytesUl" : 110010, "numDataBytesDl" : 220010,
                        "numGoodPktsUl" : 33010, "numGoodPktsDl" : 44010, "numBadPktsUl" : 55010, "numBadPktsDl" : 66010,
                        "numPktsDroppedUl" : 77010, "numPktsDroppedDl" : 88010, "latencyAvg" : 110, "latencyMin" : 210, "latencyMax" : 310,
                        "jitter" : 410, "frameLossRatio" : 510,
                    } //linkStats
                },
                {
                    "sliceId" : 10111, "timestampStart" : 20211, "timestampStop" : 20311,
                    "linkStats" : {
                        "throughputUl" : 1111, "throughputDl" : 2211, "numDataBytesUl" : 110011, "numDataBytesDl" : 220011,
                        "numGoodPktsUl" : 33011, "numGoodPktsDl" : 44011, "numBadPktsUl" : 55011, "numBadPktsDl" : 66011,
                        "numPktsDroppedUl" : 77011, "numPktsDroppedDl" : 88011, "latencyAvg" : 111, "latencyMin" : 211, "latencyMax" : 311,
                        "jitter" : 411, "frameLossRatio" : 511,
                    } //linkStats
                },
            ] //statsReportList
        }}; //response_one_stats

    functionCallback(response_one_stats);
    // functionCallback(response_all_stats);

    $('#js-response-time').html('9999 msec');
} //func ut_stats_rpcStub

/******************************************************************************************************************
 ******************************************************************************************************************
                        BELOW CODE WILL NEVER BE HIT AS OF NOW - NOTIFICATIONS ARE FOR FUTURE SPRINT
 ******************************************************************************************************************
 *******************************************************************************************************************/
/* 1. start stats monitoring timer if stats monitoring checkbox is enabled
 * 2. close stats monitoring timer when
 *    2.a. stats pg is not active - since the timer is pg-local, when pg goes out of scope, timer will be destroyed anyway
 *    2.b. stats monitoring checkbox is disabled - event-trigger
 */
var stats_monitoring_timer_g; //scope: file global
function statsMonitoringTimerStart() {
    printLog('dbg', 'statsMonitoringTimerStart entered');

    stats_monitoring_interval = Number($("#js-stats-monitoring-interval").val());
    printLog('dbg', 'stats_monitoring_interval=', stats_monitoring_interval);

    stats_monitoring_timer_g = new Timer(); //scope: file global
    stats_monitoring_timer_g.start({
        countdown: true,
        precision: 'seconds',
            startValues: {seconds: stats_monitoring_interval}, //startValues means expiry for countdown timer
        });
    // stats_monitoring_timer_g.addEventListener('secondsUpdated', function (e) {
    //         // printLog('dbg', 'sessionTimerUpdateInterval timeLeft=', stats_monitoring_timer_g.getTimeValues().toString());
    //         stats_monitoring_interval = Number(stats_monitoring_timer_g.getTimeValues().toString(['seconds']));
    //         printLog('dbg', 'updating stats_monitoring_interval=', stats_monitoring_interval);
    //         localStorage.setItem('stats_monitoring_interval', stats_monitoring_interval);
    //     });
    stats_monitoring_timer_g.addEventListener('targetAchieved', statsMonitoringTimerExpired);
    printLog('dbg', 'stats_monitoring_timer_g created=', JSON.stringify(stats_monitoring_timer_g.getTotalTimeValues()));
} //func statsMonitoringTimerStart

/* On each stats-monitoring-timer expiry:
 * 1. compile a list of slice-ids for which stats needs to be fetched. The pending-stats-array contains sliceIds as populated by stats notifications
 * 2. One-by-one (its either one or all so we cant fetch many anyway) fetch updated stats for these compiled slices.
 * 3. Update that slice's stats info in the stats table on this page (without updating whole pg - is this possible?)
 * 4. start the stats monitoring timer again if checkbox is still enabled
 */
function statsMonitoringTimerExpired() {
    printLog('dbg', 'statsMonitoringTimerExpired entered');
    try {
        // initialize if array returned is null
        pendingStatsArray = JSON.parse(localStorage.getItem('pendingStatsArray'));
        if (pendingStatsArray == null) {
            printLog('dbg', 'no stats to be fetched');
            return;
        }
        copyOfPendingStatsArray = [...pendingStatsArray]; //spread operator - use this copy to find, delete in original
        printLog('dbg', 'current pending-stats-list:', JSON.stringify(copyOfPendingStatsArray));

        copyOfPendingStatsArray.forEach(function(sliceId) {
            printLog('dbg', 'check for sliceId:', sliceId);
            currentIndex = pendingStatsArray.indexOf(sliceId);
            if (currentIndex != -1) {
                pendingStatsArray.splice(currentIndex, 1);
                printLog('dbg', 'got 1 splice, new pending-stats-list:', JSON.stringify(pendingStatsArray));

                //fetch stats for this sliceId
                statsFetchAsPerNotification(sliceId);
            } //if currentIndex
            else {
                printLog('err', 'sliceId '+sliceId+' does not exists, next iter');
            }
        }); //forEach sliceId

        // push the final array back into pendingStatsArray
        printLog('dbg', 'updating localStorage with new pending-stats-list:', JSON.stringify(pendingStatsArray));
        localStorage.setItem('pendingStatsArray', JSON.stringify(pendingStatsArray));
    } //try
    catch (e) {
        printLog('err', 'statsMonitoringTimerExpired exception - ' + e);
    }
} //func statsMonitoringTimerExpired

function statsFetchAsPerNotification(sliceId) {
    printLog('dbg', 'statsFetchAsPerNotification entered for sliceId:', sliceId);
    var statsRequestBody = {
        "input" : {
            "sessionId" : localStorage.getItem("session_id"),
            "type"      : "1",
            "sliceId"   : sliceId,
        } //input
    }; //stats

    if (isUtMode() == true) {
        ut_stats_rpcStub(statsTableUpdateSingleRow);
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlGetStats"), statsRequestBody, statsUpdateSingleRowCallback);
    }
} //func statsFetchAsPerNotification

function statsUpdateSingleRowCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'statsUpdateSingleRowCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ',        JSON.stringify(textStatus));

    statsTableUpdateSingleRow(data);
} //func statsUpdateSingleRowCallback

function statsTableUpdateSingleRow(response) {
    printLog('dbg', 'statsTableUpdateSingleRow entered');

    try{
        response_code       = response.output.returnCode;
        result_text = statsResponseCodeMap.get(response_code);
        printLog('info', 'response_code='+response_code, ', result_text='+result_text);

        if (response_code == 'NC_NBI_OK') {
            printLog('dbg', 'update table row containing sliceId:', response.output.statsReportList[3].sliceId); //this SHOULD always be single slice
            statsUpdateTableInplace(response.output.statsReportList[3]);
        }
    }
    catch(e) {
        error_string = 'Parse error in response:<br/>' + JSON.stringify(response) + '<br/>' + e;
        printLog('err', 'exception: ', error_string);
    }
} //func statsTableUpdateSingleRow

function statsUpdateTableInplace(statsOfSlice) {
    printLog('dbg', 'statsUpdateTableInplace entered');
    var tableRow = $("td").filter(function() {
        return $(this).text() == statsOfSlice.sliceId;
    }).closest("tr");
    tableRow.css("background-color", "red");
    row_index = tableRow.index() + 1;
    printLog('dbg', "row_index=", row_index);
    oldStr = $(tableRow).html();
    printLog('dbg', 'tableRow data=', oldStr);

    newStr = oldStr
            .replace(/(\"timestampStart\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.timestampStart+9)
            .replace(/(\"timestampStop\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.timestampStop+9)

            .replace(/(\"throughputUl\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.throughputUl+99)
            .replace(/(\"numDataBytesUl\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.numDataBytesUl+99)
            .replace(/(\"numGoodPktsUl\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.numGoodPktsUl+99)
            .replace(/(\"numBadPktsUl\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.numBadPktsUl+99)
            .replace(/(\"numPktsDroppedUl\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.numPktsDroppedUl+99)

            .replace(/(\"throughputDl\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.throughputDl+999)
            .replace(/(\"numDataBytesDl\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.numDataBytesDl+999)
            .replace(/(\"numGoodPktsDl\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.numGoodPktsDl+999)
            .replace(/(\"numBadPktsDl\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.numBadPktsDl+999)
            .replace(/(\"numPktsDroppedDl\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.numPktsDroppedDl+999)

            .replace(/(\"latencyAvg\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.latencyAvg+9999)
            .replace(/(\"latencyMin\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.latencyMin+9999)
            .replace(/(\"latencyMax\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.latencyMax+9999)
            .replace(/(\"jitter\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.jitter+9999)
            .replace(/(\"frameLossRatio\"\s*:\s*)(\d*)/, '$1'+statsOfSlice.linkStats.frameLossRatio+9999);

    printLog('dbg', 'new row data:', newStr);
} //func statsUpdateTableInplace
