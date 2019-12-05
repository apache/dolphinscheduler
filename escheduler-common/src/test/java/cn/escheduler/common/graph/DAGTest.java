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
package cn.escheduler.common.graph;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DAGTest {
  private DAG<Integer, String, String> graph;
  private static final Logger logger = LoggerFactory.getLogger(DAGTest.class);

  @Before
  public void setup() {
    graph = new DAG<>();
  }

  @After
  public void tearDown() {
    clear();
  }

  private void clear() {
    graph = null;
    graph = new DAG<>();

    assertEquals(graph.getNodesCount(), 0);
  }


  private void makeGraph() {
    clear();

    //         1->2
    //         2->5
    //         3->5
    //         4->6
    //         5->6
    //         6->7

    for (int i = 1; i <= 7; ++i) {
      graph.addNode(i, "v(" + i + ")");
    }

    // 构造边
    assertTrue(graph.addEdge(1, 2));

    assertTrue(graph.addEdge(2, 5));

    assertTrue(graph.addEdge(3, 5));

    assertTrue(graph.addEdge(4, 6));

    assertTrue(graph.addEdge(5, 6));

    assertTrue(graph.addEdge(6, 7));

    assertEquals(graph.getNodesCount(), 7);
    assertEquals(graph.getEdgesCount(), 6);

  }


  /**
   * 测试增加顶点
   */
  @Test
  public void testAddNode() {
    clear();

    graph.addNode(1, "v(1)");
    graph.addNode(2, null);
    graph.addNode(5, "v(5)");

    assertEquals(graph.getNodesCount(), 3);

    assertEquals(graph.getNode(1), "v(1)");
    assertTrue(graph.containsNode(1));

    assertFalse(graph.containsNode(10));
  }


  /**
   * 添加边
   */
  @Test
  public void testAddEdge() {
    clear();

    assertFalse(graph.addEdge(1, 2, "edge(1 -> 2)", false));

    graph.addNode(1, "v(1)");

    assertTrue(graph.addEdge(1, 2, "edge(1 -> 2)",true));

    graph.addNode(2, "v(2)");

    assertTrue(graph.addEdge(1, 2, "edge(1 -> 2)",true));

    assertFalse(graph.containsEdge(1, 3));

    assertTrue(graph.containsEdge(1, 2));
    assertEquals(graph.getEdgesCount(), 1);

  }


  /**
   * 测试后续结点
   */
  @Test
  public void testSubsequentNodes() {
    makeGraph();

    assertEquals(graph.getSubsequentNodes(1).size(), 1);

  }


  /**
   * 测试入度
   */
  @Test
  public void testIndegree() {
    makeGraph();

    assertEquals(graph.getIndegree(1), 0);
    assertEquals(graph.getIndegree(2), 1);
    assertEquals(graph.getIndegree(3), 0);
    assertEquals(graph.getIndegree(4), 0);
  }


  /**
   * 测试起点
   */
  @Test
  public void testBeginNode() {
    makeGraph();

    assertEquals(graph.getBeginNode().size(), 3);

    assertTrue(graph.getBeginNode().contains(1));
    assertTrue(graph.getBeginNode().contains(3));
    assertTrue(graph.getBeginNode().contains(4));
  }


  /**
   * 测试终点
   */
  @Test
  public void testEndNode() {
    makeGraph();

    assertEquals(graph.getEndNode().size(), 1);

    assertTrue(graph.getEndNode().contains(7));
  }


  /**
   * 测试环
   */
  @Test
  public void testCycle() {
    clear();

    // 构造顶点
    for (int i = 1; i <= 5; ++i) {
      graph.addNode(i, "v(" + i + ")");
    }

    // 构造边, 1->2, 2->3, 3->4
    try {
      graph.addEdge(1, 2);
      graph.addEdge(2, 3);
      graph.addEdge(3, 4);

      assertFalse(graph.hasCycle());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }


    try {
      boolean addResult = graph.addEdge(4, 1);//有环，添加失败

      if(!addResult){//有环，添加失败
        assertTrue(true);
      }

      graph.addEdge(5, 1);

      assertFalse(graph.hasCycle());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    // 重新清空
    clear();

    // 构造顶点
    for (int i = 1; i <= 5; ++i) {
      graph.addNode(i, "v(" + i +")");
    }

    // 构造边, 1->2, 2->3, 3->4
    try {
      graph.addEdge(1, 2);
      graph.addEdge(2, 3);
      graph.addEdge(3, 4);
      graph.addEdge(4, 5);
      graph.addEdge(5, 2);//会失败，添加不进去，所以下一步无环

      assertFalse(graph.hasCycle());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }


  @Test
  public void testTopologicalSort(){
    makeGraph();

    try {
      List<Integer> topoList = new ArrayList<>();//一种拓扑结果是1 3 4 2 5 6 7
      topoList.add(1);
      topoList.add(3);
      topoList.add(4);
      topoList.add(2);
      topoList.add(5);
      topoList.add(6);
      topoList.add(7);

      assertEquals(graph.topologicalSort(),topoList);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }


  @Test
  public void testTopologicalSort2() {
    clear();

    graph.addEdge(1, 2, null, true);
    graph.addEdge(2, 3, null, true);
    graph.addEdge(3, 4, null, true);
    graph.addEdge(4, 5, null, true);
    graph.addEdge(5, 1, null, false); //因环会添加失败，ERROR级别日志输出

    try {
      List<Integer> topoList = new ArrayList<>();//拓扑结果是1 2 3 4 5
      topoList.add(1);
      topoList.add(2);
      topoList.add(3);
      topoList.add(4);
      topoList.add(5);

      assertEquals(graph.topologicalSort(),topoList);

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }

  }


  /**
   *
   */
  @Test
  public void testTopologicalSort3() throws Exception {
    clear();

    //         1->2
    //         1->3
    //         2->5
    //         3->4
    //         4->6
    //         5->6
    //         6->7
    //         6->8

    for (int i = 1; i <= 8; ++i) {
      graph.addNode(i, "v(" + i + ")");
    }

    // 构造边
    assertTrue(graph.addEdge(1, 2));

    assertTrue(graph.addEdge(1, 3));

    assertTrue(graph.addEdge(2, 5));
    assertTrue(graph.addEdge(3, 4));

    assertTrue(graph.addEdge(4, 6));

    assertTrue(graph.addEdge(5, 6));

    assertTrue(graph.addEdge(6, 7));
    assertTrue(graph.addEdge(6, 8));




    assertEquals(graph.getNodesCount(), 8);

    logger.info(Arrays.toString(graph.topologicalSort().toArray()));

    List<Integer> expectedList = new ArrayList<>();

    for (int i = 1; i <= 8; ++i) {
      expectedList.add(i);

      logger.info(i + " subsequentNodes : " + graph.getSubsequentNodes(i));
    }

//    assertArrayEquals(expectedList.toArray(),graph.topologicalSort().toArray());

    logger.info(6 + "  previousNodesb: " + graph.getPreviousNodes(6));
    assertEquals(5, graph.getSubsequentNodes(2).toArray()[0]);

  }

}
