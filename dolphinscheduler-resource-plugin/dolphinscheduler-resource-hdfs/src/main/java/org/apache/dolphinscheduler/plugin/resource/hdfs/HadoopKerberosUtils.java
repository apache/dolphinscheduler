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

package org.apache.dolphinscheduler.plugin.resource.hdfs;

import org.apache.dolphinscheduler.spi.resource.ResourceStorageException;

import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.UserGroupInformation.AuthenticationMethod;

import java.io.IOException;


public class HadoopKerberosUtils {

    public static void authenticate(HdfsConfiguration conf, String user, String keytabPath){
        UserGroupInformation.setConfiguration(conf);
        if(! UserGroupInformation.isSecurityEnabled())
            return;
        try {
            UserGroupInformation.getCurrentUser().setAuthenticationMethod(AuthenticationMethod.KERBEROS);
            UserGroupInformation.loginUserFromKeytab(user,keytabPath);
        } catch (IOException e) {
          throw new ResourceStorageException("load kerberos authenticate error",e);
        }
    }
}
