#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# db
spring.datasource.driver-class-name=${DATABASE_DRIVER}
spring.datasource.url=jdbc:${DATABASE_TYPE}://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_DATABASE}?${DATABASE_PARAMS}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

## base spring data source configuration todo need to remove
#spring.datasource.type=com.alibaba.druid.pool.DruidDataSource

# connection configuration
#spring.datasource.initialSize=5
# min connection number
#spring.datasource.minIdle=5
# max connection number
#spring.datasource.maxActive=50

# max wait time for get a connection in milliseconds. if configuring maxWait, fair locks are enabled by default and concurrency efficiency decreases.
# If necessary, unfair locks can be used by configuring the useUnfairLock attribute to true.
#spring.datasource.maxWait=60000

# milliseconds for check to close free connections
#spring.datasource.timeBetweenEvictionRunsMillis=60000

# the Destroy thread detects the connection interval and closes the physical connection in milliseconds if the connection idle time is greater than or equal to minEvictableIdleTimeMillis.
#spring.datasource.timeBetweenConnectErrorMillis=60000

# the longest time a connection remains idle without being evicted, in milliseconds
#spring.datasource.minEvictableIdleTimeMillis=300000

#the SQL used to check whether the connection is valid requires a query statement. If validation Query is null, testOnBorrow, testOnReturn, and testWhileIdle will not work.
#spring.datasource.validationQuery=SELECT 1

#check whether the connection is valid for timeout, in seconds
#spring.datasource.validationQueryTimeout=3

# when applying for a connection, if it is detected that the connection is idle longer than time Between Eviction Runs Millis,
# validation Query is performed to check whether the connection is valid
#spring.datasource.testWhileIdle=true

#execute validation to check if the connection is valid when applying for a connection
#spring.datasource.testOnBorrow=true
#execute validation to check if the connection is valid when the connection is returned
#spring.datasource.testOnReturn=false
#spring.datasource.defaultAutoCommit=true
#spring.datasource.keepAlive=true

# open PSCache, specify count PSCache for every connection
#spring.datasource.poolPreparedStatements=true
#spring.datasource.maxPoolPreparedStatementPerConnectionSize=20