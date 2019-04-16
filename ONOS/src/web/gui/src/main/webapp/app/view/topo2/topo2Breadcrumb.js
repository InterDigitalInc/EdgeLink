/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 ONOS GUI -- Topology Breadcrumb Module.
 Module that renders the breadcrumbs for regions
 */

(function () {

    'use strict';

    var $log, wss;

    // Internal
    var breadcrumbContainer,
        breadcrumbs,
        layout;

    function init() {
        $log.debug("Topo2BreadcrumbService Initiated");
        breadcrumbs = [];
        breadcrumbContainer = d3.select('#breadcrumbs');

        breadcrumbContainer
            .append('span')
            .text('Regions: ');

        render();
    }

    function addBreadcrumb(crumbs) {
        breadcrumbContainer.selectAll('.breadcrumb').remove();
        breadcrumbs = crumbs.reverse();
        render();
    }

    function navigateToRegion(data, index) {

        if (index === breadcrumbs.length - 1) {
            return;
        }

        // Remove breadcrumbs after index;
        breadcrumbs.splice(index + 1);

        wss.sendEvent('topo2navRegion', {
            dir: 'up',
            rid: data.id
        });

        layout.createForceElements();
        layout.transitionDownRegion();

        render();
    }

    function render() {

        var selection = breadcrumbContainer.selectAll('.breadcrumb')
            .data(breadcrumbs);

        selection.enter()
            .append('div')
                .attr('class', 'breadcrumb')
                .on('click', navigateToRegion)
            .append('a')
                .text(function (d) {
                    return d.name;
                });

        selection.exit()
            .transition()
            .duration(200)
            .style('opacity', 0)
            .remove();
    }

    function hide() {

        var view = d3.select('body');
        view.classed('breadcrumb--hidden', true);

        var startTranslateState = 'translate(0px,0%)',
            endTranslateState = 'translate(0px,-100%)',
            translateInterpolator = d3.interpolateString(startTranslateState, endTranslateState);

        breadcrumbContainer
            .transition()
            .duration(600)
            .style('opacity', 0)
            .styleTween('transform', function (d) {
                return translateInterpolator;
            });
    }

    function addLayout(_layout_) {
        layout = _layout_;
    }

    angular.module('ovTopo2')
    .factory('Topo2BreadcrumbService', [
        '$log', 'WebSocketService',
        function (_$log_, _wss_) {

            $log = _$log_;
            wss = _wss_;

            return {
                init: init,
                addBreadcrumb: addBreadcrumb,
                addLayout: addLayout,
                hide: hide
            };
        }
    ]);
})();
