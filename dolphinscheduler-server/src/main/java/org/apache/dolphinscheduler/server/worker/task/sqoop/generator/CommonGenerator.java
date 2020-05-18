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
package org.apache.dolphinscheduler.server.worker.task.sqoop.generator;

import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * common script generator
 */
public class CommonGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public String generate(SqoopParameters sqoopParameters) {
        StringBuilder result = new StringBuilder();
        try{
            result.append("sqoop ")
                    .append(sqoopParameters.getModelType());
            if(sqoopParameters.getConcurrency() >0){
                result.append(" -m ")
                        .append(sqoopParameters.getConcurrency());
            }
            if(CollectionUtils.isNotEmpty(sqoopParameters.localParams)){
                for (Property property : sqoopParameters.localParams){
                    result.append(" ").append(property.getProp());
                    if(StringUtils.isNotEmpty(property.getValue())){
                        result.append(" ").append(property.getValue());
                    }
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        return result.toString();
    }
}
