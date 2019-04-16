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
package org.onosproject.distributedprimitives.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.Versioned;

/**
 * CLI command to manipulate a distributed value.
 */
@Command(scope = "onos", name = "map-test",
        description = "Manipulate a consistent map")
public class ConsistentMapTestCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "name",
            description = "map name",
            required = true, multiValued = false)
    String name = null;

    @Argument(index = 1, name = "operation",
            description = "operation name",
            required = true, multiValued = false)
    String operation = null;

    @Argument(index = 2, name = "key",
            description = "first arg",
            required = false, multiValued = false)
    String arg1 = null;

    @Argument(index = 3, name = "value1",
            description = "second arg",
            required = false, multiValued = false)
    String arg2 = null;

    @Argument(index = 4, name = "value2",
            description = "third arg",
            required = false, multiValued = false)
    String arg3 = null;

    ConsistentMap<String, String> map;

    @Override
    protected void execute() {
        StorageService storageService = get(StorageService.class);
        map = storageService.<String, String>consistentMapBuilder()
                                    .withName(name)
                                    .withSerializer(Serializer.using(KryoNamespaces.BASIC))
                                    .build();
        if (operation.equals("get")) {
            print(map.get(arg1));
        } else if (operation.equals("put")) {
            print(map.put(arg1, arg2));
        } else if (operation.equals("size")) {
            print("%d", map.size());
        } else if (operation.equals("isEmpty")) {
            print("%b", map.isEmpty());
        } else if (operation.equals("putIfAbsent")) {
            print(map.putIfAbsent(arg1, arg2));
        } else if (operation.equals("putAndGet")) {
            print(map.putAndGet(arg1, arg2));
        } else if (operation.equals("clear")) {
            map.clear();
        } else if (operation.equals("remove")) {
            if (arg2 == null) {
                print(map.remove(arg1));
            } else {
                print("%b", map.remove(arg1, arg2));
            }
        } else if (operation.equals("containsKey")) {
            print("%b", map.containsKey(arg1));
        } else if (operation.equals("containsValue")) {
            print("%b", map.containsValue(arg1));
        } else if (operation.equals("replace")) {
            if (arg3 == null) {
                print(map.replace(arg1, arg2));
            } else {
                print("%b", map.replace(arg1, arg2, arg3));
            }
        }
    }

    void print(Versioned<String> value) {
        if (value == null) {
            print("null");
        } else {
            print("%s", value.value());
        }
    }
}
