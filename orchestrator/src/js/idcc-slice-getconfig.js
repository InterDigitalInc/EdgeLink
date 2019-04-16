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

$(document).ready(sliceGetConfigInit);

var slice_getconfig_input_html =
'<form class="form-horizontal st-inputfields-shiftup">                                                      ' +

'    <div class="form-group ">                                                                              ' +
'        <div class="col-sm-offset-4 col-sm-2 badge st-form-label">Type</div>                               ' +
'        <div class="col-sm-2">                                                                             ' +
'            <input class="form-control st-form-value" id="js-type-slices" placeholder="0-All, 1-Single">     ' +
'        </div>                                                                                             ' +
'    </div> <!-- end form-group -->                                                                         ' +

'    <div class="form-group">                                                                               ' +
'        <div class="col-sm-offset-4 col-sm-2 badge st-form-label">Slice ID</div>                           ' +
'        <div class="col-sm-2">                                                                             ' +
'            <input type="text" class="form-control st-form-value" id="js-slice-id" placeholder="Slice ID"> ' +
'        </div>                                                                                             ' +
'    </div> <!-- end form-group -->                                                                         ' +

'    <div class="form-group">                                                                               ' +
'        <div class="col-sm-offset-4 col-sm-4 col-md-offset-5 col-md-2">                                    ' +
'            <button type="submit" id="js-getconfig-btn" class="btn btn-info btn-block" href="#">Get Config</button>' +
'        </div>                                                                                             ' +
'    </div> <!-- end form-group -->                                                                         ' +
'</form> <!-- end form-horizontal -->'; //slice_getconfig_input_html

var slice_getconfig_output_html = 
'<div class="row st-slice-tbl" id="js-output-row">                                  ' +
'   <div id="js-btn-toolbar" class="btn-group">                                     ' +
'       <button id="js-del-btn" type="button" class="btn btn-default" title="Delete Slice"> ' +
'           <i class="glyphicon glyphicon-trash"></i>                               ' +
'       </button>                                                                   ' +
'   </div>                                                                          ' +
'   <table id="js-slice-table" data-toolbar="#js-btn-toolbar" data-search="true"    ' +
'       data-click-to-select="true" data-single-select="true"                       ' +
'       data-pagination="true" data-page-size="10" data-page-list="[10,50,100]"     ' +
'       data-classes="table table-hover table-condensed" data-striped="true">       ' +
'        <thead>                                                                    ' +
'            <tr>                                                                   ' +
'                <th rowspan="3" data-field="state" data-checkbox="true"></th>      ' +
'                <th rowspan="3" data-formatter="tableIndexGenerator" class="st-sno-col">S.No.</th> ' +
'                <th rowspan="3" data-field="sliceId" data-sortable="true"> Slice ID </th> ' +
'                <th rowspan="3" data-field="poaId" data-sortable="true"> POA ID </th> ' +
'                <th colspan="18"> SLA </th>                                        ' +
'            </tr>                                                                  ' +
'            <tr>                                                                   ' +
'                <th rowspan="2" data-field="sla.priorityConfig.priority" data-sortable="true"> Priority </th> ' +
'                <th colspan="7"> UL </th>                                          ' +
'                <th colspan="7"> DL </th>                                          ' +
'                <th colspan="3" class="text-muted"> Statistics </th>                                  ' +
'            </tr>                                                                  ' +
'            <tr>                                                                   ' +
'                <th data-field="sla.bwReqUl.cir" data-sortable="true"> CIR <br> (Mbps) </th>       ' +
'                <th data-field="sla.bwReqUl.cbs" data-sortable="true"> CBS <br> (Kbytes) </th>     ' +
'                <th data-field="sla.bwReqUl.eir" data-sortable="true" class="text-muted"> EIR <br> (Mbps) </th>       ' +
'                <th data-field="sla.bwReqUl.ebs" data-sortable="true" class="text-muted"> EBS <br> (Kbytes) </th>     ' +
'                <th data-field="sla.bwReqUl.commMaxLatency" data-sortable="true" class="text-muted"> Latency <br> (usec) </th> ' +
'                <th data-field="sla.bwReqUl.commMaxJitter" data-sortable="true" class="text-muted"> Jitter <br> (msec) </th>   ' +
'                <th data-field="sla.bwReqUl.commMaxFlr" data-sortable="true" class="text-muted"> FLR </th>            ' +

