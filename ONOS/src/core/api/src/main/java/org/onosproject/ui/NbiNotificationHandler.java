/*
 * Copyright 2015-present Open Networking Laboratory
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
package org.onosproject.ui;

import java.util.ArrayList;
import java.util.List;

public class NbiNotificationHandler {

    private static NbiNotificationHandler instance;
    private List<UiMessageHandler> uiMessageHandlerList = new ArrayList<UiMessageHandler>();

    private NbiNotificationHandler() {
    }

    public static NbiNotificationHandler getInstance() {
        if (instance == null) {
            synchronized (NbiNotificationHandler.class) {
                if (instance == null) {
                    instance = new NbiNotificationHandler();
                }
            }
        }
        return instance;
    }

    public void addUiMessageHandler(UiMessageHandler uiMessageHandler) {
        this.uiMessageHandlerList.add(uiMessageHandler);
    }

    public void removeUiMessageHandler(UiMessageHandler uiMessageHandler) {
        uiMessageHandler.destroy();
        this.uiMessageHandlerList.remove(uiMessageHandler);
    }

    public void clearUiMessageHandlerList() {
        uiMessageHandlerList.forEach(
                (handler) -> handler.destroy()
        );
        uiMessageHandlerList.clear();
    }

    public List<UiMessageHandler> getNbiNotificationHandlers() {
        return uiMessageHandlerList;
    }
}