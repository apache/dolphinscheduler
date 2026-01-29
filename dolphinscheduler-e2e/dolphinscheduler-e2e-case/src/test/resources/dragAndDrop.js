/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function createEvent(typeOfEvent) {
    const event = document.createEvent("CustomEvent");
    event.initCustomEvent(typeOfEvent, true, true, null);
    event.dataTransfer = {
        data: {},
        setData: function (key, value) {
            this.data[key] = value;
        },
        getData: function (key) {
            return this.data[key];
        }
    };
    return event;
}

function dispatchEvent(element, event, transferData) {
    if (transferData !== undefined) {
        event.dataTransfer = transferData;
    }
    if (element.dispatchEvent) {
        element.dispatchEvent(event);
    } else if (element.fireEvent) {
        element.fireEvent("on" + event.type, event);
    }
}

function simulateHTML5DragAndDrop(element, destination) {
    const dragStartEvent = createEvent('dragstart');
    dispatchEvent(element, dragStartEvent);
    const dropEvent = createEvent('drop');
    dispatchEvent(destination, dropEvent, dragStartEvent.dataTransfer);
    const dragEndEvent = createEvent('dragend');
    dispatchEvent(element, dragEndEvent, dropEvent.dataTransfer);
}

const source = arguments[0];
const destination = arguments[1];
simulateHTML5DragAndDrop(source, destination);
