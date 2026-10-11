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

package org.apache.dolphinscheduler.extract.base.exception;

/**
 * The server answered that it does not have the requested method — the old-server signal during
 * a rolling upgrade. A subclass of {@link MethodInvocationException} so existing callers keep
 * working; only callers that must distinguish "outdated peer" from "the invocation failed" (e.g.
 * the worker-upgrade guidance of the chunked log download) need to check for this type: every
 * other server-side failure — the method threw, the server's invocation pool is full — is
 * reported as a plain {@link MethodInvocationException}.
 */
public class MethodNotFoundException extends MethodInvocationException {

    public MethodNotFoundException(String message) {
        super(message);
    }
}
