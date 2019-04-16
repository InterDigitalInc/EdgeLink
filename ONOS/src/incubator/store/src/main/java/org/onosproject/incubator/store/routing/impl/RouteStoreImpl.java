/*
 * Copyright 2017-present Open Networking Laboratory
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

package org.onosproject.incubator.store.routing.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.IpAddress;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.incubator.net.routing.NextHopData;
import org.onosproject.incubator.net.routing.Route;
import org.onosproject.incubator.net.routing.RouteEvent;
import org.onosproject.incubator.net.routing.RouteStore;
import org.onosproject.incubator.net.routing.RouteStoreDelegate;
import org.onosproject.incubator.net.routing.RouteTableId;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.service.StorageService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

/**
 * An implementation of RouteStore that is backed by either LocalRouteStore or
 * DistributedRouteStore according to configuration.
 */
@Service
@Component
public class RouteStoreImpl extends AbstractStore<RouteEvent, RouteStoreDelegate>
        implements RouteStore {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService componentConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    public StorageService storageService;

    @Property(name = "distributed", boolValue = false,
            label = "Enable distributed route store")
    private boolean distributed;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private RouteStore currentRouteStore;

    @Activate
    public void activate(ComponentContext context) {
        componentConfigService.registerProperties(getClass());
        modified(context);
    }

    @Deactivate
    public void deactivate() {
        if (distributed) {
            ((DistributedRouteStore) currentRouteStore).deactivate();
        } else {
            ((LocalRouteStore) currentRouteStore).deactivate();
        }
        componentConfigService.unregisterProperties(getClass(), false);
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();
        if (properties == null) {
            return;
        }

        String strDistributed = Tools.get(properties, "distributed");
        boolean expectDistributed = Boolean.parseBoolean(strDistributed);

        // Start route store during first start or config change
        // NOTE: new route store will be empty
        if (currentRouteStore == null || expectDistributed != distributed) {
            if (expectDistributed) {
                if (currentRouteStore != null) {
                    ((LocalRouteStore) currentRouteStore).deactivate();
                }
                currentRouteStore = new DistributedRouteStore(storageService);
                ((DistributedRouteStore) currentRouteStore).activate();
                ((DistributedRouteStore) currentRouteStore).setDelegate(delegate);
            } else {
                if (currentRouteStore != null) {
                    ((DistributedRouteStore) currentRouteStore).deactivate();
                }
                currentRouteStore = new LocalRouteStore();
                ((LocalRouteStore) currentRouteStore).activate();
                ((LocalRouteStore) currentRouteStore).setDelegate(delegate);
            }

            this.distributed = expectDistributed;
            log.info("Switched to {} route store", distributed ? "distributed" : "local");
        }

    }

    @Override
    public void setDelegate(RouteStoreDelegate delegate) {
        checkState(this.delegate == null || this.delegate == delegate,
                "Store delegate already set");
        this.delegate = delegate;

        // Set the delegate of underlying route store implementation
        currentRouteStore.setDelegate(delegate);
    }

    @Override
    public void unsetDelegate(RouteStoreDelegate delegate) {
        if (this.delegate == delegate) {
            this.delegate = null;
        }

        // Unset the delegate of underlying route store implementation
        currentRouteStore.unsetDelegate(delegate);
    }

    @Override
    public void updateRoute(Route route) {
        currentRouteStore.updateRoute(route);
    }

    @Override
    public void removeRoute(Route route) {
        currentRouteStore.removeRoute(route);
    }

    @Override
    public Set<RouteTableId> getRouteTables() {
        return currentRouteStore.getRouteTables();
    }

    @Override
    public Collection<Route> getRoutes(RouteTableId table) {
        return currentRouteStore.getRoutes(table);
    }

    @Override
    public Route longestPrefixMatch(IpAddress ip) {
        return currentRouteStore.longestPrefixMatch(ip);
    }

    @Override
    public Collection<Route> getRoutesForNextHop(IpAddress ip) {
        return currentRouteStore.getRoutesForNextHop(ip);
    }

    @Override
    public void updateNextHop(IpAddress ip, NextHopData nextHopData) {
        currentRouteStore.updateNextHop(ip, nextHopData);
    }

    @Override
    public void removeNextHop(IpAddress ip, NextHopData nextHopData) {
        currentRouteStore.removeNextHop(ip, nextHopData);
    }

    @Override
    public NextHopData getNextHop(IpAddress ip) {
        return currentRouteStore.getNextHop(ip);
    }

    @Override
    public Map<IpAddress, NextHopData> getNextHops() {
        return currentRouteStore.getNextHops();
    }
}
