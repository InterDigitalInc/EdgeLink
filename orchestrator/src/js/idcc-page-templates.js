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

var header_html =
'<nav class="navbar navbar-fixed-top st-white-on-color" id="st-header">                                                      ' +
'    <div class="container-fluid"> <!-- header -->                                                                           ' +
'        <div class="row">                                                                                                   ' +

'            <div class="col-sm-3 col-md-3 col-lg-2" id="st-logo-div"> <!-- IDCC logo -->                                    ' +
'                <a href="http://www.interdigital.com"><img src="img/idcc-logo.png" alt="InterDigital"></a>                       ' +
'            </div> <!-- IDCC logo -->                                                                                       ' +

'            <div class="col-sm-7 col-md-7 col-lg-8"> <!-- session start and end time-->                                     ' +
'                <div class="container-fluid">                                                                               ' +

'                    <div class="col-sm-1" id="st-toggle-btn">                                           ' +
'                        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#st-sidebar" id="id-sidebar-btn"> ' +
'                            <span class="icon-bar"></span>                                                                  ' +
'                            <span class="icon-bar"></span>                                                                  ' +
'                            <span class="icon-bar"></span>                                                                  ' +
'                        </button>                                                                                           ' +
'                    </div>                                                                                                  ' +

'                    <div class="hidden-sm col-md-2 label st-text-ellipsis st-sessionTime-label">Session Start</div>         ' +
'                    <div id="js-session-start" class="col-sm-4 col-md-3 badge st-color-on-white st-text-ellipsis st-sessionTime-value"></div> ' +
'                    <div class="col-sm-1 hidden-md">&nbsp;</div>                                                            ' +
'                    <div class="hidden-sm col-md-2 label st-text-ellipsis st-sessionTime-label ">Session End</div>          ' +
'                    <div id="js-session-end"   class="col-sm-4 col-md-3 badge st-color-on-white st-text-ellipsis st-sessionTime-value"></div> ' +

'                </div> <!-- end container-fluid of session start and end time -->                                           ' +
'            </div>  <!-- session start and end time-->                                                                      ' +

'            <a href="#" class="st-header-icon">                                                                             ' +
'                <div class="col-sm-1 col-md-1 col-lg-1 notification-icon" title="Alerts">                                   ' +
'                       <span class="glyphicon glyphicon-bell"></span>                                                       ' +
'                       <span class="badge" id="jt-alarm-count"></span>                                                      ' +
'                </div>                                                                                                      ' +
'            </a>                                                                                                            ' +

'            <a href="#" onclick="sessionLogoutBtnClicked()" class="st-header-icon">                                         ' +
'                <div class="col-sm-1 col-md-1 col-lg-1 glyphicon glyphicon-off" id="st-close-session" title="logoff session"></div> ' +
'            </a>                                                                                                            ' +
'        </div> <!-- end row -->                                                                                             ' +
'    </div> <!-- end container-fluid of header -->                                                                           ' +
'</nav>'; //header_html

var footer_html =
'<nav class="navbar navbar-fixed-bottom st-white-on-color" id="st-footer">                        ' +
'    <script id="js-copyright-template" type="text/x-handlebars-template">                        ' +
'        <div id="st-copyright">                                                                  ' +
'            © Copyright {{year}} <a id="st-copyright-link" href="http://www.interdigital.com/">InterDigital, Inc.</a> All rights reserved' +
'        </div>                                                                                   ' +
'        <div id="st-version">Version {{version}}</div>                                           ' +
'    </script>                                                                                    ' +
'</nav> <!-- footer -->'; //footer_html

var responsebar_html =
'<div class="col-sm-2 col-md-2 label st-responsebar-label st-text-ellipsis"> Response Code </div> ' +
'<div class="col-sm-4 col-md-3 badge st-responsebar-value" id="js-response-code"></div>           ' +

'<!-- spacing empty div column -->                                                                ' +
'<div class="col-sm-1 col-md-2"></div>                                                            ' +

'<div class="col-sm-2 col-md-2 label st-responsebar-label st-text-ellipsis"> Response Time </div> ' +
'<div class="col-sm-3 col-md-3 badge st-responsebar-value" id="js-response-time"></div>'; //responsebar_html

