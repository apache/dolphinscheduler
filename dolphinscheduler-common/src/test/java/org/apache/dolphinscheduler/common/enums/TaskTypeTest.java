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
package org.apache.dolphinscheduler.common.enums;

import org.junit.Assert;
import org.junit.Test;

public class TaskTypeTest {
    @Test
    public void typeIsNormalTask(){

        Assert.assertTrue(TaskType.typeIsNormalTask("SQL"));
        Assert.assertFalse(TaskType.typeIsNormalTask("SUB_PROCESS"));
        Assert.assertTrue(TaskType.typeIsNormalTask("PROCEDURE"));
        Assert.assertTrue(TaskType.typeIsNormalTask("MR"));
        Assert.assertTrue(TaskType.typeIsNormalTask("SPARK"));
        Assert.assertTrue(TaskType.typeIsNormalTask("PYTHON"));
        Assert.assertFalse(TaskType.typeIsNormalTask("DEPENDENT"));
        Assert.assertTrue(TaskType.typeIsNormalTask("FLINK"));
        Assert.assertTrue(TaskType.typeIsNormalTask("HTTP"));
        Assert.assertTrue(TaskType.typeIsNormalTask("DATAX"));

        try{
            Assert.assertFalse(TaskType.typeIsNormalTask("unknown"));
            Assert.fail("unknown task type");
        }catch (IllegalArgumentException ignore){
            Assert.assertTrue(true);
        }
        try{
            Assert.assertFalse(TaskType.typeIsNormalTask(""));
            Assert.fail("empty task type");
        }catch (IllegalArgumentException ignore){
            Assert.assertTrue(true);
        }
        try{
            Assert.assertFalse(TaskType.typeIsNormalTask(null));
            Assert.fail("null task type");
        }catch (NullPointerException ignore){
            Assert.assertTrue(true);
        }
    }
    @Test
    public void typeIsYarnTask(){

        Assert.assertTrue(TaskType.typeIsYarnTask("MR"));
        Assert.assertTrue(TaskType.typeIsYarnTask("SPARK"));
        Assert.assertTrue(TaskType.typeIsYarnTask("FLINK"));

        Assert.assertFalse(TaskType.typeIsYarnTask("SQL"));
        Assert.assertFalse(TaskType.typeIsYarnTask("SUB_PROCESS"));
        Assert.assertFalse(TaskType.typeIsYarnTask("PROCEDURE"));
        Assert.assertFalse(TaskType.typeIsYarnTask("PYTHON"));
        Assert.assertFalse(TaskType.typeIsYarnTask("DEPENDENT"));
        Assert.assertFalse(TaskType.typeIsYarnTask("HTTP"));
        Assert.assertFalse(TaskType.typeIsYarnTask("DATAX"));

        try{
            Assert.assertFalse(TaskType.typeIsYarnTask("unknown"));
            Assert.fail("unknown task type");
        }catch (IllegalArgumentException ignore){
            Assert.assertTrue(true);
        }
        try{
            Assert.assertFalse(TaskType.typeIsYarnTask(""));
            Assert.fail("empty task type");
        }catch (IllegalArgumentException ignore){
            Assert.assertTrue(true);
        }
        try{
            Assert.assertFalse(TaskType.typeIsYarnTask(null));
            Assert.fail("null task type");
        }catch (NullPointerException ignore){
            Assert.assertTrue(true);
        }

    }
}
