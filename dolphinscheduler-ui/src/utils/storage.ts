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

import { StorageInterface, StorageKey, StorageExpire, StorageData, StorageResult } from "@/common/types";
import { Dictionaries } from "@/common/enum";

export class StorageImpl implements StorageInterface {
    public set<T = any>(key: StorageKey, value: T, expire: StorageExpire = Dictionaries.StoragePermanent) {
        const data = {
            value,
            [Dictionaries.StorageExpire]: expire
        }
        localStorage.setItem(key, JSON.stringify(data))
    }

    public get<T = any>(key: StorageKey):StorageResult<T | null> {
        const value = localStorage.getItem(key)
        if (value) {
            const obj: StorageData<T> = JSON.parse(value)
            const now = new Date().getTime()
            if (typeof obj[Dictionaries.StorageExpire] == 'number' && obj[Dictionaries.StorageExpire] < now) {
                this.remove(key)
                return {
                    message:`your ${key} is expired`,
                    value:null
                }
            }else{
                return {
                    message:"read successfully",
                    value:obj.value
                }
            }
        } else {
            console.warn('key value is invalid')
            return {
                message:`key value is invalid`,
                value:null
            }
        }
    }
    public remove(key:StorageKey) {
        localStorage.removeItem(key)
    }
    public clear() {
        localStorage.clear()
    }

}