var left_sidebar_html =
'<ul class="nav nav-pills nav-stacked">                                                                          ' +
'    <li class="st-bottom-line" id="js-pg-cntrl-config">                                                         ' +
'        <a href="index.html"> <i class="glyphicon glyphicon-wrench"></i> Configuration </a>                     ' +
'    </li>                                                                                                       ' +
'    <li class="st-bottom-line">                                                                                 ' +
'        <a class="st-menu-empty"> <i class="glyphicon glyphicon-cog"></i> Operations </a>                       ' +
'        <ul class="nav nav-pills nav-stacked">                                                                  ' +
'            <li id="js-pg-cust-regn"> <a href="cust_regn.html"> Customer Registration </a> </li>                ' +
'            <li>                                                                                                ' +
'                <a class="st-menu-empty"> <i class="glyphicon glyphicon-triangle-bottom"></i> Slice Management </a> ' +
'                <ul class="nav nav-pills nav-stacked">                                                          ' +
'                    <li id="js-pg-get-max-bw">   <a href="node_getbw.html">   Get Max BW </a> </li>            ' +
'                    <li id="js-pg-slice-create"> <a href="slice_create.html">  Create Slice </a> </li>          ' +
'                    <li id="js-pg-slice-modify"> <a href="slice_modify.html">  Modify Slice </a> </li>          ' +
'                    <li id="js-pg-slice-config"> <a href="slice_getconf.html"> Get Slice Config </a> </li>      ' +
'                </ul>                                                                                           ' +
'            </li>                                                                                               ' +
'        </ul>                                                                                                   ' +
'    </li>                                                                                                       ' +
'    <li id="js-pg-node-info" class="st-bottom-line"><a href="node_getinfo.html"> <i class="glyphicon glyphicon-th"></i> Node Info </a></li> ' +
'    <li id="js-pg-stats" class="st-bottom-line"><a href="stats.html"> <i class="glyphicon glyphicon-stats"></i> Statistics </a></li> ' +
'    <li id="js-pg-alerts"><a href="#"> <i class="glyphicon glyphicon-bell"></i> Alerts </a></li>                ' +
'</ul>'; //left_sidebar_html

var middlerow_html =
'    <div class="container-fluid st-container-right-border"> <!-- container-fluid corepart -->  ' +
'        <div class="row st-container-right-border"> <!-- corepart-row -->                      ' +

'            <div id="st-sidebar"></div>                     ' +

'            <div id="id-corepart-right-column" class="st-container-right-border"> ' +

'                <div id="st-pg-title" class="page-header st-color-on-white"></div>             ' +

'                <div class="container-fluid" id="js-right-corepart"> <!-- everything main -->  ' +
'                    <div class="row" id="js-input-params"></div>                               ' +
'                    <div class="row" id="js-responsebar-row"></div>                            ' +
'                    <div class="row" id="js-output-params"></div>                              ' +
'                    <div class="row" id="js-errow-row"></div>                                  ' +
'                </div> <!-- everything main container-fluid -->                                ' +

'            </div> <!-- corepart-right-column -->                                              ' +

'        </div> <!-- corepart-row -->                                                           ' +
'    </div> <!-- container-fluid corepart -->'; //middlerow_html

var error_row_html =
'    <form class="form-horizontal">                                         ' +
'        <div class="form-group">                                           ' +
'            <div class="col-sm-offset-1 col-sm-10" id="js-http-err"></div> ' +
'        </div> <!-- end form-group -->                                     ' +
'    </form> <!-- end form-horizontal -->'; //error_row_html

function pgTemplatesLoad() {
    $('#js-header-row').html(header_html);
    $('#js-middle-row').html(middlerow_html);
    $('#js-footer-row').html(footer_html);

    $('#st-sidebar').html(left_sidebar_html);

    //within middle row - input/output are filled page-specific
    $('#js-responsebar-row').html(responsebar_html);
    $('#js-errow-row').html(error_row_html);

    pgTemplatesFooter();

    $('#id-sidebar-btn').on("click", toggleSidebarCollapse);
} //func pgTemplatesLoad

/* copyright footer */
function pgTemplatesFooter() {

    // failsafe version string otherwise pg would crash
    try{
        if (! idcc_version) {
            idcc_version = "";
        } //else all ok already
    }
    catch(e) {
        idcc_version = "";
    }

    // failsafe copyright_year string otherwise pg would crash
    try{
        if (! idcc_copyright_year) {
            idcc_copyright_year = "";
        } //else all ok already
    }
    catch(e) {
        idcc_copyright_year = "";
    }

    var source   = $("#js-copyright-template").html();
    var template = Handlebars.compile(source);
    var context = {year: idcc_copyright_year, version: ": "+idcc_version};
    var html    = template(context);
    $('#st-footer').html(html);
}