'                <th data-field="sla.bwReqDl.cir" data-sortable="true"> CIR <br> (Mbps) </th>       ' +
'                <th data-field="sla.bwReqDl.cbs" data-sortable="true"> CBS <br> (Kbytes) </th>     ' +
'                <th data-field="sla.bwReqDl.eir" data-sortable="true" class="text-muted"> EIR <br> (Mbps) </th>       ' +
'                <th data-field="sla.bwReqDl.ebs" data-sortable="true" class="text-muted"> EBS <br> (Kbytes) </th>     ' +
'                <th data-field="sla.bwReqDl.commMaxLatency" data-sortable="true" class="text-muted"> Latency <br> (usec) </th> ' +
'                <th data-field="sla.bwReqDl.commMaxJitter" data-sortable="true" class="text-muted"> Jitter <br> (msec) </th>   ' +
'                <th data-field="sla.bwReqDl.commMaxFlr" data-sortable="true" class="text-muted"> FLR </th> ' +

'                <th data-field="sla.statsConfig.statsReporting" data-sortable="true" data-formatter="sliceGetConfigStatsReportFormatter" class="text-muted"> Stats <br> Report </th> ' +
'                <th data-field="sla.statsConfig.accumulationInterval" data-sortable="true" class="text-muted"> Accumulation <br/> Interval (sec) </th> ' +
'                <th data-field="sla.statsConfig.reportingInterval" data-sortable="true" class="text-muted"> Reporting <br/> Interval  (sec) </th> ' +
'            </tr>                                                                 ' +
'        </thead>                                                                  ' +
'    </table>                                                                      ' +
'</div>'; //slice_getconfig_output_html

function sliceGetConfigInit() {
    printLog('dbg', 'sliceGetConfigInit entered');

    //1. initialize
    commonInit();
    $('#st-pg-title').html('Get Slice Config');

    //2. make page active
    $('#js-pg-slice-config').addClass("active");

    //3. enable page if session is valid
    if (validateSession() == 'err') {
        printLog('err', 'session invalid. Customer registration required!!');
        $('#js-right-corepart').html('<div id="js-http-err">Customer registration required!!</div>');
        return 'err';
    }

    //4. set page content
    $('#js-input-params').html(slice_getconfig_input_html);
    $('#js-output-params').html(slice_getconfig_output_html);

    //5. enable event handlers
    $('#js-getconfig-btn').on("click", sliceGetConfigBtnClick);
    $('#js-slice-delete-rpc-btn').on("click", sliceDeleteInvokeRpc);

    //6. populate default input fields
    sliceGetConfigCreateInputVarsMap();
    localStorage.setItem("type_list", 0);
    localStorage.setItem("slice_id", 0);
    sliceGetConfigInputVarsMap.forEach(function(value, key) {
        $(value).val(localStorage.getItem(key));
    });

    //7. cleanout output fields
    cleanOutResult();

    //8. Create response code map
    sliceGetConfigCreateResponseCodeMap();
    sliceDeleteCreateResponseCodeMap();

    //9. custom CSS
    $('#js-responsebar-row').addClass('st-responsebar-shiftup');

    //10. Call RPC on Pg load itself
    try {
        sliceGetConfigInvokeRpc();
    }
    catch(e) {
        errTxt = 'RPC exception - ' + e;
        printLog('err', errTxt);
        populateHttpErrorText(errTxt);
    }
} //func sliceGetConfigInit

// file-global map containing input parameters and corresponding html div placeholders
var sliceGetConfigInputVarsMap = new Map();
function sliceGetConfigCreateInputVarsMap() {
    printLog('dbg', 'sliceGetConfigCreateInputVarsMap entered');
    sliceGetConfigInputVarsMap.set('type_list', '#js-type-slices');
    sliceGetConfigInputVarsMap.set('slice_id', '#js-slice-id');
} //func sliceGetConfigCreateInputVarsMap

