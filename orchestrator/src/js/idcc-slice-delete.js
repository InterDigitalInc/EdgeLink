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

// file-global map containing output response code and corresponding interpretations
var sliceDeleteResponseCodeMap = new Map();
function sliceDeleteCreateResponseCodeMap() {
    printLog('dbg', 'sliceDeleteCreateResponseCodeMap entered');
    sliceDeleteResponseCodeMap.set('NC_NBI_OK',                 'Slice Deleted Successfully');
    sliceDeleteResponseCodeMap.set('NC_NBI_INVALID_SESSION_ID', 'Invalid Session ID');
    sliceDeleteResponseCodeMap.set('NC_NBI_INVALID_SLICE_ID',   'Invalid Slice ID');
} //func sliceDeleteCreateResponseCodeMap

function sliceDeleteBtnClicked() {
    printLog('dbg', 'sliceDeleteBtnClicked entered');

    // var index = $('#js-slice-table').find('tr.selected').data('index');
    // console.log('row to delete=', index+1);
    slice_id_to_delete = localStorage.getItem('slice_id_to_delete');
    
    printLog('dbg', 'slice_id_to_delete=', slice_id_to_delete);
    localStorage.setItem('slice_id_to_delete', slice_id_to_delete);
    $('#js-modal-body-text').html('Delete Slice ID '+localStorage.getItem('slice_id_to_delete')+'?');
    $('#sliceDeleteModal').modal();
} //func sliceDeleteBtnClicked

function sliceDeleteInvokeRpc() {
    printLog('dbg', 'sliceDeleteInvokeRpc entered');
    $('#sliceDeleteModal').modal('hide'); //hide the model - this call not immediate
    var sliceDeleteRequestBody = {
        "input" : {
            "sessionId" : localStorage.getItem("session_id"),
            "sliceId"   : localStorage.getItem('slice_id_to_delete'),
        } //input
    }; //sliceDelete

    if (isUtMode() == true) {
        ut_sliceDelete_rpcStub();
    }
    else {
        callRpc("POST", localStorage.getItem("rpcUrlSliceDelete"), sliceDeleteRequestBody, sliceDeleteParseResponseCallback);
    }
} //func sliceDeleteInvokeRpc

function sliceDeleteParseResponseCallback(data, textStatus, jqXHR) {
    printLog('dbg', 'sliceDeleteParseResponseCallback entered');
    printLog('dbg', 'full response: ', JSON.stringify(jqXHR));
    printLog('dbg', 'response text: ', JSON.stringify(data));
    printLog('dbg', 'status: ',        JSON.stringify(textStatus));

    sliceDeletePopulateOutput(data);
} //func sliceDeleteParseResponseCallback

function sliceDeletePopulateOutput(response) {
    printLog('dbg', 'sliceDeletePopulateOutput entered');

    try{
        response_code       = response.output.returnCode;
        result_text = sliceDeleteResponseCodeMap.get(response_code);
        printLog('info', 'response_code='+response_code, ', result_text='+result_text);
        $('#js-response-code').html(response_code);

        if (response_code == 'NC_NBI_OK') {
            var sliceIds = sliceGetConfigGetIdSelections();
            $('#js-slice-table').bootstrapTable('remove', {
                field: 'sliceId',
                values: sliceIds
            });

            //disable the delete slice btn
            $('#js-del-btn').prop('disabled', true);
            result_text = 'Slice ID '+ localStorage.getItem('slice_id_to_delete') +' Deleted Successfully';
            localStorage.removeItem('slice_id_to_delete');
        }
        //no else block needed as we will generate the popup at the end of this func
    }
    catch(e) {
        result_text = 'Parse error in response:<br/>' + JSON.stringify(response) + '<br/>' + e;
        printLog('err', 'exception: ', result_text);
    }
    if (! result_text) {
        result_text = response_code;
    }
    alert(result_text);
} //func sliceDeletePopulateOutput

function ut_sliceDelete_rpcStub() {
    printLog('dbg', 'ut_sliceDelete_rpcStub entered');
    var response = {'output': {"returnCode": "NC_NBI_OK"}};
    // var response = {'output': {"returnCode": "NC_NBI_INVALID_SESSION_ID"}};
    // var response = {'output': {"returnCode": "NC_NBI_INVALID_SLICE_ID"}};
    sliceDeletePopulateOutput(response);
    $('#js-response-time').html('9999 msec');
} //func ut_sliceDelete_rpcStub
