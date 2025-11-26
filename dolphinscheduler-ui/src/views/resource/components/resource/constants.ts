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

/**
 * Monaco Editor vertical offset for file edit page
 * This offset accounts for:
 * - Page header and navigation (~80px)
 * - Card title and padding (~60px)
 * - File name display (~60px)
 * - Form padding and button area (~120px)
 * Total: ~320px
 */
export const EDITOR_VERTICAL_OFFSET_EDIT = 320

/**
 * Monaco Editor vertical offset for file create page
 * This offset accounts for:
 * - Page header and navigation (~80px)
 * - Card title and padding (~60px)
 * - File name input field (~80px)
 * - File format selector (~80px)
 * - Form padding and button area (~120px)
 * Total: ~420px
 */
export const EDITOR_VERTICAL_OFFSET_CREATE = 420
