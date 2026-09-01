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

export const SENSITIVE_VALUE_MASK = '******'

/**
 * Keep-original is only ******. Unchecking does not clear the field:
 * saving sensitive=false with ****** is rejected by the API, which is
 * safer than silently overwriting the secret with an empty string.
 * Re-checking with an empty field restores the mask (keep-original).
 */
export function applySensitiveToggle(
  param: { value?: string; sensitive?: boolean },
  checked: boolean
) {
  param.sensitive = checked
  if (checked && (param.value === '' || param.value == null)) {
    param.value = SENSITIVE_VALUE_MASK
  }
}
