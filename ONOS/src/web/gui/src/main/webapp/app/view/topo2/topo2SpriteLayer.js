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
 ONOS GUI -- Topology Sprite Module.
 Defines behavior for loading sprites into the sprite layer.
 */

 (function() {
    'use strict';

    var instance,
        renderer;

    function vbox(w, h) {
      return '0 0 ' + w + ' ' + h;
    }

    angular.module('ovTopo2')
        .factory('Topo2SpriteLayerService', [
            'Topo2ViewController', 'SpriteService',

            function (ViewController, ss) {

                var SpriteLayer = ViewController.extend({
                    init: function(svg, zoomLayer) {
                        this.svg = svg;
                        this.createSpriteDefs();
                        this.container = zoomLayer.append('g').attr('id', 'topo-sprites');
                    },
                    loadLayout: function (id) {
                        this.container.selectAll("*").remove();
                        this.layout = ss.layout(id);
                        this.renderLayout();
                    },
                    createSpriteDefs: function () {
                       this.defs = this.svg.append('defs')
                            .attr('id', 'sprites');
                    },

                    renderSprite: function (spriteData) {

                        var id = spriteData.sprite.data.id,
                            definition = d3.select('#' + id);

                        if (definition.empty()) {

                            var data = spriteData.sprite.data,
                                spriteEl = this.defs.append('symbol')
                                    .attr('viewBox', vbox(data.w, data.h))
                                    .attr('id', id);

                            _.each(spriteData.sprite.paths, function (path) {
                                spriteEl.append('path')
                                    .attr('d', path)
                                    .style('fill', 'none')
                                    .style('stroke', 'black');
                            });

                            _.each(spriteData.sprite.rects, function (rect) {
                                spriteEl.append('rect')
                                    .attr('width', rect.w)
                                    .attr('height', rect.h)
                                    .attr('x', rect.x)
                                    .attr('y', rect.y)
                                    .style('fill', 'rgba(0,0,0,0.5)')
                            });
                        }

                        return spriteEl
                    },
                    renderLayout: function () {

                        var _this = this;

                        var layout = this.container.append('svg')
                            .attr('class', layout)
                            .attr('viewBox', vbox(this.layout.data.w, this.layout.data.h));

                        _.each(this.layout.sprites, function (spriteData) {
                            _this.renderSprite(spriteData);

                            layout
                                .append('g')
                                .append("use")
                                .attr('xlink:href', '#rack')
                                .attr('width', 20)
                                .attr('height', 25)
                                .attr('x', spriteData.x)
                                .attr('y', spriteData.y);
                        });
                    },

                    hide: function () {
                        if (this.visible) {
                            this.container
                                .style('opacity', 1)
                                .transition()
                                .duration(400)
                                .style('opacity', 0);
                        }
                        this.visible = false;
                    },
                    show: function () {
                        if (!this.visible) {
                            this.container
                                .style('opacity', 0)
                                .transition()
                                .duration(400)
                                .style('opacity', 1);
                        }
                        this.visible = true;
                    }
                });

                function getInstance() {
                    return instance || new SpriteLayer();
                }

                return getInstance();
            }
        ])
 })();