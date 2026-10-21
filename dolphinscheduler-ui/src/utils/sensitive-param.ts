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

/** TODO: Optimize sensitive-value identification so it can be recognized accurately instead
 *  of relying on a regex / length heuristic; this will allow removing this rule restriction.
 *  Regex rule for a valid sensitive value. Mirrors the backend {@code SENSITIVE_FIELD_RULE}.
 *  Kept as a regex so the rule can be extended later. Currently requires at least 6
 *  characters ({@code .{6,}}). */
export const SENSITIVE_FIELD_RULE = '.{6,}'

const SENSITIVE_FIELD_RULE_REGEXP = new RegExp(`^${SENSITIVE_FIELD_RULE}$`, 's')

/**
 * A sensitive value is valid when it is empty / keep-original placeholder
 * (handled separately by the create/update logic) or satisfies
 * {@link SENSITIVE_FIELD_RULE}.
 */
export function isValidSensitiveValue(value?: string): boolean {
  if (
    value === undefined ||
    value === null ||
    value === '' ||
    value === SENSITIVE_VALUE_MASK
  ) {
    return true
  }
  return SENSITIVE_FIELD_RULE_REGEXP.test(value)
}

/** Unchecking does not clear value; empty + re-check restores ****** (keep-original). */
export function applySensitiveToggle(
  param: { value?: string; sensitive?: boolean },
  checked: boolean
) {
  param.sensitive = checked
  if (checked && (param.value === '' || param.value == null)) {
    param.value = SENSITIVE_VALUE_MASK
  }
}
