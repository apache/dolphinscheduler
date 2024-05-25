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

export const DATA_TYPES_MAP = {
    VARCHAR: {
        alias: 'VARCHAR'
    },
    INTEGER: {
        alias: 'INTEGER'
    },
    LONG: {
        alias: 'LONG'
    },
    FLOAT: {
        alias: 'FLOAT'
    },
    DOUBLE: {
        alias: 'DOUBLE'
    },
    DATE: {
        alias: 'DATE'
    },
    TIME: {
        alias: 'TIME'
    },
    TIMESTAMP: {
        alias: 'TIMESTAMP'
    },
    BOOLEAN: {
        alias: 'BOOLEAN'
    },
    LIST: {
        alias: 'LIST'
    },
    FILE: {
        alias: 'FILE'
    }
}

export const DEFAULT_DATA_TYPE = 'VARCHAR'