// file-global map containing output response code and corresponding interpretations
var sliceGetConfigResponseCodeMap = new Map();
function sliceGetConfigCreateResponseCodeMap() {
    printLog('dbg', 'sliceGetConfigCreateResponseCodeMap entered');
    sliceGetConfigResponseCodeMap.set('NC_NBI_OK',                 'Slice Info fetched Successfully');
    sliceGetConfigResponseCodeMap.set('NC_NBI_INVALID_SESSION_ID', 'Invalid Session ID');
    sliceGetConfigResponseCodeMap.set('NC_NBI_INVALID_SLICE_ID',   'Invalid Slice ID');
} //func sliceGetConfigCreateResponseCodeMap

function sliceGetConfigBtnClick(evnt) {
    printLog('dbg', 'sliceGetConfigBtnClick entered');
    cleanOutResult();
    if ('ok' == validateInputFields(sliceGetConfigInputVarsMap)) {
        try {
            sliceGetConfigInvokeRpc();
        }
        catch(e) {
            errTxt = 'RPC exception - ' + e;
            printLog('err', errTxt);
            populateHttpErrorText(errTxt);
        }
    }
    evnt.preventDefault(); //avoid page reload
} //sliceGetConfigBtnClick

function sliceGetConfigInvokeRpc() {
    printLog('dbg', 'sliceGetConfigInvokeRpc entered');
    var sliceGetConfigRequestBody = {
        "input" : {
            "sessionId" : localStorage.getItem("session_id"),
            "type"      : localStorage.getItem("type_list"),
            "sliceId"   : localStorage.getItem("slice_id"),
        } //input
    }; //sliceGetConfig

    if (isUtMode() == true) {
        ut_sliceGetConfig_rpcStub();
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlSliceGetConfig"), sliceGetConfigRequestBody, sliceGetConfigParseResponseCallback);
    }
} //func sliceGetConfigInvokeRpc

function sliceGetConfigParseResponseCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'sliceGetConfigParseResponseCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ',        JSON.stringify(textStatus));

    sliceGetConfigPopulateOutput(data);
} //func sliceGetConfigParseResponseCallback

