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

package org.apache.dolphinscheduler.remote;


import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.junit.Assert;
import org.junit.Test;

public class FastJsonSerializerTest {

    @Test
    public void testSerialize(){
        TestObj testObj = new TestObj();
        testObj.setAge(12);
        byte[] serializeByte = FastJsonSerializer.serialize(testObj);

        //
        TestObj deserialize = FastJsonSerializer.deserialize(serializeByte, TestObj.class);

        Assert.assertEquals(testObj.getAge(), deserialize.getAge());
    }

    static class TestObj {

        private int age;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "TestObj{" +
                    "age=" + age +
                    '}';
        }
    }
}
