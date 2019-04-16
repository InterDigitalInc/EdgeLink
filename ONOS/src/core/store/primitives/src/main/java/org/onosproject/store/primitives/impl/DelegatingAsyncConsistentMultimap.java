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

package org.onosproject.store.primitives.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;
import org.onosproject.store.service.AsyncConsistentMultimap;
import org.onosproject.store.service.Versioned;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * {@code AsyncConsistentMultimap} that merely delegates control to
 * another AsyncConsistentMultimap.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class DelegatingAsyncConsistentMultimap<K, V>
        implements AsyncConsistentMultimap<K, V> {

    private final AsyncConsistentMultimap<K, V> delegateMap;

    public DelegatingAsyncConsistentMultimap(
            AsyncConsistentMultimap<K, V> delegateMap) {
        this.delegateMap = Preconditions.checkNotNull(delegateMap);
    }

    @Override
    public String name() {
        return delegateMap.name();
    }

    @Override
    public CompletableFuture<Integer> size() {
        return delegateMap.size();
    }

    @Override
    public CompletableFuture<Boolean> isEmpty() {
        return delegateMap.isEmpty();
    }

    @Override
    public CompletableFuture<Boolean> containsKey(K key) {
        return delegateMap.containsKey(key);
    }

    @Override
    public CompletableFuture<Boolean> containsValue(V value) {
        return delegateMap.containsValue(value);
    }

    @Override
    public CompletableFuture<Boolean> containsEntry(K key, V value) {
        return delegateMap.containsEntry(key, value);
    }

    @Override
    public CompletableFuture<Boolean> put(K key, V value) {
        return delegateMap.put(key, value);
    }

    @Override
    public CompletableFuture<Boolean> remove(K key, V value) {
        return delegateMap.remove(key, value);
    }

    @Override
    public CompletableFuture<Boolean> removeAll(
            K key, Collection<? extends V> values) {
        return delegateMap.removeAll(key, values);
    }

    @Override
    public CompletableFuture<Versioned<Collection<? extends V>>>
        removeAll(K key) {
        return delegateMap.removeAll(key);
    }

    @Override
    public CompletableFuture<Boolean> putAll(
            K key, Collection<? extends V> values) {
        return delegateMap.putAll(key, values);
    }

    @Override
    public CompletableFuture<Versioned<Collection<? extends V>>>
        replaceValues(K key, Collection<V> values) {
        return delegateMap.replaceValues(key, values);
    }

    @Override
    public CompletableFuture<Void> clear() {
        return delegateMap.clear();
    }

    @Override
    public CompletableFuture<Versioned<Collection<? extends V>>> get(K key) {
        return delegateMap.get(key);
    }

    @Override
    public CompletableFuture<Set<K>> keySet() {
        return delegateMap.keySet();
    }

    @Override
    public CompletableFuture<Multiset<K>> keys() {
        return delegateMap.keys();
    }

    @Override
    public CompletableFuture<Multiset<V>> values() {
        return delegateMap.values();
    }

    @Override
    public CompletableFuture<Collection<Map.Entry<K, V>>> entries() {
        return delegateMap.entries();
    }

    @Override
    public CompletableFuture<Map<K, Collection<V>>> asMap() {
        return delegateMap.asMap();
    }

    @Override
    public void addStatusChangeListener(Consumer<Status> listener) {
        delegateMap.addStatusChangeListener(listener);
    }

    @Override
    public void removeStatusChangeListener(Consumer<Status> listener) {
        delegateMap.removeStatusChangeListener(listener);
    }

    @Override
    public Collection<Consumer<Status>> statusChangeListeners() {
        return delegateMap.statusChangeListeners();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("delegateMap", delegateMap)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegateMap);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DelegatingAsyncConsistentMultimap) {
            DelegatingAsyncConsistentMultimap<K, V> that =
                    (DelegatingAsyncConsistentMultimap) other;
            return this.delegateMap.equals(that.delegateMap);
        }
        return false;
    }
}