function sliceGetConfigPopulateOutput(response) {
    printLog('dbg', 'sliceGetConfigPopulateOutput entered');

    try{
        response_code       = response.output.returnCode;
        result_text = sliceGetConfigResponseCodeMap.get(response_code);
        printLog('info', 'response_code='+response_code, ', result_text='+result_text);
        $('#js-response-code').html(response_code);

        if (response_code == 'NC_NBI_OK') {
            num_slices = response.output.numSlice;
            printLog('info', 'num_slices', num_slices);
            localStorage.setItem("num_slices", num_slices);

            sliceGetConfigPopulateOutputTable(response.output.sliceConfigList);
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
} //func sliceGetConfigPopulateOutput

function sliceGetConfigPopulateOutputTable(sliceConfigList) {
    printLog('dbg', 'sliceGetConfigPopulateOutputTable entered');

    //1. process data - convert POA from long value to mac id
    sliceConfigList = sliceGetConfigProcessDataForDisplay(sliceConfigList);

    $('#js-slice-table').bootstrapTable('destroy');
    $('#js-slice-table').bootstrapTable({data: sliceConfigList});

    //disable the delete slice btn
    $('#js-del-btn').prop('disabled', true);

    //event - delete slice btn clicked
    $('#js-del-btn').click(sliceDeleteBtnClicked);

    //event - row selected
    $('#js-slice-table').on('check.bs.table', function (e, row, $element) {
      localStorage.setItem('slice_id_to_delete', row.sliceId);
  });

    //event - when row is selected/deselected
    $('#js-slice-table').on('check.bs.table uncheck.bs.table', function () {
        //enable\disable the delete slice btn
        $('#js-del-btn').prop('disabled', !$('#js-slice-table').bootstrapTable('getSelections').length);

        // save your data, here just save the current page
        selections = sliceGetConfigGetIdSelections();
    });
} //func sliceGetConfigPopulateOutputTable

function sliceGetConfigGetIdSelections() {
    return $.map($('#js-slice-table').bootstrapTable('getSelections'), function (row) {
        return row.sliceId;
    });
} //func sliceGetConfigGetIdSelections

function sliceGetConfigStatsReportFormatter(value, row) {
    printLog("sliceGetConfigStatsReportFormatter unused as of now");
    return value;
/* uncomment this when statsReporting works from NC side
 *
    showVal = (value == 1)?'ON':'OFF';
    return showVal;
 *
 */
} //func sliceGetConfigStatsReportFormatter

function sliceGetConfigProcessDataForDisplay(unprocessedData) {
    printLog('dbg', "sliceGetConfigProcessDataForDisplay entered");

    processedData = [];
    unprocessedData.forEach(function(eachNode) {
        dataString = JSON.stringify(eachNode);

        // poaId
        decoratedMac = convertLongToMacString(eachNode.poaId);
        dataString = dataString.replace(/(\"poaId\"\s*:\s*)(\d*)/, '$1'+decoratedMac);

        //mark unused fields
        dataString = dataString.replace(/\"eir\"\s*:\s*[\d]*,/g,  "\"eir\":\"N/A\",");
        dataString = dataString.replace(/\"ebs\"\s*:\s*[\d]*,/g,  "\"ebs\":\"N/A\",");
        dataString = dataString.replace(/\"commMaxLatency\"\s*:\s*[\d]*,/g,  "\"commMaxLatency\":\"N/A\",");
        dataString = dataString.replace(/\"commMaxJitter\"\s*:\s*[\d]*,/g,  "\"commMaxJitter\":\"N/A\",");
        dataString = dataString.replace(/\"commMaxFlr\"\s*:\s*[\d]*/g,  "\"commMaxFlr\":\"N/A\"");
        dataString = dataString.replace(/\"statsReporting\"\s*:\s*[\d]*,/g,  "\"statsReporting\":\"N/A\",");
        dataString = dataString.replace(/\"accumulationInterval\"\s*:\s*[\d]*,/g,  "\"accumulationInterval\":\"N/A\",");
        dataString = dataString.replace(/\"reportingInterval\"\s*:\s*[\d]*/g,  "\"reportingInterval\":\"N/A\"");


        processedData.push(JSON.parse(dataString));
    }); //forEach
    // printLog('dbg', "processedData="+JSON.stringify(processedData));

    return processedData;
} //func sliceGetConfigProcessDataForDisplay

function ut_sliceGetConfig_rpcStub() {
    printLog('dbg', 'ut_sliceGetConfig_rpcStub entered');

    var response_one_slice = {
        'output': {
            "returnCode": "NC_NBI_OK",
            // "returnCode": "NC_NBI_INVALID_SESSION_ID",
            // "returnCode": "NC_NBI_INVALID_SLICE_ID",
            "numSlice": 1,
            "sliceConfigList": [
            {
                "sliceId": 1234,
                "poaId": 8963798073620,
                "sla": {
                    "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                    "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                    "priorityConfig": {"priority": 55 },
                    "statsConfig": {"statsReporting": 61, "accumulationInterval": 62, "reportingInterval": 63 }
                }
            }
            ] //sliceConfigList
        }}; //response_one_slice

        var response_all_slices = {
            "output": {
                "returnCode": "NC_NBI_OK",
            // "returnCode": "NC_NBI_INVALID_SESSION_ID",
            // "returnCode": "NC_NBI_INVALID_SLICE_ID",
            "numSlice": 2,
            "sliceConfigList": [
            {
                "sliceId": 101,
                "poaId": 8963798073877,
                "sla": {
                    "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                    "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                    "priorityConfig": {"priority": 51 },
                    "statsConfig": {"statsReporting": 1, "accumulationInterval": 62, "reportingInterval": 63 }
                }
            },
            {
                "sliceId": 102,
                "poaId": 8963798073887,
                "sla": {
                  "bwReqUl": {"cir": 71, "cbs": 72, "eir": 73, "ebs": 74, "commMaxLatency": 75, "commMaxJitter": 76, "commMaxFlr": 77},
                  "bwReqDl": {"cir": 81, "cbs": 82, "eir": 83, "ebs": 84, "commMaxLatency": 85, "commMaxJitter": 86, "commMaxFlr": 87},
                  "priorityConfig": {"priority": 52 },
                  "statsConfig": {"statsReporting": 0, "accumulationInterval": 64, "reportingInterval": 65 }
              }
          },
          {
            "sliceId": 101,
            "poaId": 8963798073877,
            "sla": {
                "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                "priorityConfig": {"priority": 51 },
                "statsConfig": {"statsReporting": 1, "accumulationInterval": 62, "reportingInterval": 63 }
            }
        },
        {
            "sliceId": 102,
            "poaId": 8963798073887,
            "sla": {
              "bwReqUl": {"cir": 71, "cbs": 72, "eir": 73, "ebs": 74, "commMaxLatency": 75, "commMaxJitter": 76, "commMaxFlr": 77},
              "bwReqDl": {"cir": 81, "cbs": 82, "eir": 83, "ebs": 84, "commMaxLatency": 85, "commMaxJitter": 86, "commMaxFlr": 87},
              "priorityConfig": {"priority": 52 },
              "statsConfig": {"statsReporting": 0, "accumulationInterval": 64, "reportingInterval": 65 }
          }
        },
        {
            "sliceId": 101,
            "poaId": 8963798073877,
            "sla": {
                "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                "priorityConfig": {"priority": 51 },
                "statsConfig": {"statsReporting": 1, "accumulationInterval": 62, "reportingInterval": 63 }
            }
        },
        {
            "sliceId": 102,
            "poaId": 8963798073887,
            "sla": {
              "bwReqUl": {"cir": 71, "cbs": 72, "eir": 73, "ebs": 74, "commMaxLatency": 75, "commMaxJitter": 76, "commMaxFlr": 77},
              "bwReqDl": {"cir": 81, "cbs": 82, "eir": 83, "ebs": 84, "commMaxLatency": 85, "commMaxJitter": 86, "commMaxFlr": 87},
              "priorityConfig": {"priority": 52 },
              "statsConfig": {"statsReporting": 0, "accumulationInterval": 64, "reportingInterval": 65 }
            }
        },
        {
            "sliceId": 101,
            "poaId": 8963798073877,
            "sla": {
                "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                "priorityConfig": {"priority": 51 },
                "statsConfig": {"statsReporting": 1, "accumulationInterval": 62, "reportingInterval": 63 }
            }
        },
        {
            "sliceId": 102,
            "poaId": 8963798073887,
            "sla": {
              "bwReqUl": {"cir": 71, "cbs": 72, "eir": 73, "ebs": 74, "commMaxLatency": 75, "commMaxJitter": 76, "commMaxFlr": 77},
              "bwReqDl": {"cir": 81, "cbs": 82, "eir": 83, "ebs": 84, "commMaxLatency": 85, "commMaxJitter": 86, "commMaxFlr": 87},
              "priorityConfig": {"priority": 52 },
              "statsConfig": {"statsReporting": 0, "accumulationInterval": 64, "reportingInterval": 65 }
          }
        },
        {
            "sliceId": 101,
            "poaId": 8963798073877,
            "sla": {
                "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                "priorityConfig": {"priority": 51 },
                "statsConfig": {"statsReporting": 1, "accumulationInterval": 62, "reportingInterval": 63 }
            }
        },
        {
            "sliceId": 102,
            "poaId": 8963798073887,
            "sla": {
              "bwReqUl": {"cir": 71, "cbs": 72, "eir": 73, "ebs": 74, "commMaxLatency": 75, "commMaxJitter": 76, "commMaxFlr": 77},
              "bwReqDl": {"cir": 81, "cbs": 82, "eir": 83, "ebs": 84, "commMaxLatency": 85, "commMaxJitter": 86, "commMaxFlr": 87},
              "priorityConfig": {"priority": 52 },
              "statsConfig": {"statsReporting": 0, "accumulationInterval": 64, "reportingInterval": 65 }
          }
        },
        {
            "sliceId": 101,
            "poaId": 8963798073877,
            "sla": {
                "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                "priorityConfig": {"priority": 51 },
                "statsConfig": {"statsReporting": 1, "accumulationInterval": 62, "reportingInterval": 63 }
            }
        },
        {
            "sliceId": 102,
            "poaId": 8963798073887,
            "sla": {
              "bwReqUl": {"cir": 71, "cbs": 72, "eir": 73, "ebs": 74, "commMaxLatency": 75, "commMaxJitter": 76, "commMaxFlr": 77},
              "bwReqDl": {"cir": 81, "cbs": 82, "eir": 83, "ebs": 84, "commMaxLatency": 85, "commMaxJitter": 86, "commMaxFlr": 87},
              "priorityConfig": {"priority": 52 },
              "statsConfig": {"statsReporting": 0, "accumulationInterval": 64, "reportingInterval": 65 }
          }
        },
        {
            "sliceId": 101,
            "poaId": 8963798073877,
            "sla": {
                "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                "priorityConfig": {"priority": 51 },
                "statsConfig": {"statsReporting": 1, "accumulationInterval": 62, "reportingInterval": 63 }
            }
        },
        {
            "sliceId": 102,
            "poaId": 8963798073887,
            "sla": {
              "bwReqUl": {"cir": 71, "cbs": 72, "eir": 73, "ebs": 74, "commMaxLatency": 75, "commMaxJitter": 76, "commMaxFlr": 77},
              "bwReqDl": {"cir": 81, "cbs": 82, "eir": 83, "ebs": 84, "commMaxLatency": 85, "commMaxJitter": 86, "commMaxFlr": 87},
              "priorityConfig": {"priority": 52 },
              "statsConfig": {"statsReporting": 0, "accumulationInterval": 64, "reportingInterval": 65 }
          }
        },
        {
            "sliceId": 101,
            "poaId": 8963798073877,
            "sla": {
                "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                "priorityConfig": {"priority": 51 },
                "statsConfig": {"statsReporting": 1, "accumulationInterval": 62, "reportingInterval": 63 }
            }
        },
        {
            "sliceId": 102,
            "poaId": 8963798073887,
            "sla": {
              "bwReqUl": {"cir": 71, "cbs": 72, "eir": 73, "ebs": 74, "commMaxLatency": 75, "commMaxJitter": 76, "commMaxFlr": 77},
              "bwReqDl": {"cir": 81, "cbs": 82, "eir": 83, "ebs": 84, "commMaxLatency": 85, "commMaxJitter": 86, "commMaxFlr": 87},
              "priorityConfig": {"priority": 52 },
              "statsConfig": {"statsReporting": 0, "accumulationInterval": 64, "reportingInterval": 65 }
          }
        },
        {
            "sliceId": 101,
            "poaId": 8963798073877,
            "sla": {
                "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                "priorityConfig": {"priority": 51 },
                "statsConfig": {"statsReporting": 1, "accumulationInterval": 62, "reportingInterval": 63 }
            }
        },
        {
            "sliceId": 102,
            "poaId": 8963798073887,
            "sla": {
              "bwReqUl": {"cir": 71, "cbs": 72, "eir": 73, "ebs": 74, "commMaxLatency": 75, "commMaxJitter": 76, "commMaxFlr": 77},
              "bwReqDl": {"cir": 81, "cbs": 82, "eir": 83, "ebs": 84, "commMaxLatency": 85, "commMaxJitter": 86, "commMaxFlr": 87},
              "priorityConfig": {"priority": 52 },
              "statsConfig": {"statsReporting": 0, "accumulationInterval": 64, "reportingInterval": 65 }
          }
        },
        {
            "sliceId": 101,
            "poaId": 8963798073877,
            "sla": {
                "bwReqUl": {"cir": 31, "cbs": 32, "eir": 33, "ebs": 34, "commMaxLatency": 35, "commMaxJitter": 36, "commMaxFlr": 37 },
                "bwReqDl": {"cir": 41, "cbs": 42, "eir": 43, "ebs": 44, "commMaxLatency": 45, "commMaxJitter": 46, "commMaxFlr": 47 },
                "priorityConfig": {"priority": 51 },
                "statsConfig": {"statsReporting": 1, "accumulationInterval": 62, "reportingInterval": 63 }
            }
        },
        {
            "sliceId": 102,
            "poaId": 8963798073887,
            "sla": {
              "bwReqUl": {"cir": 71, "cbs": 72, "eir": 73, "ebs": 74, "commMaxLatency": 75, "commMaxJitter": 76, "commMaxFlr": 77},
              "bwReqDl": {"cir": 81, "cbs": 82, "eir": 83, "ebs": 84, "commMaxLatency": 85, "commMaxJitter": 86, "commMaxFlr": 87},
              "priorityConfig": {"priority": 52 },
              "statsConfig": {"statsReporting": 0, "accumulationInterval": 64, "reportingInterval": 65 }
          }
        },
            ] //sliceConfigList
        }}; //response_all_slices

    // sliceGetConfigPopulateOutput(response_one_slice);
    sliceGetConfigPopulateOutput(response_all_slices);

    $('#js-response-time').html('9999 msec');
} //func ut_sliceGetConfig_rpcStub
