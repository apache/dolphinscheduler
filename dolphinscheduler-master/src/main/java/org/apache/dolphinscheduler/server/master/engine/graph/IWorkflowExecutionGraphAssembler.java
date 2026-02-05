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

package org.apache.dolphinscheduler.server.master.engine.graph;

/**
 * Interface for deferred assembly of WorkflowExecutionGraph.
 * <p>
 * The implementation captures all the context needed to assemble the execution graph,
 * allowing the actual graph construction to be deferred until the WorkflowStartLifecycleEvent
 * is fired. This reduces transaction time during command processing.
 */
@FunctionalInterface
public interface IWorkflowExecutionGraphAssembler {

    /**
     * Assemble and return the WorkflowExecutionGraph.
     * <p>
     * This method should be called when the workflow is ready to start execution,
     * typically during the handling of WorkflowStartLifecycleEvent.
     *
     * @return the assembled WorkflowExecutionGraph
     */
    IWorkflowExecutionGraph assemble();

}
