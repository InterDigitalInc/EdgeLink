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

$(document).ready(cntrlrConfigInit);

var cntrl_config_input_html =
'<form class="form-horizontal">                                                                     ' +
'    <div class="form-group">                                                                       ' +
'        <div class="col-sm-offset-3 col-sm-3 badge st-form-label"> Controller IP </div>            ' +
'        <div class="col-sm-4 col-md-3">                                                            ' +
'            <input id="js-controller-ip" type="text" class="form-control st-form-value" placeholder="controller ip">  ' +
'        </div>                                                                                     ' +
'    </div> <!-- end form-group -->                                                                 ' +

'    <div class="form-group">                                                                       ' +
'        <div class="col-sm-offset-3 col-sm-3 badge st-form-label"> Controller RPC Port </div>          ' +
'        <div class="col-sm-4 col-md-3">                                                            ' +
'            <input id="js-controller-rpc-port" type="text" class="form-control st-form-value" placeholder="rpc port"> ' +
'        </div>                                                                                     ' +
'    </div> <!-- end form-group -->                                                                 ' +

/******** FUTURE SPRINT *********/
/********************************
'    <div class="form-group">                                                                       ' +
'        <div class="col-sm-offset-3 col-sm-3 badge st-form-label"> Controller WS Port </div>          ' +
'        <div class="col-sm-4 col-md-3">                                                            ' +
'            <input id="js-controller-ws-port" type="text" class="form-control st-form-value" placeholder="ws port"> ' +
'        </div>                                                                                     ' +
'    </div> <!-- end form-group -->                                                                 ' +
********************************/

'    <div class="form-group">                                                                       ' +
'        <div class="col-sm-offset-5 col-sm-2">                                                     ' +
'            <button type="submit" id="js-save-ctrlr-btn" class="btn btn-info btn-block"> Save </button> ' +
'        </div>                                                                                     ' +
'    </div> <!-- end form-group -->                                                                 ' +
'</form> <!-- end form-horizontal -->'; //cntrl_config_html

var cntrl_config_output_html =
'<form class="form-horizontal" id="js-output-row">                                                  ' +
'    <div class="form-group">                                                                       ' +
'        <div class="col-sm-offset-2 col-sm-8" id="js-result-text"></div>                           ' +
'    </div> <!-- end form-group -->                                                                 ' +
'</form> <!-- output params -->'; //cntrl_config_output_html

function cntrlrConfigInit() {
    printLog('dbg', 'cntrlrConfigInit entered');

    //1. initialize
    commonInit();
    $('#st-pg-title').html('Configuration');

    //2. make page active
    $('#js-pg-cntrl-config').addClass("active");

    //3. enable page if session is valid
    //this page is always available - even out of session

    //4. set page content
    $('#js-input-params').html(cntrl_config_input_html);
    $('#js-output-params').html(cntrl_config_output_html);
    $('#js-responsebar-row').html(''); //No response code and time here
    $('#js-errow-row').html(''); //No scope of displaying error here

    //5. enable event handlers
    $('#js-save-ctrlr-btn').on("click", cntrlrConfigSaveBtnClicked);

    //6. populate default input fields
    cntrlrConfigCreateInputVarsMap();
    cntrlrConfigInputVarsMap.forEach(function(value, key) {
        $(value).val(localStorage.getItem(key));
    });

    //7. cleanout output fields
    cleanOutResult();
} //func cntrlrConfigInit

// file-global map containing input parameters and corresponding html div placeholders
var cntrlrConfigInputVarsMap = new Map();
function cntrlrConfigCreateInputVarsMap() {
    printLog('dbg', 'cntrlrConfigCreateInputVarsMap entered');
    cntrlrConfigInputVarsMap.set('controller_ip',   '#js-controller-ip');
    cntrlrConfigInputVarsMap.set('controller_rpc_port', '#js-controller-rpc-port');
    // /******** FUTURE SPRINT *********/ cntrlrConfigInputVarsMap.set('controller_ws_port', '#js-controller-ws-port');
} //func cntrlrConfigCreateInputVarsMap

function cntrlrConfigSaveBtnClicked(evnt) {
    printLog('dbg', 'cntrlrConfigSaveBtnClicked entered');
    cleanOutResult();
    if ('ok' == validateInputFields(cntrlrConfigInputVarsMap)) {
        populateResultText('Controller configuration saved');
    }
    evnt.preventDefault(); //avoid page reload
} //func cntrlrConfigSaveBtnClicked
