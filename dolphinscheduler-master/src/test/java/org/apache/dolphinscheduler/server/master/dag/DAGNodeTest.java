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

package org.apache.dolphinscheduler.server.master.dag;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

class DAGNodeTest {

    @Test
    void buildDAGNode_EmptyNodeName() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> DAGNode.builder()
                        .inDegrees(new ArrayList<>())
                        .outDegrees(new ArrayList<>())
                        .build());
        assertEquals("nodeName cannot be empty", illegalArgumentException.getMessage());
    }

    @Test
    void buildDAGNode_BadInDegree() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> DAGNode.builder()
                        .nodeName("A")
                        .inDegrees(Lists.newArrayList(DAGEdge.builder()
                                .fromNodeName(null)
                                .toNodeName("B")
                                .build()))
                        .outDegrees(new ArrayList<>())
                        .build());
        assertEquals(
                "The toNodeName of inDegree should be the nodeName of the node: A, inDegree: DAGEdge(fromNodeName=null, toNodeName=B)",
                illegalArgumentException.getMessage());
    }

    @Test
    void buildDAGNode_NiceInDegree() {
        assertDoesNotThrow(() -> DAGNode.builder()
                .nodeName("A")
                .inDegrees(Lists.newArrayList(DAGEdge.builder()
                        .fromNodeName(null)
                        .toNodeName("A")
                        .build()))
                .outDegrees(new ArrayList<>())
                .build());
    }

    @Test
    void buildDAGNode_BadOutDegree() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> DAGNode.builder()
                        .nodeName("A")
                        .inDegrees(new ArrayList<>())
                        .outDegrees(Lists.newArrayList(DAGEdge.builder()
                                .fromNodeName("B")
                                .toNodeName(null)
                                .build()))
                        .build());
        assertEquals(
                "The fromNodeName of outDegree should be the nodeName of the node: A, outDegree: DAGEdge(fromNodeName=B, toNodeName=null)",
                illegalArgumentException.getMessage());
    }

    @Test
    void buildDAGNode_NiceOutDegree() {
        assertDoesNotThrow(() -> DAGNode.builder()
                .nodeName("A")
                .inDegrees(new ArrayList<>())
                .outDegrees(Lists.newArrayList(DAGEdge.builder()
                        .fromNodeName("A")
                        .toNodeName(null)
                        .build()))
                .build());
    }

    @Test
    void buildDAGNode_NotSkip() {
        DAGNode dagNode = DAGNode.builder()
                .nodeName("A")
                .inDegrees(new ArrayList<>())
                .outDegrees(new ArrayList<>())
                .build();
        assertFalse(dagNode.isSkip());
    }

    @Test
    void buildDAGNode_Skip() {
        DAGNode dagNode = DAGNode.builder()
                .nodeName("A")
                .skip(true)
                .inDegrees(new ArrayList<>())
                .outDegrees(new ArrayList<>())
                .build();
        assertTrue(dagNode.isSkip());
    }

}
